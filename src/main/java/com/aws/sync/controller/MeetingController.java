package com.aws.sync.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.RestResult;
import com.aws.sync.constants.CsvConstants;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.dto.MeetingSearchDTO;
import com.aws.sync.dto.SearchDTO;
import com.aws.sync.entity.*;
import com.aws.sync.service.*;
import com.aws.sync.utils.ContentTypeUtils;
import com.aws.sync.utils.CsvUtil;
import com.aws.sync.utils.NlpUtil;
import com.aws.sync.vo.*;
import com.aws.sync.vo.csv.Score;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.opencsv.CSVWriter;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/meeting")
public class MeetingController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    AResultService aResultService;

    @Autowired
    VResultService vResultService;

    @Autowired
    RResultService rResultService;

    @Autowired
    ASyncService aSyncService;

    @Autowired
    VSyncService vSyncService;

    @Autowired
    RSyncService rSyncService;

    @Autowired
    HeatmapService heatmapService;

    @Autowired
    AmazonUploadService amazonUploadService;

    @Autowired
    NlpService nlpService;

    @Autowired
    PieSpeakerService pieSpeakerService;

    @Autowired
    PieEmotionService pieEmotionService;

    @Autowired
    PieActService pieActService;

    @Autowired
    BarEmotionService barEmotionService;

    @Autowired
    RadarService radarService;

    @Autowired
    SectionService sectionService;

    @Autowired
    UserAvatarService userAvatarService;

    @Autowired
    IndividualAService individualAService;

    @Autowired
    IndividualVService individualVService;

    @Autowired
    IndividualRService individualRService;

    @Autowired
    AveSyncService aveSyncService;

    @Autowired
    IndividualSyncService individualSyncService;

    @Autowired
    CVHandleService cvHandleService;

    @Autowired
    SummaryService summaryService;

    @Autowired
    SynchronyMomentService synchronyMomentService;

