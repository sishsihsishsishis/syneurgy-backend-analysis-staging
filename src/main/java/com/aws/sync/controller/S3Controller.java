package com.aws.sync.controller;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.config.common.ResultCodeEnum;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.entity.VideoAnalysisTable;
import com.aws.sync.service.*;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.aws.sync.constants.CsvConstants.*;
import static java.net.HttpURLConnection.HTTP_OK;

@Slf4j
@RestController
@RequestMapping("/s3")
@CrossOrigin
public class S3Controller {
    @Autowired
    AmazonS3 s3Client;

    @Autowired
    AmazonUploadService s3Service;

    @Autowired
    MeetingService meetingService;

    @Autowired
    SpeakerService speakerService;

    @Autowired
    NlpService nlpService;

    @Autowired
    CVHandleService cvHandleService;

    @Autowired
    VideoAnalysisService videoAnalysisService;

    @Autowired
    AnalysisService analysisService;

    /**
     * 上传文件到S3存储服务。
     *
     * @param file 用户上传的文件对象，通过表单的file字段接收。
     * @return 返回文件存储在S3服务上的md5值或者错误信息。
     */
    @ApiOperation("UploadFile")
    @RequestMapping(value = {"/upload"}, method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file) {
        log.info("[S3Controller][upload] file :{}", file.getOriginalFilename());
        return s3Service.saveFile(file);
    }

