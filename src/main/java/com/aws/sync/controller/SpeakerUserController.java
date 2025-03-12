package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.UpdateSpeakerNameDTO;
import com.aws.sync.entity.MeetingTable;
import com.aws.sync.entity.match.CVUser;
import com.aws.sync.entity.match.Speaker;
import com.aws.sync.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aws.sync.constants.MeetingConstants.SYNCHRONY_MOMENT_HANDLE;

@RestController
@RequestMapping
@CrossOrigin
public class SpeakerUserController {
    @Autowired
    private SpeakerUserService speakerUserService;

    @Autowired
    private CVUserService cvUserService;

    @Autowired
    private SpeakerService speakerService;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private NlpService nlpService;

    @Autowired
    private SynchronyMomentService synchronyMomentService;

    @PostMapping("/update-speaker-names")
    public RestResult updateSpeakerNames(@RequestBody UpdateSpeakerNameDTO updateSpeakerNameDTO) throws IOException {
//        boolean is_match = meetingService.checkMatch(updateSpeakerNameDTO.getMeeting_id());
//        if(is_match){
//            return RestResult.fail().message("已经匹配");
//        }

        //清数据
        if (meetingService.checkNlpHandle(updateSpeakerNameDTO.getMeeting_id())) {
            meetingService.removeNlpDataByMeetingId(updateSpeakerNameDTO.getMeeting_id());
        }
        //若之前匹配过，则清除匹配结果
        speakerUserService.deleteByMeetingID(updateSpeakerNameDTO.getMeeting_id());

        speakerUserService.updateSpeakerNames(updateSpeakerNameDTO);

        nlpService.handleNlp(updateSpeakerNameDTO.getMeeting_id());
        meetingService.updateMatch(updateSpeakerNameDTO.getMeeting_id());


        LambdaQueryWrapper<MeetingTable> syncMomentQueryWrapper = new LambdaQueryWrapper<>();
        syncMomentQueryWrapper.eq(MeetingTable::getMeeting_id,updateSpeakerNameDTO.getMeeting_id());
        List<MeetingTable> tables = meetingService.list(syncMomentQueryWrapper);
        //此时nlp已经处理
        if(meetingService.checkCVHandle(updateSpeakerNameDTO.getMeeting_id())){
            meetingService.updateHandle(updateSpeakerNameDTO.getMeeting_id());

            if(tables != null && tables.size() > 0 && tables.get(0).getSynchrony_moment_handle() == 0){
                synchronyMomentService.saveSmallest3(updateSpeakerNameDTO.getMeeting_id());
                UpdateWrapper<MeetingTable> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("meeting_id",updateSpeakerNameDTO.getMeeting_id())
                        .set("synchrony_moment_handle",SYNCHRONY_MOMENT_HANDLE);
                meetingService.update(null,updateWrapper);
            }
        }
        return RestResult.success().message("match success");
    }

    @GetMapping("/speaker-users")
    public RestResult getSpeakerUsersByMeetingId(@RequestParam Long meeting_id) {
        List<CVUser> userList = cvUserService.getUserList(meeting_id);
        List<Speaker> speakerList = speakerService.getSpeakerList(meeting_id);
//        List<SpeakerUser> speakerUsers = speakerUserService.getSpeakerUsersByMeetingId(meeting_id);
//        List<String> speakerNames = speakerUserService.getAllSpeakerNames(meeting_id);

        Map<String, Object> result = new HashMap<>();
        result.put("speakerUsers", userList);
        result.put("speakerNames", speakerList);
        return RestResult.success().data(result);
    }

    @GetMapping("/match-result/{meetingID}")
    public RestResult getMatchResult(@PathVariable("meetingID") Long meetingID) {
        return speakerUserService.getMatchUser(meetingID);
    }

    @GetMapping("/checkmatch/{meetingID}")
    public RestResult checkMatch(@PathVariable("meetingID") Long meetingID){
        boolean b = meetingService.checkMatch(meetingID);
        HashMap<String,Integer> ans = new HashMap<>();
        if(b){
            ans.put("is_match",1);
        }else {
            ans.put("is_match",0);
        }
        return RestResult.success().data(ans);

    }

    @ApiOperation("Delete Match")
    @PostMapping("/deletematch/{meetingID}")
    public RestResult deleteMatch(@PathVariable("meetingID")Long meetingID){
        meetingService.deleteMatch(meetingID);
        speakerUserService.deleteByMeetingID(meetingID);
        return RestResult.success().message("delete success");
    }

    @ApiOperation("Nlp data")
    @GetMapping("/nlp/{meetingID}")
    public RestResult getNlpData(@PathVariable("meetingID")Long meetingID){
        return nlpService.getNlpDataByMeetingID(meetingID);
    }

    @ApiOperation("Need Match List")
    @GetMapping("/need-match")
    public RestResult getNeedMatchList(){
        LambdaQueryWrapper<MeetingTable> meetingTableLambdaQueryWrapper = new LambdaQueryWrapper<>();
        meetingTableLambdaQueryWrapper.eq(MeetingTable::getIs_match, 0)
                .eq(MeetingTable::getCv_handle, 1)
                .eq(MeetingTable::getNlp_handle, 1);

        List<MeetingTable> list = meetingService.list(meetingTableLambdaQueryWrapper);
        return RestResult.success();
    }


}