/*    @Autowired
    SpeakerUserService speakerUserService;*/

    @Autowired
    AnalysisService analysisService;

    @Autowired
    AmazonS3 s3Client;

    /**
     * 更新指定会议的会议类型。
     *
     * @param meetingID 会议ID。
     * @param meetingType 会议类型。
     * @return RestResult 更新操作的结果，包含操作成功或失败的信息。
     */
    @ApiOperation("Update meeting type")
    @RequestMapping(value = {"/type/{meetingID}"}, method = RequestMethod.POST)
    public RestResult<Void> updateMeetingType(@PathVariable("meetingID")Long meetingID,
                                              @RequestParam("meetingType")String meetingType) {
        log.info("[MeetingController][updateMeetingType] meetingID :{}, meetingType :{}", meetingID, meetingType);
        return meetingService.updateMeetingType(meetingID, meetingType);
    }

    /**
     * 通过会议ID更新会议名称。
     *
     * @param meetingId 会议ID。
     * @param meetingName 新会议名称。
     * @return RestResult 返回操作的结果，包括操作成功或失败的相关信息。
     */
    @ApiOperation("Update meeting name")
    @RequestMapping(value = {"/update/{meetingId}"}, method = RequestMethod.POST)
    public  RestResult<Void> updateMeetingName(@PathVariable("meetingId")Long meetingId,
                                               @RequestParam("meetingName")String meetingName) {
        log.info("[MeetingController][updateMeetingName] meetingID :{}, meetingName :{}", meetingId, meetingName);
        return meetingService.updateMeetingName(meetingId,meetingName);
    }

    /**
     * 根据会议ID获取所有用户的头像URL列表。
     *
     * @param meetingID 会议ID。
     * @return RestResult 返回包含用户头像URL的结果对象。
     */
    @ApiOperation("GetUsrAvatar1")
    @RequestMapping(value = {"/avatar/{meetingID}"}, method = RequestMethod.GET)
    public RestResult<Map<String, Object>> userAvatarUrlList(@PathVariable("meetingID")Long meetingID) {
        List<UserAvatarVO> userAvatar = userAvatarService.findUserAvatar(meetingID);
        Map<String, String> userAvatarMap = userAvatar.stream()
                .collect(Collectors.toMap(UserAvatarVO::getUsers, UserAvatarVO::getUrl));

        Map<String, Object> res = new HashMap<>();
        res.put("userAvatar", userAvatarMap);
        return RestResult.success().data(res);
    }

    /**
     * 根据会议ID和头像url获取用户头像。
     *
     * @param httpServletResponse HTTP响应对象，用于写入头像数据。
     * @param meetingID 会议ID，路径变量。
     * @param url 头像文件的序号和格式后缀，如 "01.png"。
     * @return RestResult<Void> 如果操作成功，返回成功结果；如果发生错误，返回失败结果。
     */
    @ApiOperation("GetUserAvatar2")
    @RequestMapping(value = {"/img/{meetingID}/{url}"}, method = RequestMethod.GET)
    public RestResult<Void> userAvatar(HttpServletResponse httpServletResponse,
                                 @PathVariable("meetingID") Long meetingID,
                                 @PathVariable("url") String url) {
        List<UserAvatarVO> userAvatar = userAvatarService.findUserAvatar(meetingID);
        Integer index = Integer.valueOf(url.substring(url.lastIndexOf(".") - 2,url.lastIndexOf(".")));
        if (index < 0 || index >= userAvatar.size()) {
            return RestResult.fail().message("Invalid image index provided.");
        }
        url  = userAvatar.get(index).getUrl();
        byte[] img = amazonUploadService.downloadUserAvatar("meeting" + meetingID + "/" + url);
        //httpServletResponse.setContentType("image/png");
        try (OutputStream os = httpServletResponse.getOutputStream()) {
            os.write(img);
            os.flush();
        } catch (IOException e) {
            return RestResult.fail().message("Failed to send image data: " + e.getMessage());
        }
        return RestResult.success();
    }


    /**
     * 根据会议ID和用户名获取用户头像。
     *
     * @param httpServletResponse HTTP响应对象，用于写入头像数据。
     * @param meetingID 会议ID，路径变量。
     * @param userName 用户名。
     */
    @ApiOperation("Get user avatar by username and meetingId")
    @RequestMapping(value = {"/img/{meetingID}"}, method = RequestMethod.GET)
    public void getUserAvatar(HttpServletResponse httpServletResponse,
                              @PathVariable("meetingID")Long meetingID,
                              @RequestParam("userName")String userName) throws IOException {
        String userAvatarUrl;
        try {
            userAvatarUrl = userAvatarService.findUserAvatarUrl(meetingID, userName);
        } catch (SystemException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }

        String fileExtension = userAvatarUrl.substring(userAvatarUrl.lastIndexOf('.') + 1);
        String contentType = ContentTypeUtils.getContentType(fileExtension);

        byte[] img = amazonUploadService.downloadUserAvatar("meeting" + meetingID + "/" + userAvatarUrl);
        httpServletResponse.setContentType(contentType);
        try (OutputStream os = httpServletResponse.getOutputStream()) {
            os.write(img);
            os.flush();
        }
    }

    /**
     * 获取指定会议ID下的文件列表。
     *
     * @param meetingID 会议的ID，用于查询S3中的文件。
     * @return RestResult<List<String>> 返回操作成功的结果，包括会议相关的文件列表。
     */
    @ApiOperation("FindFileList")
    @RequestMapping(value = {"/file"}, method = RequestMethod.GET)
    public RestResult<List<String>> fileList(@RequestParam("meetingID")String meetingID) {
        String prefix = "test/meeting" + meetingID + "/";
        List<String> s3ObjectKeys = amazonUploadService.listFiles(prefix);
        MeetingTable meetingTable = meetingService.getByMeetingId(Long.valueOf(meetingID));
        if (meetingTable == null) {
            return RestResult.fail().message("meetingId not exist");
        }
        String video_url = meetingTable.getVideo_url();
        String regex = "https?://[^/]+/(.+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(video_url);

        String fileKey;
        if (matcher.find()) {
            fileKey = matcher.group(1); // 返回第一个捕获组的内容
        } else {
            return RestResult.fail().message("invalid meetingId");
        }
        List<String> fileList = new ArrayList<>();
        for (String key : s3ObjectKeys) {
            try {
                String[] parts = key.split("/");
                if (parts.length > 2) {
                    // Assuming the structure "bucketName/test/meetingID/fileName"
                    fileList.add(parts[parts.length - 1]);
                } else {
                    // Log error or handle the case where the format does not meet the expectation
                    System.err.println("Unexpected key format: " + key);
                }
            } catch (Exception e) {
                // Handle potential errors gracefully
                return RestResult.fail().message("Error processing S3 key: " + e.getMessage());
            }
        }
        fileList.add(fileKey);
        return RestResult.success().data(fileList);
    }

    /**
     * 处理nlp相关文件
     *
     * @param meetingID 会议的ID
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Nlp Handle")
    @RequestMapping(value = {"/nlp/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handleNlp(@PathVariable("meetingID")String meetingID) {
        log.info("[MeetingController][handleNlp] meetingID :{}", meetingID);
        try {
            return nlpService.handleNlp(Long.valueOf(meetingID));
            //return RestResult.success();
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 处理cv相关文件(a,v,r)
     *
     * @param meetingID 会议的ID
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("CV Handle")
    @RequestMapping(value = {"/cv/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handleCV(@PathVariable("meetingID")String meetingID) {
        log.info("[MeetingController][handleCV] meetingID :{}", meetingID);
        //add one need modify removeCv
        try {
            return cvHandleService.handleCV(Long.valueOf(meetingID));
            //return RestResult.success();
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 处理并更新会议score，根据提供的系数和权重计算不同维度的得分。
     *
     * @param meetingID 会议的唯一标识符。
     * @param coefficientBody 身体行为评分的系数。
     * @param coefficientBehaviour 行为评分的系数。
     * @param coefficientTotal 总评分的系数。
     * @param weightBody 身体行为的权重。
     * @param weightBehaviour 行为的权重。
     * @param weightNlpTime NLP 时间权重。
     * @param weightEqualParticipation 平等参与权重。
     * @return RestResult 返回操作的结果，包含计算后的评分数据。
     */
    @ApiOperation("Handle Score")
    @RequestMapping(value = {"/score/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handleScore(@PathVariable("meetingID")Long meetingID,
                                  @RequestParam("coefficientBody")int coefficientBody,
                                  @RequestParam("coefficientBehaviour")int coefficientBehaviour,
                                  @RequestParam("coefficientTotal")int coefficientTotal,
                                  @RequestParam("weightBody")double weightBody,
                                  @RequestParam("weightBehaviour")double weightBehaviour,
                                  @RequestParam("weightNlpTime")double weightNlpTime,
                                  @RequestParam("weightEqualParticipation")double weightEqualParticipation
                                ) {
        log.info("[MeetingController][handleScore] meetingID :{}", meetingID);
        try {
            //add one need modify removeCv
            List<String> userList = new ArrayList<>();
            //nlp data handle
            List<String[]> nlp_data = amazonUploadService.readNlp("nlp_results.txt", Long.toString(meetingID), userList);

            List<Double> speakers_time = new ArrayList<>();
            Double nlp_time = NlpUtil.get_time(nlp_data, speakers_time, userList) * 1000;
            Double equal_participation = NlpUtil.get_equal_participation(speakers_time, nlp_time, userList);

            String meeting = Long.toString(meetingID);
            List<String[]> dataA = amazonUploadService.readCSV(CsvConstants.CSV_READ_A, meeting);
            List<String[]> dataV = amazonUploadService.readCSV(CsvConstants.CSV_READ_V, meeting);
            List<String[]> dataR = amazonUploadService.readCSV(CsvConstants.CSV_READ_RPPG, meeting);
            //处理score
            Score scores = CsvUtil.get_score(
                    dataR, dataV, dataA, coefficientBody, coefficientBehaviour,0, coefficientTotal, weightBody,
                    weightBehaviour,0.0, weightNlpTime, weightEqualParticipation, nlp_time, equal_participation
            );
            scores.setMeeting_id(meetingID);
            meetingService.updateScore(scores);
            return RestResult.success().data(scores);
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 从Amazon S3下载并返回指定的缩略图。
     *
     * @param imageName 缩略图的文件名，通过路径变量传递。
     * @param httpServletResponse HTTP响应对象，用于写入图像数据到响应体。
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("load thumbnail")
    @RequestMapping(value = {"/thumbnail/{imageName}"}, method = RequestMethod.GET)
    public RestResult loadThumbnail(@PathVariable("imageName") String imageName,
                                    HttpServletResponse httpServletResponse) {
        try {
            byte[] img = amazonUploadService.downloadThumbnail(imageName);
//            httpServletResponse.setContentType("image/jpeg"); // 假设图片格式为JPEG
            try (OutputStream os = httpServletResponse.getOutputStream()) {
                os.write(img);
                os.flush();
            }
            return RestResult.success();
        } catch (IOException e) {
            return RestResult.fail().message("Failed to load thumbnail: " + e.getMessage());
        }
    }


    /**
     * 删除Cv相关数据。
     *
     * @param meetingID 会议ID。
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Delete CV Data")
    @RequestMapping(value = {"/remove-cv/{meetingID}"}, method = RequestMethod.POST)
    public RestResult deleteCVData(@PathVariable("meetingID")Long meetingID) {
        log.info("[MeetingController][deleteCVData] meetingID :{}", meetingID);
        return meetingService.removeCvDataByMeetingId(meetingID);
    }

    /**
     * 删除Nlp相关数据。
     *
     * @param meetingID 会议ID。
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Delete Nlp Data")
    @RequestMapping(value = {"/remove-nlp/{meetingID}"}, method = RequestMethod.POST)
    public RestResult deleteNlpData(@PathVariable("meetingID")Long meetingID) {
        log.info("[MeetingController][deleteNlpData] meetingID :{}", meetingID);
        return meetingService.removeNlpDataByMeetingId(meetingID);
    }

    /**
     * 删除meeting所有相关数据。
     *
     * @param meetingID 会议ID。
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Delete All Meeting Data")
    @RequestMapping(value = {"/remove/{meetingID}"}, method = RequestMethod.POST)
    public RestResult deleteAllData(@PathVariable("meetingID")Long meetingID) {
        log.info("[MeetingController][deleteAllData] meetingID :{}", meetingID);
        meetingService.removeCvDataByMeetingId(meetingID);
        meetingService.removeNlpDataByMeetingId(meetingID);
        HashMap<String,Object> deleteMap = new HashMap<>();
        deleteMap.put("meeting_id",meetingID);
        meetingService.removeByMap(deleteMap);
        return RestResult.success();
    }

    /**
     * 查询meeting相关信息。
     *
     * @param searchDTO searchDTO。
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Meeting Search")
    @RequestMapping(value = {"/search"}, method = RequestMethod.GET)
    public RestResult searchMeeting(SearchDTO searchDTO) {

        return meetingService.searchMeeting(searchDTO);
    }

    /**
     * 查询所有Video相关信息。
     *
     * @return RestResult<List<VideoVO>> 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("FindVideoList")
    @RequestMapping(value = {"/video"}, method = RequestMethod.GET)
    public RestResult<List<VideoVO>> videoList(){
        List<VideoVO> video = meetingService.findVideo();
        return RestResult.success().data(video);
    }


    /**
     * 查询team下的相关meeting progress。
     *
     * @param teamID 团队ID
     * @param progress progress
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Query meeting progress")
    @RequestMapping(value = {"/progress/{teamID}"}, method = RequestMethod.GET)
    public RestResult findVideoListByTeamID(@PathVariable("teamID")Long teamID,
                                            @RequestParam("progress") int progress) {
        return meetingService.findTeamMeetingProgress(teamID, progress);
    }

    /**
     * 查询team下的相关meeting信息。
     *
     * @param teamID 团队ID
     * @param meetingSearchDTO meetingSearchDTO
     * @return RestResult 返回操作成功的结果，或在发生错误时返回失败的结果。
     */
    @ApiOperation("Query meeting list by teamId")
    @RequestMapping(value = {"/video/{teamID}"}, method = RequestMethod.GET)
    public RestResult findVideoListByTeamID(@PathVariable("teamID")Long teamID,
                                            MeetingSearchDTO meetingSearchDTO) {
        return meetingService.findTeamMeetingByTeamID(teamID, meetingSearchDTO);
    }

    /**
     * 修改指定团队ID的最新会议类型。
     *
     * @param teamId 团队的唯一标识符。
     * @param meetingType 新的会议类型。
     * @param modCount 查询会议个数。
     * @return RestResult 返回操作的结果，成功或失败。
     */
    @ApiOperation("Modify latest meeting type")
    @RequestMapping(value = {"/video/{teamId}"}, method = RequestMethod.POST)
    public RestResult modLatestMeetingTypeByTeamId(@PathVariable("teamId")Long teamId,
                                                   @RequestParam("meetingType")String meetingType,
                                                   @RequestParam("modCount")int modCount) {
        log.info("[MeetingController][modLatestMeetingTypeByTeamId] teamId :{}, meetingType :{}, modCount :{}", teamId, meetingType, modCount);
        return meetingService.modLatestMeetingTypeByTeamId(teamId, meetingType, modCount);
    }

    /**
     * 查询指定团队的最近会议类型，可以按会议类型过滤。
     *
     * @param teamId 团队的唯一标识符。
     * @param meetingType 可选，用于过滤会议类型的参数。
     * @return RestResult 包含会议类型列表的结果。
     */
    @ApiOperation("Find latest meeting type")
    @RequestMapping(value = {"/latest-meeting/{teamId}"}, method = RequestMethod.GET)
    public RestResult findLatestMeetingTypeByTeamId(@PathVariable("teamId")Long teamId,
                                                    @RequestParam(required = false, name = "meetingType" )String meetingType) {
        List<MeetingTable> latestMeetingTypeByTeamId = meetingService.findLatestMeetingTypeByTeamId(teamId, meetingType);
        return RestResult.success().data(latestMeetingTypeByTeamId);
    }

    /**
     * 获取指定会议的概要信息。
     *
     * @param meetingID 会议的唯一标识符。
     * @return RestResult 返回包含会议概要的结果。
     */
    @ApiOperation("Summary about meeting")
    @RequestMapping(value = {"/summary/{meetingID}"}, method = RequestMethod.GET)
    public RestResult getSummaryAboutMeeting(@PathVariable("meetingID")Long meetingID) {
        return summaryService.getSummaryByMeetingId(meetingID);
    }

    /**
     * 获取指定会议的详细信息。
     *
     * @param meetingID 会议的唯一标识符。
     * @return RestResult 返回包含会议详细信息的结果。
     */
    @ApiOperation("Meeting Info")
    @RequestMapping(value = {"/info/{meetingID}"}, method = RequestMethod.GET)
    public RestResult getMeetingInfo(@PathVariable("meetingID")Long meetingID) {
        LambdaQueryWrapper<MeetingTable> meetingInfoWrapper = new LambdaQueryWrapper<>();
        meetingInfoWrapper.eq(MeetingTable::getMeeting_id,meetingID);
        List<MeetingTable> meetingTables = meetingService.list(meetingInfoWrapper);
        MeetingInfoVO meetingInfoVO = new MeetingInfoVO();
        if(meetingTables != null && meetingTables.size() > 0 && meetingTables.get(0) != null){
            BeanUtils.copyProperties(meetingTables.get(0),meetingInfoVO);
        }
        return RestResult.success().data(meetingInfoVO);
    }

    /**
     * 为指定会议ID保存最小的3个synchronyMoment。
     *
     * @param meetingID 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("SynchronyMoment")
    @RequestMapping(value = {"/synchrony-moment/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handleSynchronyMoment(@PathVariable("meetingID")Long meetingID) throws IOException {
        log.info("[MeetingController][handleSynchronyMoment] meetingID :{}", meetingID);
        synchronyMomentService.saveSmallest3(meetingID);
        return RestResult.success();
    }

    /**
     * 为指定会议ID添加额外信息。
     *
     * @param meetingID 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Add info")
    @RequestMapping(value = {"/info/{meetingID}"}, method = RequestMethod.POST)
    public RestResult postInfo(@PathVariable("meetingID")Long meetingID) {
        //TODO:查看真正处理时有没有用到
        log.info("[MeetingController][postInfo] meetingID :{}", meetingID);
        try {
            sectionService.addAdditionInfoToSection(meetingID);
        } catch (NoSuchFieldException e) {
            return RestResult.fail().message(e.getMessage());
        } catch (IllegalAccessException e) {
            return RestResult.fail().message(e.getMessage());
        } catch (IOException e) {
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }

    /**
     * 处理指定会议ID的部分CV数据。
     *
     * @param meetingID 会议的唯一标识符。
     * @param part 指定部分数据的序号，默认为0。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Handle partial cv data")
    @RequestMapping(value = {"/handle-partial-cv/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handlePartialCVData(@PathVariable("meetingID")Long meetingID, @RequestParam(name = "part", defaultValue = "0") Integer part) {
        log.info("[MeetingController][handlePartialCVData] meetingID :{}, part :{}", meetingID, part);
        try {
            return cvHandleService.handlePartCV(meetingID, part);
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 处理指定会议ID的部分NLP数据。
     *
     * @param meetingID 会议的唯一标识符。
     * @param part 指定部分数据的序号，默认为0。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Handle partial nlp data")
    @RequestMapping(value = {"/handle-partial-nlp/{meetingID}"}, method = RequestMethod.POST)
    public RestResult handlePartialNlpData(@PathVariable("meetingID")Long meetingID, @RequestParam(name = "part", defaultValue = "0") Integer part) {
        log.info("[MeetingController][handlePartialNlpData] meetingID :{}, part :{}", meetingID, part);
        try {
            return nlpService.handlePartNlp(meetingID, part);
        } catch (IOException e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 为指定会议ID处理NLP概要信息。
     *
     * @param meetingId 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Handle nlp summary")
    @RequestMapping(value = {"/handle-nlp-summary/{meetingId}"}, method = RequestMethod.POST)
    public RestResult handleNlpSummary(@PathVariable("meetingId")Long meetingId) {
        log.info("[MeetingController][handleNlpSummary] meetingID :{}", meetingId);
        try {
            return nlpService.handleNplSummary(meetingId);
        } catch (IOException e) {
            return RestResult.fail().message(e.getMessage());
        }
    }


    /**
     * 统计指定会议ID的NLP分类 (Trust(Consistency & Reliability))。
     *
     * @param meetingId 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("nlp summary")
    @RequestMapping(value = {"/nlp-summary/{meetingId}"}, method = RequestMethod.POST)
    public RestResult nlpSummary(@PathVariable("meetingId")Long meetingId) {
        log.info("[MeetingController][nlpSummary] meetingID :{}", meetingId);
        try {
            return nlpService.nlpSummary(meetingId);
        } catch (IOException e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 对指定会议ID进行NLP分类处理。
     *
     * @param meetingId 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("nlp classification")
    @RequestMapping(value = {"/nlp-classification/{meetingId}"}, method = RequestMethod.POST)
    public RestResult nlpClassification(@PathVariable("meetingId")Long meetingId) {
        log.info("[MeetingController][nlpClassification] meetingID :{}", meetingId);
        try {
            return nlpService.nlpClassification(meetingId);
        } catch (IOException e) {
            return RestResult.fail().message(e.getMessage());
        }
    }

    /**
     * 处理会议的雷达图数据，包括信任和心理安全的调整。
     *
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("handle radar")
    @RequestMapping(value = {"/radar"}, method = RequestMethod.POST)
    public RestResult handleRadar() {
        log.info("[MeetingController][handleRadar]");
        LambdaQueryWrapper<MeetingTable> meetingTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingTableLambdaQueryWrapper
                .eq(MeetingTable::getIs_handle, 0);
        List<MeetingTable> meetingTables = meetingService.list(meetingTableLambdaQueryWrapper);

        for (MeetingTable meetingTable : meetingTables) {
            if (meetingTable.getBehavior_score() != null && meetingTable.getBody_score() != null) {
                LambdaQueryWrapper<Radar> radarLambdaQueryWrapper = new LambdaQueryWrapper<>();
                radarLambdaQueryWrapper.eq(Radar::getMeeting_id, meetingTable.getMeeting_id());
                List<Radar> radars = radarService.list(radarLambdaQueryWrapper);
//                if (radars.size() == 5) {
//                    //1、找到Trust and Psychological Safety的索引
//                    int ind = -1;
//                    Double value = 0.0d;
//                    for (int i = 0; i < radars.size(); i++) {
//                        if ("Trust and Psychological Safety".equals(radars.get(i).getK())) {
//                            ind = i;
//                            value = radars.get(i).getV();
//                            break;
//                        }
//                    }
//                    if (ind != -1) {
//                        radars.remove(ind);
//                        Double behaviourScore = meetingTable.getBehavior_score();
//                        Double bodyScore = meetingTable.getBody_score();
//                        Double rateTrust = behaviourScore / (behaviourScore + bodyScore);
//                        if (rateTrust > 0.6) rateTrust = 0.6;
//                        if (rateTrust < 0.4) rateTrust = 0.4;
//                        Double ratePsy = 1 - rateTrust;
//                        radars.add(new Radar(meetingTable.getMeeting_id(), "Trust", rateTrust * value * 2));
//                        radars.add(new Radar(meetingTable.getMeeting_id(), "Psychological Safety", ratePsy * value * 2));
//                        HashMap<String,Object> deleteMap = new HashMap<>();
//                        deleteMap.put("meeting_id", meetingTable.getMeeting_id());
//                        radarService.removeByMap(deleteMap);
//                        radarService.insertRadar(radars);
//                    }
//                }
//
                if (radars.size() == 6) {
                    for (int i = 0; i < radars.size(); i++) {
                        if ("Trust".equals(radars.get(i).getK())) {
                            radars.get(i).setV(radars.get(i).getV() * 2);
                        } else if ("Psychological Safety".equals(radars.get(i).getK())) {
                            radars.get(i).setV(radars.get(i).getV() * 2);
                        }
                    }
                    HashMap<String,Object> deleteMap = new HashMap<>();
                    deleteMap.put("meeting_id", meetingTable.getMeeting_id());
                    radarService.removeByMap(deleteMap);
                    radarService.insertRadar(radars);

                }
            }
        }
        return RestResult.success();
    }

    /**
     * 对指定会议ID进行匹配处理。
     *
     * @param meetingId 会议的唯一标识符。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Handle Match")
    @RequestMapping(value = {"/match/{meetingId}"}, method = RequestMethod.POST)
    public RestResult handleMatch(@PathVariable("meetingId")Long meetingId) {
        log.info("[MeetingController][handleMatch] meetingID :{}", meetingId);
        try {
            meetingService.handleMatch(meetingId);
        } catch (Exception e) {
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }

    /**
     * 为指定会议ID提交分析时间数据
     *
     * @param meetingId 会议的唯一标识符。
     * @param time 提交的时间数据，包括各个分段的时间列表。
     * @return RestResult 返回操作成功的结果。
     */
    @ApiOperation("Post analysis time")
    @RequestMapping(value = {"/analysis-time/{meetingId}"}, method = RequestMethod.POST)
    public RestResult postAnalysisTime(@PathVariable("meetingId")Long meetingId,
                                @RequestBody HashMap<String, List<Long>> time) {
        log.info("[MeetingController][postAnalysisTime] meetingID :{}, timeInfo :{}", meetingId, time);
        return analysisService.saveAnalysisInfo(meetingId, time);
    }

/*    @ApiOperation("handle detection nlp")
    @PostMapping("/detection-nlp/{meetingId}")
    public RestResult testDetection(@PathVariable("meetingId") Long meetingId) throws IOException {
//        meetingService.handleDetection(meetingId, CsvConstants.EMOTION_DETECTION, 0);
//        meetingService.handleDetection(meetingId, CsvConstants.POSTURE_DETECTION, 1);
//        nlpService.handleDetectionNlp(meetingId);
        try {
            meetingService.handleBrainScore(meetingId);
        } catch (SystemException e) {
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }*/

//    @PostMapping("/generate-rppg")
//    public RestResult generateRppg(@RequestParam("meetingId") Long meetingId) {
//
//        List<Rsync> listRsync = CsvUtil.get_and_save_sync_r(10000, CsvConstants.CSV_FILE_RPPG, dataR, sync_r, meetingID, isr);
//    }

    @PostMapping("/download-meeting-data")
    public ResponseEntity<byte[]> downloadMeetingData(@RequestBody MeetingRequest request) {
        try {
            log.info("[MeetingController][downloadMeetingData] request :{}", request);
            List<Long> meetingIds = request.getMeetingIds();
            byte[] zipFile = generateZipFile(meetingIds);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=meeting_data.zip");
            return new ResponseEntity<>(zipFile, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



/*    public byte[] generateZipFile(List<Long> meetingIds) throws IOException {
        // Create a temporary directory to store the zip file contents
        Path tempDir = Files.createTempDirectory("meetingZip");

        // Step 1: Create CSV file
        Path csvFile = tempDir.resolve("meeting_data.csv");
        createCsvFile(csvFile, meetingIds);

        // Step 2: Copy folders for each meetingId
        for (Long meetingId : meetingIds) {
            Path meetingFolder = Paths.get("syne" + meetingId);
            Path destination = tempDir.resolve("syne" + meetingId);
            copyDirectory(meetingFolder, destination);
        }

        // Step 3: Create the zip file
        Path zipFile = Files.createTempFile(tempDir, "meeting_data", ".zip");
        zipDirectory(tempDir.toFile(), zipFile.toFile());

        // Return the byte content of the zip file
        return Files.readAllBytes(zipFile);
    }*/

//    public byte[] generateZipFile(List<Long> meetingIds) throws IOException {
//        // Step 1: 创建临时目录用于存放内容
//        Path tempDir = Files.createTempDirectory("meetingZip");
//
//        // Step 2: 创建 CSV 文件
//        Path csvFile = tempDir.resolve("meeting_data.csv");
//        createCsvFile(csvFile, meetingIds);
//
//        // Step 3: 复制 meetingId 对应的文件夹
//        for (Long meetingId : meetingIds) {
//            Path meetingFolder = Paths.get("syne" + meetingId); // 源文件夹路径
//            Path destination = tempDir.resolve("syne" + meetingId); // 临时目录中的目标路径
//            copyDirectory(meetingFolder, destination); // 复制文件夹
//        }
//
//        // Step 4: 将 tempDir 打包到独立的临时文件中，而不是存放在 tempDir 下
//        Path zipFile = Files.createTempFile("meeting_data", ".zip"); // 临时目录之外创建 ZIP 文件
//        zipDirectory(tempDir.toFile(), zipFile.toFile());
//
//        // Step 5: 读取 ZIP 文件内容返回
//        byte[] zipBytes = Files.readAllBytes(zipFile);
//
//        // 清理临时文件和目录（可选）
//        deleteDirectory(tempDir.toFile());
//        Files.deleteIfExists(zipFile); // 删除临时 ZIP 文件
//
//        return zipBytes;
//    }

    public byte[] generateZipFile(List<Long> meetingIds) throws IOException {
        // Step 1: 创建临时目录
        Path tempDir = Files.createTempDirectory("meetingZip");

        // Step 2: 创建 CSV 文件
        Path csvFile = tempDir.resolve("meeting_data.csv");
        createCsvFile(csvFile, meetingIds);

        // Step 3: 为每个 meetingId 创建独立文件夹并填充数据
        for (Long meetingId : meetingIds) {
            // 创建 meetingId 文件夹
            Path meetingFolder = tempDir.resolve("meeting_" + meetingId);
            Files.createDirectories(meetingFolder);

            // 复制本地 syne 文件夹内容到 meetingId 文件夹
            Path localSyneFolder = Paths.get("syne" + meetingId);
            if (Files.exists(localSyneFolder)) {
                Path localDestination = meetingFolder.resolve("syne" + meetingId);
                copyDirectory(localSyneFolder, localDestination);
            }

            // 从 S3 获取文件并放到 meetingId 文件夹
            List<S3ObjectSummary> s3Files = s3Client.listObjects(S3Prefix.VIDEO_BUCKET_NAME, S3Prefix.MEETING_FILE_PREFIX + meetingId + "/").getObjectSummaries();
            for (S3ObjectSummary s3File : s3Files) {
                String key = s3File.getKey();
                Path s3FileTarget = meetingFolder.resolve(Paths.get(key).getFileName().toString());
                Files.createDirectories(s3FileTarget.getParent()); // 确保目标目录存在
                try (S3Object s3Object = s3Client.getObject(S3Prefix.VIDEO_BUCKET_NAME, key);
                     InputStream inputStream = s3Object.getObjectContent()) {
                    Files.copy(inputStream, s3FileTarget, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            // Step 4: 根据 IndividualSync 创建每个 meetingId 的 CSV 文件
            createIndividualSyncCsv(meetingFolder, meetingId);
        }

        // Step 5: 将临时目录压缩到 ZIP 文件中
        Path zipFile = Files.createTempFile("meeting_data", ".zip");
        zipDirectory(tempDir.toFile(), zipFile.toFile());

        // Step 6: 读取 ZIP 文件字节内容返回
        byte[] zipBytes = Files.readAllBytes(zipFile);

        // 清理临时目录和文件
        deleteDirectory(tempDir.toFile());
        Files.deleteIfExists(zipFile);

        return zipBytes;
    }

    private void createIndividualSyncCsv(Path meetingFolder, Long meetingId) throws IOException {
        // 获取 meetingId 对应的所有 IndividualSync 数据
//        LambdaQueryWrapper<IndividualSync> queryWrapper = new LambdaQueryWrapper<IndividualSync>()
//                .eq(IndividualSync::getMeeting_id, meetingId)
//                .orderByDesc(IndividualSync::getTime_ms);
//        List<IndividualSync> individualSyncList = individualSyncService.list(queryWrapper);

        LambdaQueryWrapper<IndividualSyncA> queryAWrapper = new LambdaQueryWrapper<IndividualSyncA>()
                .eq(IndividualSyncA::getMeeting_id, meetingId)
                .orderByDesc(IndividualSyncA::getTime_ms);
        List<IndividualSyncA> individualASyncList = individualAService.list(queryAWrapper);

        LambdaQueryWrapper<IndividualSyncV> queryVWrapper = new LambdaQueryWrapper<IndividualSyncV>()
                .eq(IndividualSyncV::getMeeting_id, meetingId)
                .orderByDesc(IndividualSyncV::getTime_ms);
        List<IndividualSyncV> individualVSyncList = individualVService.list(queryVWrapper);

        LambdaQueryWrapper<IndividualSyncR> queryRWrapper = new LambdaQueryWrapper<IndividualSyncR>()
                .eq(IndividualSyncR::getMeeting_id, meetingId)
                .orderByDesc(IndividualSyncR::getTime_ms);
        List<IndividualSyncR> individualRSyncList = individualRService.list(queryRWrapper);

        // 按时间戳分组，并将用户映射到其 individual_sync 值
//        Map<Double, Map<String, Double>> timeGroupedData = individualSyncList.stream()
//                .collect(Collectors.groupingBy(
//                        IndividualSync::getTime_ms,
//                        Collectors.toMap(IndividualSync::getUsers, IndividualSync::getIndividual_sync)
//                ));


        Map<Double, Map<String, Double>> timeAGroupedData = individualASyncList.stream()
                .collect(Collectors.groupingBy(
                        IndividualSyncA::getTime_ms,
                        Collectors.toMap(
                                IndividualSyncA::getUsers,
                                IndividualSyncA::getIndividual_sync,
                                // 当出现重复键时，保留第一个遇到的值
                                (existing, replacement) -> existing
                        )
                ));
        List<Map.Entry<Double, Map<String, Double>>> sortedAEntries = new ArrayList<>(timeAGroupedData.entrySet());
        sortedAEntries.sort(Map.Entry.comparingByKey());  // 根据时间戳（key）升序排序

        ArrayList<Double> timeLine = new ArrayList<>(timeAGroupedData.keySet());
        Collections.sort(timeLine);

        Map<Double, Map<String, Double>> timeVGroupedData = individualVSyncList.stream()
                .collect(Collectors.groupingBy(
                        IndividualSyncV::getTime_ms,
                        Collectors.toMap(
                                IndividualSyncV::getUsers,
                                IndividualSyncV::getIndividual_sync,
                                // 当出现重复键时，保留第一个遇到的值
                                (existing, replacement) -> existing
                        )
                ));
        Map<Double, Map<String, Double>> timeRGroupedData = individualRSyncList.stream()
                .collect(Collectors.groupingBy(
                        IndividualSyncR::getTime_ms,
                        Collectors.toMap(
                                IndividualSyncR::getUsers,
                                IndividualSyncR::getIndividual_sync,
                                // 当出现重复键时，保留第一个遇到的值
                                (existing, replacement) -> existing
                        )
                ));

        // 获取所有用户，并保证列的顺序一致
        Set<String> users = individualASyncList.stream()
                .map(IndividualSyncA::getUsers)
                .collect(Collectors.toSet());
        List<String> userList = new ArrayList<>(users);
        userList.sort(String::compareTo);  // 按字母顺序排序，例如 user00, user01...

        Path individualSyncACsv = meetingFolder.resolve("individual_sync_a.csv");
        // 创建 CSV 文件并写入表头
        try (BufferedWriter writer = Files.newBufferedWriter(individualSyncACsv);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            // 表头
            String[] header = new String[userList.size() + 1];
            header[0] = "time_ms";
            for (int i = 0; i < userList.size(); i++) {
                header[i + 1] = userList.get(i);
            }
            csvWriter.writeNext(header);

//            // 写入每个时间点的数据
//            for (Map.Entry<Double, Map<String, Double>> entry : timeAGroupedData.entrySet()) {
//                Double time = entry.getKey();
//                Map<String, Double> userSyncMap = entry.getValue();
//
//                // 创建每一行数据
//                String[] row = new String[userList.size() + 1];
//                row[0] = time.toString(); // 时间戳
//                for (int i = 0; i < userList.size(); i++) {
//                    String user = userList.get(i);
//                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
//                            ? userSyncMap.get(user).toString()
//                            : "";
//                }
//                csvWriter.writeNext(row);
//            }

            // 写入每个时间点的数据（排序后的数据）
            for (Map.Entry<Double, Map<String, Double>> entry : sortedAEntries) {
                Double time = entry.getKey();
                Map<String, Double> userSyncMap = entry.getValue();

                // 创建每一行数据
                String[] row = new String[userList.size() + 1];
                row[0] = time.toString(); // 时间戳
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
                            ? userSyncMap.get(user).toString()
                            : "";
                }
                csvWriter.writeNext(row);
            }

//            for (Double time : timeLine) {
//
//                Map<String, Double> userSyncMap = timeAGroupedData.get(time);
//
//                // 创建每一行数据
//                String[] row = new String[userList.size() + 1];
//                row[0] = time.toString(); // 时间戳
//                for (int i = 0; i < userList.size(); i++) {
//                    String user = userList.get(i);
//                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
//                            ? userSyncMap.get(user).toString()
//                            : "";
//                }
//                csvWriter.writeNext(row);
//            }

        }

        Path individualSyncVCsv = meetingFolder.resolve("individual_sync_v.csv");
        // 创建 CSV 文件并写入表头
        try (BufferedWriter writer = Files.newBufferedWriter(individualSyncVCsv);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            // 表头
            String[] header = new String[userList.size() + 1];
            header[0] = "time_ms";
            for (int i = 0; i < userList.size(); i++) {
                header[i + 1] = userList.get(i);
            }
            csvWriter.writeNext(header);

/*            // 写入每个时间点的数据
            for (Map.Entry<Double, Map<String, Double>> entry : timeVGroupedData.entrySet()) {
                Double time = entry.getKey();
                Map<String, Double> userSyncMap = entry.getValue();

                // 创建每一行数据
                String[] row = new String[userList.size() + 1];
                row[0] = time.toString(); // 时间戳
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
                            ? userSyncMap.get(user).toString()
                            : "";
                }
                csvWriter.writeNext(row);
            }*/
            // 写入每个时间点的数据（排序后的数据）
            List<Map.Entry<Double, Map<String, Double>>> sortedVEntries = new ArrayList<>(timeAGroupedData.entrySet());
            sortedVEntries.sort(Map.Entry.comparingByKey());
            for (Map.Entry<Double, Map<String, Double>> entry : sortedVEntries) {
                Double time = entry.getKey();
                Map<String, Double> userSyncMap = entry.getValue();

                // 创建每一行数据
                String[] row = new String[userList.size() + 1];
                row[0] = time.toString(); // 时间戳
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
                            ? userSyncMap.get(user).toString()
                            : "";
                }
                csvWriter.writeNext(row);
            }

        }

        Path individualSyncRCsv = meetingFolder.resolve("individual_sync_r.csv");
        // 创建 CSV 文件并写入表头
        try (BufferedWriter writer = Files.newBufferedWriter(individualSyncRCsv);
             CSVWriter csvWriter = new CSVWriter(writer)) {
            // 表头
            String[] header = new String[userList.size() + 1];
            header[0] = "time_ms";
            for (int i = 0; i < userList.size(); i++) {
                header[i + 1] = userList.get(i);
            }
            csvWriter.writeNext(header);

            // 写入每个时间点的数据
        /*    for (Map.Entry<Double, Map<String, Double>> entry : timeRGroupedData.entrySet()) {
                Double time = entry.getKey();
                Map<String, Double> userSyncMap = entry.getValue();

                // 创建每一行数据
                String[] row = new String[userList.size() + 1];
                row[0] = time.toString(); // 时间戳
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
                            ? userSyncMap.get(user).toString()
                            : "";
                }
                csvWriter.writeNext(row);
            }*/
            List<Map.Entry<Double, Map<String, Double>>> sortedREntries = new ArrayList<>(timeRGroupedData.entrySet());
            sortedREntries.sort(Map.Entry.comparingByKey());
            for (Map.Entry<Double, Map<String, Double>> entry : sortedREntries) {
                Double time = entry.getKey();
                Map<String, Double> userSyncMap = entry.getValue();

                // 创建每一行数据
                String[] row = new String[userList.size() + 1];
                row[0] = time.toString(); // 时间戳
                for (int i = 0; i < userList.size(); i++) {
                    String user = userList.get(i);
                    row[i + 1] = userSyncMap.containsKey(user) && userSyncMap.get(user) != null
                            ? userSyncMap.get(user).toString()
                            : "";
                }
                csvWriter.writeNext(row);
            }
        }
    }

//    private void createIndividualSyncCsv(Path individualSyncCsv, Long meetingId) throws IOException {
//        // 获取 meetingId 对应的所有 IndividualSync 数据
//        LambdaQueryWrapper<IndividualSync> queryWrapper = new LambdaQueryWrapper<IndividualSync>()
//                .eq(IndividualSync::getMeeting_id, meetingId)
//                .orderByDesc(IndividualSync::getTime_ms);
//        List<IndividualSync> individualSyncList = individualSyncService.list(queryWrapper);
//
//        // 按时间升序排序
//        individualSyncList.sort(Comparator.comparing(IndividualSync::getTime_ms));
//
//        // 获取所有用户（userXX）
//        Set<String> users = individualSyncList.stream()
//                .map(IndividualSync::getUsers)
//                .collect(Collectors.toSet());
//        List<String> userList = new ArrayList<>(users);
//        userList.sort(String::compareTo);  // 按字母排序，例如 user00, user01, user02...
//
//        // Step 1: 创建 CSV 文件并写入表头
//        try (BufferedWriter writer = Files.newBufferedWriter(individualSyncCsv);
//             CSVWriter csvWriter = new CSVWriter(writer)) {
//            // 表头
//            String[] header = new String[userList.size() + 1];
//            header[0] = "time_ms";
//            for (int i = 0; i < userList.size(); i++) {
//                header[i + 1] = "user" + String.format("%02d", i); // user00, user01, ...
//            }
//            csvWriter.writeNext(header);
//
//            // Step 2: 遍历时间戳，填充每个时间点对应的用户同步值
//            for (Double time : individualSyncList.stream()
//                    .map(IndividualSync::getTime_ms)
//                    .distinct()
//                    .sorted()
//                    .collect(Collectors.toList())) {
//                // 获取每个时间点对应的用户同步值
//                Map<String, Double> timeSyncMap = individualSyncList.stream()
//                        .filter(sync -> sync.getTime_ms().equals(time))
//                        .collect(Collectors.toMap(IndividualSync::getUsers, IndividualSync::getIndividual_sync));
//
//                // 创建每一行数据
//                String[] row = new String[userList.size() + 1];
//                row[0] = time.toString(); // 时间戳
//                for (int i = 0; i < userList.size(); i++) {
//                    String user = userList.get(i);
//                    row[i + 1] = timeSyncMap.get(user) == null ? "NAN" : timeSyncMap.get(user).toString(); // 获取用户同步值
//                }
//                csvWriter.writeNext(row);
//            }
//        }
//    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) { // 递归删除目录中的文件和子目录
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete(); // 删除空目录
    }


    private void createCsvFile(Path csvFile, List<Long> meetingIds) throws IOException {
        List<String> radarKValues = Arrays.asList(
                "Shared Goal Commitment",
                "Psychological Safety",
                "Equal Participation",
                "Absorption or Task Engagement",
                "Enjoyment",
                "Trust"
        );
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile);
            CSVWriter csvWriter = new CSVWriter(writer)) {
            csvWriter.writeNext(new String[]{
                    "meeting_id", "meeting_name",
                    "body_score", "behavior_score", "total_score",
                    "Shared Goal Commitment", "Psychological Safety", "Equal Participation", "Absorption or Task Engagement", "Enjoyment", "Trust",
                    "Engage", "Alignment", "Agency", "Stress", "Burnout",
                    "Positive", "Neutral", "Negative"
            });
            for (Long meetingId : meetingIds) {
                MeetingTable meeting = meetingService.getByMeetingId(meetingId);
                MeetingSummaryVO meetingSummaryVO = meetingService.computeTeamMeetingSummary(Arrays.asList(meeting));
                List<PieEmotionVO> emotion = pieEmotionService.findEmotion(meetingId);
                Map<String, Double> sentimentMap = emotion.stream()
                        .collect(Collectors.toMap(PieEmotionVO::getEmotion, PieEmotionVO::getEmotion_time_rate));

                if (meeting != null) {
                    List<RadarVO> radars = radarService.findKV(meetingId);
                    String[] radarValues = new String[6];
                    for (RadarVO radar : radars) {
                        int index = radarKValues.indexOf(radar.getK());  // 根据k值找到对应的索引
                        if (index != -1) {
                            radarValues[index] = radar.getV().toString();  // 将v值放入对应的index位置
                        }
                    }
                    for (int i = 0; i < radarValues.length; i++) {
                        if (radarValues[i] == null) {
                            radarValues[i] = "";  // 填充为空
                        }
                    }
                    csvWriter.writeNext(new String[]{
                            //meeting info
                            meeting.getMeeting_id().toString(),
                            meeting.getMeeting_name(),

                            //Performance
                            meeting.getBody_score() == null ? "" : meeting.getBody_score().toString(),
                            meeting.getBehavior_score() == null ? "" : meeting.getBehavior_score().toString(),
                            meeting.getTotal_score() == null ? "" : meeting.getTotal_score().toString(),

                            //radar
                            radarValues[0],  // Shared Goal Commitment
                            radarValues[1],  // Psychological Safety
                            radarValues[2],  // Equal Participation
                            radarValues[3],  // Absorption or Task Engagement
                            radarValues[4],  // Enjoyment
                            radarValues[5],   // Trust

                            //Status
                            meetingSummaryVO.getEngagement() == null ? "" : meetingSummaryVO.getEngagement().toString(),
                            meetingSummaryVO.getAlignment() == null ? "" : meetingSummaryVO.getAlignment().toString(),
                            meetingSummaryVO.getAgency() == null ? "" : meetingSummaryVO.getAgency().toString(),
                            meetingSummaryVO.getStress() == null ? "" : meetingSummaryVO.getStress().toString(),
                            meetingSummaryVO.getBurnout() == null ? "" : meetingSummaryVO.getBurnout().toString(),

                            //sentiment
                            sentimentMap.get("positive") == null ? "" : sentimentMap.get("positive").toString(),
                            sentimentMap.get("neutral") == null ? "" : sentimentMap.get("neutral").toString(),
                            sentimentMap.get("negative") == null ? "" : sentimentMap.get("negative").toString(),
                    });
                }
            }
        }
    }

    private void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        Files.walk(sourceDir)
                .forEach(sourcePath -> {
                    try {
                        Files.copy(sourcePath, targetDir.resolve(sourceDir.relativize(sourcePath)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            addFolderToZip(sourceDir, sourceDir, zos);
        }
    }

    private void addFolderToZip(File folder, File baseFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addFolderToZip(file, baseFolder, zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(baseFolder.toPath().relativize(file.toPath()).toString());
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
}

class MeetingRequest {
    private List<Long> meetingIds;

    // getter 和 setter
    public List<Long> getMeetingIds() {
        return meetingIds;
    }

    public void setMeetingIds(List<Long> meetingIds) {
        this.meetingIds = meetingIds;
    }

    @Override
    public String toString() {
        return "MeetingRequest{" +
                "meetingIds=" + meetingIds +
                '}';
    }
}
