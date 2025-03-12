package com.aws.sync.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.config.common.ResultCodeEnum;
import com.aws.sync.constants.MeetingConstants;
import com.aws.sync.constants.S3Prefix;

import com.aws.sync.dto.VideoDTO;
import com.aws.sync.dto.video.VideoUploadDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.PartETagWrapper;
import com.aws.sync.entity.UserAvatar;
import com.aws.sync.service.MeetingService;
import com.aws.sync.service.UserAvatarService;
import com.aws.sync.service.impl.VideoUploadService;
import com.aws.sync.utils.JsonProcessor;
import com.aws.sync.utils.MediaInfo;
import com.aws.sync.utils.ShortLinkMediaParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping(value = "/video")
public class VideoController {

	@Value("${s3.base-url}")
	private String s3BaseUrl;

	@Value("${s3.storage.bucketName}")
	private String bucketName;

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private MeetingService meetingService;

	@Autowired
	private UserAvatarService userAvatarService;

	@Autowired
	private VideoUploadService videoUploadService;

	@Autowired
	WebClient.Builder webClientBuilder;

	/**
	 * Initiates a new multipart upload.
	 *
	 * @param fileName the name of the file to be uploaded
	 * @return the uploadId
	 */
	@ApiOperation("initiate-upload")
	@PostMapping("/initiate-upload")
	public String initiateUpload(@RequestParam("fileName") String fileName) {
		if(!fileName.endsWith(".mp4") && !fileName.endsWith(".MP4")){
			throw new SystemException(ResultCodeEnum.FILE_TYPE_ERROR);
		}
		log.info("[VideoController][initiateUpload] fileName :{}", fileName);
		//TODO:消除fileName空格问题
		InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(S3Prefix.VIDEO_BUCKET_NAME, S3Prefix.VIDEO_FILE_PREFIX + fileName);
		String uploadId = s3Client.initiateMultipartUpload(initiateRequest).getUploadId();
		redisTemplate.opsForHash().put("uploadIdToFileMap", uploadId, fileName);
		return uploadId;
	}

	/**
	 * Uploads a part of the file.
	 *
	 * @param uploadId   the uploadId of the initiated multipart upload
	 * @param partNumber the part number
	 * @param file       the chunked part
	 * @return ETag of the uploaded part
	 * @throws IOException if there is an issue with file handling
	 */
	@PostMapping("/upload-part")
	public ResponseEntity<String> uploadPart(@RequestParam("uploadId") String uploadId,
											 @RequestParam("partNumber") int partNumber,
											 @RequestParam("data") MultipartFile file) throws IOException {
		log.info("[VideoController][uploadPart] uploadId :{}, partNumber :{}, file :{}", uploadId, partNumber, file.getOriginalFilename());
		String fileName = (String) redisTemplate.opsForHash().get("uploadIdToFileMap", uploadId);
		if (fileName == null) {
			return ResponseEntity.badRequest().body("Invalid uploadId");
		}
		InputStream inputStream = file.getInputStream();
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(file.getSize());
		UploadPartRequest uploadPartRequest = new UploadPartRequest()
				.withBucketName(S3Prefix.VIDEO_BUCKET_NAME)
				.withKey(S3Prefix.VIDEO_FILE_PREFIX + fileName)
				.withUploadId(uploadId)
				.withPartNumber(partNumber)
				.withPartSize(file.getSize())
				.withInputStream(inputStream);

		String eTag = s3Client.uploadPart(uploadPartRequest).getPartETag().getETag();
		System.out.println(file.getSize());
		return ResponseEntity.ok(eTag);
	}

