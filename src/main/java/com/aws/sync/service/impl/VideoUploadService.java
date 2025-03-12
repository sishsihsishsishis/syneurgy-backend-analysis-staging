package com.aws.sync.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.MeetingConstants;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.dto.VideoDTO;
import com.aws.sync.dto.video.VideoUploadDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.FrameGrab;

import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;

@Slf4j
@Service
public class VideoUploadService {

    @Value("${s3.base-url}")
    private String s3BaseUrl;

    @Value("${s3.storage.bucketName}")
    private String bucketName;

    @Autowired
    MeetingService meetingService;

    @Autowired
    private TransferManager transferManager;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AmazonS3 s3Client;

//    @Async
    public RestResult uploadVideoInBackground(VideoDTO video) {
        File tempFile = null;
//        File tempThumbnailFile = null;
        try {
            log.info("[VideoUploadService][uploadVideoInBackground] , info :{}", video);
            // Download the video using RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            Resource resource = restTemplate.getForObject(video.getMeetingUrl(), Resource.class);
            InputStream videoStream = resource.getInputStream();

            // Save the video to a temporary file
            tempFile = File.createTempFile("video" + System.currentTimeMillis(), ".mp4");

            ReadableByteChannel readChannel = Channels.newChannel(videoStream);
            FileOutputStream fileOS = new FileOutputStream(tempFile);
            WritableByteChannel writeChannel = Channels.newChannel(fileOS);

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (readChannel.read(buffer) != -1) {
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
            }
            writeChannel.close();
            readChannel.close();

            String fileName = video.getMeetingName() + ".mp4";
            // Upload the file to S3
            Upload upload = transferManager.upload(bucketName, "test/video/" + video.getMeetingName() + ".mp4", tempFile);
            // Optionally, wait for the upload to finish
            upload.waitForCompletion();



            // Step 4: Upload thumbnail to S3
        /*    String thumbnailFileName = UUID.randomUUID().toString().replace("-","") + "_thumbnail.jpg";
            Upload thumbnailUpload = transferManager.upload(bucketName, "test/thumbnail/" + thumbnailFileName, tempThumbnailFile);
            thumbnailUpload.waitForCompletion();
            String thumbnailFileKey = S3Prefix.THUMBNAIL_URL_PREFIX + thumbnailFileName;
            String thumbnailUrl = s3BaseUrl + thumbnailFileKey.replaceAll("\\+", "%2B");

            InputStream thumbnailInputStream = new FileInputStream(tempThumbnailFile);
            saveFileToS3(S3Prefix.VIDEO_BUCKET_NAME, S3Prefix.THUMBNAIL_IMG_PREFIX + thumbnailFileName, thumbnailInputStream, "image/jpeg");

            log.info("[VideoUploadService][uploadVideoInBackground], thumbnailUrl: {}", thumbnailUrl);*/



            //todo: 添加上传方式
            String fileKey = S3Prefix.VIDEO_FILE_PREFIX + fileName;
            // 生成文件的直接下载URL
            String fileUrl = s3BaseUrl + fileKey.replaceAll("\\+","%2B");
            log.info("[VideoUploadService][uploadVideoInBackground] , fileKey :{}, fileUrl:{}", fileKey, fileUrl);
            Long videoCreationTime = video.getVideoCreationTime() != null ? video.getVideoCreationTime() : System.currentTimeMillis();

            MeetingTable meetingTable =
                    new MeetingTable(video.getTeamId(), System.currentTimeMillis(),
                            MeetingConstants.MEETING_NOT_UPDATE,
                            MeetingConstants.MEETING_NOT_HANDLE,
                            fileUrl,
//                            S3Prefix.THUMBNAIL_URL_PREFIX + thumbnailFileName,
                            "S3Prefix.THUMBNAIL_URL_PREFIX + thumbnailFileName",
                            video.getMeetingName(),
                            videoCreationTime,
                            videoCreationTime,
                            video.getMeetingType());
            meetingService.save(meetingTable);
            String redisValue = "meeting" + meetingTable.getMeeting_id()+ ":" + fileKey + ":rppgCompare!" + 0.6;
            log.info("[VideoUploadService][uploadVideoInBackground] , meeting :{}, redisValue:{}", meetingTable, redisValue);
            redisTemplate.opsForZSet().add("SingleModelPreProcess", redisValue, System.currentTimeMillis());
            VideoUploadDTO videoUploadDTO = new VideoUploadDTO();
            BeanUtils.copyProperties(meetingTable, videoUploadDTO);
            return RestResult.success().data(videoUploadDTO);
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
           /* if (tempThumbnailFile != null) {
                tempThumbnailFile.delete();
            }*/
        }
    }

    private void saveFileToS3(String bucketName, String fileName, InputStream inputStream, String contentType) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        s3Client.putObject(bucketName, fileName, inputStream, objectMetadata);
    }

 /*   private BufferedImage toBufferedImage(Picture picture) {
        BufferedImage image = new BufferedImage(
                picture.getWidth(),
                picture.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        int[] dst = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        byte[] src = picture.getPlaneData(0); // Assuming YUV420P format
        for (int i = 0; i < dst.length; i++) {
            int y = src[i] & 0xff;
            dst[i] = (y << 16) | (y << 8) | y; // RGB
        }
        return image;
    }*/

}