    /**
     * 根据会议ID上传文件，并更新视频分析状态。
     *
     * @param file 用户上传的文件对象，通过表单的file字段接收。
     * @param meetingId 路径变量传递的会议ID。
     * @return RestResult 上传操作的结果。
     */
    @ApiOperation("UploadFileAboutMeeting")
    @RequestMapping(value = {"/uploadByMeeting/{meetingId}"}, method = RequestMethod.POST)
    public RestResult uploadByMeetingID(@RequestParam(value = "file") MultipartFile file,
                                        @PathVariable("meetingId")String meetingId) {
        log.info("[S3Controller][uploadByMeetingID] OriginalFilename :{}, meetingId :{}", file.getOriginalFilename(), meetingId);
        //消除切分中存留的g问题
        if (meetingId.startsWith("g")) {
            meetingId = meetingId.replace("g", "");
        }
        Long meetingID = Long.parseLong(meetingId);
        s3Service.saveFileByMeetingID(file, meetingID);
        synchronized (this) {
            LambdaQueryWrapper<VideoAnalysisTable> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(VideoAnalysisTable::getMeeting_id, meetingID);
            VideoAnalysisTable videoAnalysis = videoAnalysisService.getOne(queryWrapper);
            boolean flag = false;
            if (videoAnalysis == null) {
                videoAnalysis = new VideoAnalysisTable(meetingID);
                flag = true;
            }
            LambdaUpdateWrapper<VideoAnalysisTable> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.eq(VideoAnalysisTable::getMeeting_id, meetingID);
            //消除fileName中的"
            String fileName = file.getOriginalFilename().replaceAll("\"", "");
            String currentFile = null;
            switch (fileName) {
                case NLP_FILE_NAME:
                    currentFile = NLP_FILE_NAME;
                    videoAnalysis.setNlp_status("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getNlp_status, "COMPLETED");
                    break;
                case CSV_READ_RPPG:
                    videoAnalysis.setRppg_status("COMPLETED");
                    if ("COMPLETED".equals(videoAnalysis.getA_status()) && "COMPLETED".equals(videoAnalysis.getV_status())) {
                        currentFile = CV_FILE_COMPLETE;
                        videoAnalysis.setCv_status("COMPLETED");
                        lambdaUpdateWrapper.set(VideoAnalysisTable::getCv_status, "COMPLETED");
                    }
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getRppg_status, "COMPLETED");
                    break;
                case CSV_READ_A:
                    videoAnalysis.setA_status("COMPLETED");
                    if ("COMPLETED".equals(videoAnalysis.getRppg_status()) && "COMPLETED".equals(videoAnalysis.getV_status())) {
                        currentFile = CV_FILE_COMPLETE;
                        videoAnalysis.setCv_status("COMPLETED");
                        lambdaUpdateWrapper.set(VideoAnalysisTable::getCv_status, "COMPLETED");
                    }
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getA_status, "COMPLETED");
                    break;
                case CSV_READ_V:
                    videoAnalysis.setV_status("COMPLETED");
                    if ("COMPLETED".equals(videoAnalysis.getRppg_status()) && "COMPLETED".equals(videoAnalysis.getA_status())) {
                        currentFile = CV_FILE_COMPLETE;
                        videoAnalysis.setCv_status("COMPLETED");
                        lambdaUpdateWrapper.set(VideoAnalysisTable::getCv_status, "COMPLETED");
                    }
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getV_status, "COMPLETED");
                    break;
                case ACTIVE_SPEAKER_CSV:
                    currentFile = ACTIVE_SPEAKER_CSV;
                    videoAnalysis.setActive_speaker_status("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getActive_speaker_status, "COMPLETED");
                    break;
                case ANCHOR_RESULT:
                    currentFile = ANCHOR_RESULT;
                    videoAnalysis.setAnchor_status("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getAnchor_status, "COMPLETED");
                    break;
                case POSTURE_DETECTION:
                    currentFile = POSTURE_DETECTION;
                    videoAnalysis.setPosture_detection("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getPosture_detection, "COMPLETED");
                    break;
                case EMOTION_DETECTION:
                    currentFile = EMOTION_DETECTION;
                    videoAnalysis.setEmotion_detection("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getEmotion_detection, "COMPLETED");
                    break;
                case BLINK_RESULT:
                    currentFile = BLINK_RESULT;
                    videoAnalysis.setBlink_results("COMPLETED");
                    lambdaUpdateWrapper.set(VideoAnalysisTable::getBlink_results, "COMPLETED");
                    break;
            }
/*            if ("COMPLETED".equals(videoAnalysis.getA_status()) && "COMPLETED".equals(videoAnalysis.getV_status()) &&
            "COMPLETED".equals(videoAnalysis.getRppg_status())) {
                currentFile = CV_FILE_COMPLETE;
                videoAnalysis.setCv_status("COMPLETED");
                lambdaUpdateWrapper.set(VideoAnalysisTable::getCv_status, "COMPLETED");
            }*/
            if (flag) {
                videoAnalysisService.save(videoAnalysis);
            } else {
                videoAnalysisService.update(null, lambdaUpdateWrapper);
            }
            log.info("[S3Controller][uploadByMeetingID] upload end!");
            try {
                analysisService.judgeIfAnalysisAsync(videoAnalysis, currentFile);
            } catch (Exception e) {
                return RestResult.fail().message(e.getMessage());
            }
        }

        return RestResult.success();
    }

    @ApiOperation("DownloadFile")
    @GetMapping("/download/{filename}/**")
    public ResponseEntity<byte[]> download(@PathVariable("filename") String filename, HttpServletRequest request) throws UnsupportedEncodingException {
        log.info("[S3Controller][download] filename :{}", filename);
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);
        String moduleName;

        if (null != arguments && !arguments.isEmpty()) {
            moduleName = filename + '/' + arguments;
        } else {
            moduleName = filename;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", MediaType.ALL_VALUE);
        headers.add("Content-Disposition", "attachment; filename=" + moduleName);

        byte[] bytes = s3Service.downloadFile(moduleName);
        return ResponseEntity.status(HTTP_OK).headers(headers).body(bytes);
    }

    @ApiOperation("DownloadFileByID")
    @GetMapping("/downloads/video")
    public ResponseEntity<byte[]> downloadByID(@Parameter Long meetingID, HttpServletRequest request) {
        log.info("[S3Controller][downloadByID] meetingID :{}", meetingID);
        String fileName = meetingService.findFileName(meetingID);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", MediaType.ALL_VALUE);
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        byte[] bytes = s3Service.downloadFile(fileName);
        return ResponseEntity.status(HTTP_OK).headers(headers).body(bytes);
    }

    @ApiOperation("DownloadCSVFile")
    @GetMapping("/downloads/csv")
    public ResponseEntity<byte[]> downloadCSV(@Parameter String meetingID,@Parameter String fileName, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", MediaType.ALL_VALUE);
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        byte[] bytes = s3Service.downloadCSV(meetingID + "/" + fileName);
        return ResponseEntity.status(HTTP_OK).headers(headers).body(bytes);
    }

//    @ApiOperation("获取文件的presignedURL")
    @RequestMapping(value = "/video/{filename}/**", method = RequestMethod.GET)
    public String GetVideo(@PathVariable("filename") String filename, HttpServletRequest request, HttpServletResponse response) {
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);

        String moduleName;
        if (null != arguments && !arguments.isEmpty()) {
            moduleName = filename + '/' + arguments;
        } else {
            moduleName = filename;
        }
        return s3Service.getPresignedURL(moduleName);
    }

    @ApiOperation("DeleteFile")
    @DeleteMapping("/delete/{filename}/**")
    public String deleteFile(@PathVariable("filename") String filename, HttpServletRequest request) {
        final String path =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();

        String arguments = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern, path);

        String moduleName;
        if (null != arguments && !arguments.isEmpty()) {
            moduleName = filename + '/' + arguments;
        } else {
            moduleName = filename;
        }
        return s3Service.deleteFile(moduleName);
    }