	/**
	 * Handles the completion of a video upload process.
	 *
	 * @param thumbnailFile The thumbnail image file for the video.
	 * @param teamId The ID of the team associated with the video.
	 * @param uploadId A unique identifier for the upload session.
	 * @param partETagsJson JSON string representing the eTags for each part of the video upload.
	 * @param videoCreationTime Optional timestamp of when the video was created.
	 * @param meetingName Optional name of the meeting related to the video.
	 * @param meetingType Optional type of the meeting.
	 * @return RestResult object containing the upload details or error information.
	 * @throws IOException If there is an error reading from the thumbnail file input stream.
	 */
	@PostMapping("/complete-upload/{teamId}")
	public RestResult completeUpload(@RequestParam("thumbnail") MultipartFile thumbnailFile,
									 @PathVariable("teamId")Long teamId,
									 @RequestParam("uploadId") String uploadId,
									 @RequestParam("partETags") String partETagsJson,
									 @RequestParam(value = "videoCreationTime", required = false)Long videoCreationTime,
									 @RequestParam(value = "meetingName", required = false)String meetingName,
									 @RequestParam(value = "meetingType", required = false)String meetingType
									 ) throws IOException {
		log.info("[VideoController][completeUpload] uploadId :{}, meetingName :{}, meetingType :{}", teamId, meetingName, meetingType);
		// 提取 partETags 列表
		List<PartETagWrapper> partETagWrappers = new ObjectMapper().readValue(partETagsJson, new TypeReference<List<PartETagWrapper>>() {});

		// 转换 PartETagWrapper 对象为 PartETag 对象
		List<PartETag> partETags = partETagWrappers.stream()
				.map(wrapper -> new PartETag(wrapper.getPartNumber(), wrapper.getETag()))
				.collect(Collectors.toList());
		String fileName = (String) redisTemplate.opsForHash().get("uploadIdToFileMap", uploadId);
		redisTemplate.opsForHash().delete("uploadIdToFileMap", uploadId);

		if (fileName == null) {
			return RestResult.fail().message("Invalid uploadId");
		}
		CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(S3Prefix.VIDEO_BUCKET_NAME, S3Prefix.VIDEO_FILE_PREFIX + fileName, uploadId, partETags);
		s3Client.completeMultipartUpload(completeRequest);

		// 将缩略图保存到 S3
		String thumbnailFileName = UUID.randomUUID().toString().replace("-","") + "_thumbnail.jpg";
		InputStream thumbnailInputStream = thumbnailFile.getInputStream();
		saveFileToS3(S3Prefix.VIDEO_BUCKET_NAME, S3Prefix.THUMBNAIL_IMG_PREFIX + thumbnailFileName, thumbnailInputStream, thumbnailFile.getContentType());
		//TODO: 缩略图 URL
		String fileKey = S3Prefix.VIDEO_FILE_PREFIX + fileName;
		// 生成文件的直接下载URL
		String fileUrl = s3BaseUrl + fileKey.replaceAll("\\+","%2B");

		//Set data about MeetingTable
		String meeting_name = fileName.substring(0,fileName.lastIndexOf("."));
		meetingName = (meetingName != null && !meetingName.isEmpty()) ? meetingName : meeting_name;
		meetingType = (meetingType != null && !meetingType.isEmpty()) ? meetingType : MeetingConstants.DEFAULT_MEETING_TYPE;
		videoCreationTime = videoCreationTime != null ? videoCreationTime : System.currentTimeMillis();
		MeetingTable meetingTable =
				new MeetingTable(teamId, System.currentTimeMillis(),
						MeetingConstants.MEETING_NOT_UPDATE,
						MeetingConstants.MEETING_NOT_HANDLE,
						fileUrl,
						S3Prefix.THUMBNAIL_URL_PREFIX + thumbnailFileName,
//						meeting_name,
						meetingName,
						videoCreationTime,
						videoCreationTime,
						meetingType);
		meetingService.save(meetingTable);
		log.info("completeUpload :{}", meetingTable);
//		redisTemplate.opsForZSet().add("meeting","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey,System.currentTimeMillis());
		//redisTemplate.opsForZSet().add("meeting_test","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
//		redisTemplate.opsForZSet().add("video-speaker-detector","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());


		redisTemplate.opsForZSet().add("SingleModelPreProcess","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":rppgCompare!" + 0.6, System.currentTimeMillis());
//		sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/analysis");
		//for dev
		sendAsyncRequest("https://z7n6sy6bqbgq4hmuapnsvdjko40nknxl.lambda-url.us-east-2.on.aws/lts/start/analysis_staging");
		/*redisTemplate.opsForZSet().add("pose-estimation","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
		redisTemplate.opsForZSet().add("emotion-detector","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
		redisTemplate.opsForZSet().add("eye-blinking-detection","meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":" + 0.6, System.currentTimeMillis());
*/
		VideoUploadDTO videoUploadDTO = new VideoUploadDTO();
		BeanUtils.copyProperties(meetingTable, videoUploadDTO);

		return RestResult.success().data(videoUploadDTO);
	}

	private  void sendAsyncRequest(String url) {

		String requestBody = "{}";  // 示例为空的请求体
//        WebClient.Builder builder = WebClient.builder();
		// 异步发送 POST 请求
/*		webClientBuilder.build()
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(String.class) // 解析响应体为 String
				.retryWhen(Retry.fixedDelay(5, Duration.ofMinutes(1)))
				.doOnTerminate(() -> System.out.println("Request completed"))  // 请求完成后执行
				.subscribe(response -> {
					// 处理响应
					System.out.println("Response: " + response);
				}, error -> {
					// 错误处理
					System.err.println("Error occurred: " + error.getMessage());
				});*/
		webClientBuilder.build()
				.get()
				.uri(url)
				.retrieve()
				.bodyToMono(String.class)
				.retryWhen(Retry.fixedDelay(5, Duration.ofMinutes(1))
						.doBeforeRetry(retrySignal ->
								System.out.println("Retrying after failure. Attempt: " + (retrySignal.totalRetries() + 1))
						)
				)
				.doOnTerminate(() -> System.out.println("Request completed"))
				.subscribe(
						response -> System.out.println("Response: " + response),
						error -> System.err.println("Error occurred after 5 retries: " + error.getMessage())
				);
	}

	/**
	 * Uploads a user avatar to a specific meeting and associates it with a username.
	 *
	 * @param meetingId The ID of the meeting to which the avatar is associated.
	 * @param username The username of the user uploading the avatar.
	 * @param avatarFile The uploaded file object containing the avatar image.
	 * @return RestResult object indicating the success or failure of the upload.
	 * @throws IOException If there is an error reading from the avatar file input stream.
	 */
	@ApiOperation("upload avatar")
	@PostMapping("/avatar/{meetingId}/{username}")
	public RestResult uploadUserAvatar(@PathVariable("meetingId")Long meetingId,
									   @PathVariable("username") String username,
									   @RequestParam("file") MultipartFile avatarFile) throws IOException {
		log.info("[VideoController][uploadUserAvatar] meetingId :{}, username :{}, avatarFile :{}", meetingId, username, avatarFile.getOriginalFilename());
		InputStream avatarFileInputStream = avatarFile.getInputStream();
		String originalFilename = avatarFile.getOriginalFilename();

		// 检查文件名是否有效
		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new IllegalArgumentException("文件名不能为空");
		}

		// 检查文件名中是否包含 "."，如果没有则添加默认后缀 ".jpg"
		int lastDotIndex = originalFilename.lastIndexOf(".");
		String fileName;
		if (lastDotIndex == -1) {
			fileName = username + ".jpg"; // 没有后缀时，添加默认后缀
		} else {
			fileName = username + originalFilename.substring(lastDotIndex); // 有后缀时，保留原后缀
		}
//		String fileName = username + originalFilename.substring(originalFilename.lastIndexOf("."));
		saveFileToS3(S3Prefix.VIDEO_BUCKET_NAME,S3Prefix.MEETING_FILE_PREFIX + meetingId + "/" + fileName,avatarFileInputStream,avatarFile.getContentType());
		HashMap<String, Object> delMap = new HashMap<>();
		delMap.put("meeting_id", meetingId);
		delMap.put("users", username);
		userAvatarService.removeByMap(delMap);
		UserAvatar userAvatar = new UserAvatar(meetingId, username, fileName);
		userAvatarService.save(userAvatar);
		return RestResult.success().data("upload avatar success!");
	}

/*	@ApiOperation("UploadFileAboutMeeting")
	@PostMapping(value = "/uploadByMeeting/{meetingID}")
	public RestResult uploadByMeetingID(@RequestParam(value = "file") MultipartFile file,
										@PathVariable("meetingID")Long meetingID) throws IOException {
		s3Service.saveFileByMeetingID(file,meetingID);
		if(CsvConstants.NLP_FILE_NAME.equals(file.getOriginalFilename())){
			int count = s3Service.getSpeakerCount(meetingID);
			HashMap<String, Object> delMap = new HashMap<>();
			delMap.put("meeting_id", meetingID);
			speakerService.removeByMap(delMap);
			speakerService.updateSpeaker(meetingID, count);
			meetingService.updateNlpFile(meetingID);
		}
		return RestResult.success();
	}*/

//	@ApiOperation("Upload Video By Url")
//	@PostMapping("/upload")
//	public RestResult uploadVideoByUrl(VideoDTO videoDTO){
//		if(videoDTO.getMeetingUrl() == null){
//			return RestResult.fail().message("Invalid Url");
//		}
//		String meetingUrl = videoDTO.getMeetingUrl();
////		try {
////			URL url = new URL(meetingUrl);
////			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
////			connection.setRequestMethod("HEAD"); // 只需要头部信息，所以使用HEAD请求
////			int responseCode = connection.getResponseCode();
////
////			if (responseCode == HttpURLConnection.HTTP_OK) {
////				String contentType = connection.getContentType();
////				//假设我们认为"application"开头的MIME类型都表示可下载的链接
////				if (contentType.startsWith("application")) {
////					//TODO: download video
////					System.out.println("This URL is a downloadable link.");
////				} else {
////					System.out.println("This URL is not a downloadable link.");
////				}
////			} else {
////				System.out.println("Can't access the URL. Response code: " + responseCode);
////			}
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//		if(!isUrlDownloadable(meetingUrl)){
//			return RestResult.fail().message("not a file");
//		}
//		return RestResult.success();
//	}
	@ApiOperation("Upload Video By Url")
	@PostMapping("/upload")
	public RestResult uploadVideo(@RequestBody VideoDTO video) {
		log.info("[VideoController][uploadVideo] video :{}", video);
		if(!isUrlDownloadable(video.getMeetingUrl())){
			return RestResult.fail().message("the url is not downloadable!");
		}

		try {
	//		// Download the video using RestTemplate
	//		RestTemplate restTemplate = new RestTemplate();
	//		Resource resource = restTemplate.getForObject(video.getMeetingUrl(), Resource.class);
	//		InputStream videoStream = resource.getInputStream();
	//
	//		// Save the video to a temporary file
	//		File tempFile = File.createTempFile("video" + System.currentTimeMillis(), ".tmp");
	//
	//		ReadableByteChannel readChannel = Channels.newChannel(videoStream);
	//		FileOutputStream fileOS = new FileOutputStream(tempFile);
	//		WritableByteChannel writeChannel = Channels.newChannel(fileOS);
	//
	//		ByteBuffer buffer = ByteBuffer.allocate(1024);
	//		while (readChannel.read(buffer) != -1) {
	//			buffer.flip();
	//			writeChannel.write(buffer);
	//			buffer.clear();
	//		}
	//		writeChannel.close();
	//		readChannel.close();
	//
	//		// Upload the file to S3
	//		Upload upload = transferManager.upload(bucketName, "test/video/" + video.getMeetingName() + ".mp4", tempFile);
	//
	//		// Optionally, wait for the upload to finish
	//		upload.waitForCompletion();
	//
	//		meetingService.save(
	//				new MeetingTable(video.getDate(),video.getMeetingName(),S3Prefix.VIDEO_URL_PREFIX + video.getMeetingName().replaceAll("\\+","%2B") + ".mp4",video.getMeetingType())
	//		);
			return videoUploadService.uploadVideoInBackground(video);
//			return RestResult.success().message("Upload started");
		} catch (Exception e) {
			return RestResult.fail().message("Upload failed: " + e.getMessage());
		}
	}

	private boolean isUrlDownloadable(String urlStr) {
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			// 可下载的url通常会返回200的响应码
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return true;
			}
		} catch (Exception e) {
			//处理错误
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return false;
	}

	/**
	 * Saves a file to the specified S3 bucket.
	 *
	 * @param bucketName the target S3 bucket
	 * @param fileName   the name of the file to be saved
	 * @param inputStream the file input stream
	 * @param contentType the content type of the file
	 */
	private void saveFileToS3(String bucketName, String fileName, InputStream inputStream, String contentType) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(contentType);
		s3Client.putObject(bucketName, fileName, inputStream, objectMetadata);
	}

	@GetMapping("/info")
	public RestResult getVideoInfo (@RequestParam("url")String share_url) throws Exception {
		System.out.println(share_url);
		if (share_url.contains("https://v.douyin.com")) {
			return parseDY(share_url);
		} else if (share_url.contains("xhslink.com")) {
			share_url = extractLink(share_url);
			MediaInfo mediaInfo = ShortLinkMediaParser.parseRed(share_url);
			return RestResult.success().data(mediaInfo);
		} else {
			return RestResult.fail().message("暂未开发");
		}
	}

		@GetMapping("/info2")
		public RestResult getVideoInfo2 (@RequestParam("url")String url) throws IOException {
			MediaInfo mediaInfo = ShortLinkMediaParser.parseRed(url);
			return RestResult.success().data(mediaInfo);
		}


		// 获取itemId