    @ApiOperation("FindAllFile")
    @GetMapping("/list")
    public List<String> getAllFiles() {

        return s3Service.listAllFiles();

    }

    @ApiOperation("VideoPlay")
    @RequestMapping(value = "/video", method = RequestMethod.GET)
    @ResponseBody
    public void VideoPlay(@Parameter Long meetingID,HttpServletResponse response) {
        String fileName = meetingService.findFileName(meetingID);
        byte[] data = s3Service.downloadFile(fileName);
        // 视频路径
//        String file = "C:\\Users\\lenovo\\Desktop\\syne\\demo1.mp4";
        try {
            //	获得视频文件的输入流
//            FileInputStream inputStream = new FileInputStream(file);
            // 创建字节数组，数组大小为视频文件大小
//            byte[] data = new byte[inputStream.available()];
            // 将视频文件读入到字节数组中
//            inputStream.read(data);
            String diskfilename = fileName;
            // 设置返回数据格式
            response.setContentType("video/mp4");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + diskfilename + "\"");
//            System.out.println("data.length " + data.length);
            response.setContentLength(data.length);
            response.setHeader("Content-Range", "" + Integer.valueOf(data.length - 1));
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Etag", "W/\"9767057-1323779115364\"");
            // 获得 response 的字节流
            OutputStream os = response.getOutputStream();
            // 将视频文件的字节数组写入 response中
            os.write(data);
            //先声明的流后关掉！
            os.flush();
            os.close();
//            inputStream.close();
        } catch (Exception e) {

        }
    }

    @ApiOperation("DownloadMeetingFile")
    @GetMapping("/downloads/{meetingID}/{fileName}")
    public ResponseEntity<byte[]> downloadMeetingFile(@PathVariable("meetingID") Long meetingID,@PathVariable String fileName, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", MediaType.ALL_VALUE);
        headers.add("Content-Disposition", "attachment; filename=" + fileName);
        byte[] bytes = s3Service.downloadMeetingFile(meetingID + "/" + fileName);
        return ResponseEntity.status(HTTP_OK).headers(headers).body(bytes);
    }

    @GetMapping("/download-folder")
    public void downloadFolderAsZip(

            @RequestParam String meetingId,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=meeting"+ meetingId +".zip");

        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            // 获取指定文件夹下的所有文件
            ObjectListing objectListing = s3Client.listObjects(S3Prefix.VIDEO_BUCKET_NAME, "test/meeting" + meetingId);

            for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                if (!os.getKey().endsWith("/")) { // 排除文件夹本身
                    addFileToZip(S3Prefix.VIDEO_BUCKET_NAME, os.getKey(), zipOut);
                }
            }

            // 如果还有更多对象，则继续分页获取
            while (objectListing.isTruncated()) {
                objectListing = s3Client.listNextBatchOfObjects(objectListing);
                for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
                    if (!os.getKey().endsWith("/")) {
                        addFileToZip(S3Prefix.VIDEO_BUCKET_NAME, os.getKey(), zipOut);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error generating ZIP file", e);
        }
    }

    private void addFileToZip(String bucketName, String key, ZipOutputStream zipOut) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, key);
        try (InputStream inputStream = s3Object.getObjectContent()) {
            ZipEntry zipEntry = new ZipEntry(key.substring(key.lastIndexOf('/') + 1));
            zipOut.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
            zipOut.closeEntry();
        }
    }

}