//        String itemId = redirectUrl.split("https://www.iesdouyin.com/share/video/")[0].split("/\\?region=")[0];
/*        String itemId = redirectUrl.split("video/")[1].split("\\?")[0];
        // 重新信息页面
        String newUrl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids="+itemId;
        // 获取主页面，核心数据
        String body = Jsoup.connect(newUrl).ignoreContentType(true).execute().body();
        // 获取列表
        String item_list = String.valueOf(JsonToMapUtil.toMap(body).get("item_list"));
        String substring = item_list.substring(1, item_list.length() - 1);
        String video = String.valueOf(JsonToMapUtil.toMap(substring).get("video"));
        String play_addr = String.valueOf(JsonToMapUtil.toMap(video).get("play_addr"));
        String url_list = String.valueOf(JsonToMapUtil.toMap(play_addr).get("url_list"));
        String play_url = url_list.substring(2, url_list.length() - 2);
        String videoUrl = (play_url.split("https://aweme.snssdk.com/aweme")[1].split("&ratio=720p&line=0")[0]).replace("playwm","play");
        String playUrl = "https://aweme.snssdk.com/aweme"+videoUrl+"&ratio=720p&line=0";
        String downLoad = DownloadUtil.download(playUrl);*/

	private RestResult parseDY(String share_url) throws Exception {
			String temp = share_url.split("com/")[1].split("/")[0];

			share_url = "https://v.douyin.com/" + temp;
			// 通过短连接获取长链接
			String redirectUrl = Jsoup.connect(share_url).followRedirects(true).execute().url().toString();
			int index = redirectUrl.indexOf('?');
			if (index != -1) {
				// 若存在 '?'，则截取 '?' 之前的部分
				redirectUrl = redirectUrl.substring(0, index);
			}
			String item_id = "";
			if (redirectUrl.contains("/video")) {
				item_id = redirectUrl.split("\\?")[0].split("video/")[1];
			} else {
				item_id = redirectUrl.split("\\?")[0].split("note/")[1];
			}
			// 获取短连接码
			System.out.println(redirectUrl);
			String userAgent = "Mozilla/5.0 (Linux; Android 8.0.0; SM-G955U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36";
			String referer = "https://www.douyin.com/?is_from_mobile_home=1&recommend=1";
			// 创建 URL 对象
			URL url = new URL("https://www.iesdouyin.com/share/video/" + item_id);
			// 打开连接
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// 设置请求头
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", userAgent);
			connection.setRequestProperty("Referer", referer);

			// 读取响应内容
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			// 正则匹配 _ROUTER_DATA
			String responseBody = response.toString();
			Pattern pattern = Pattern.compile("_ROUTER_DATA\\s*=\\s*(\\{.*?\\});");
			Matcher matcher = pattern.matcher(responseBody);

			if (matcher.find()) {
				// 返回匹配到的 JSON 数据
				System.out.println(matcher.group(1));
				MediaInfo mediaInfo = JsonProcessor.parseMediaInfo(matcher.group(1));
				System.out.println(mediaInfo);
				return RestResult.success().data(mediaInfo);
			} else {
				return RestResult.fail();
			}
		}

	public static String extractLink(String input) {
		String urlPattern = "((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
		Pattern pattern = Pattern.compile(urlPattern);
		Matcher matcher = pattern.matcher(input);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

}