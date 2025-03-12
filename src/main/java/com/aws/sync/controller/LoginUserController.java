package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.MatchDTO;
import com.aws.sync.service.CVUserService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping
@CrossOrigin
public class LoginUserController {
    @Autowired
    CVUserService cvUserService;

    @ApiOperation("Match Login User")
    @PostMapping("/match/{meetingID}")
    public RestResult matchLoginUser(@PathVariable("meetingID") Long meetingID,
                                     @RequestBody MatchDTO matchDTO) {
        log.info("[LoginUserController][matchLoginUser] matchDTO :{}", matchDTO);
        return cvUserService.matchLoginUser(meetingID, matchDTO);
    }

    @ApiOperation("Check Match")
    @GetMapping("/match/{meetingID}")
    public RestResult matchResult(@PathVariable("meetingID") Long meetingID){
        return cvUserService.getMatchResult(meetingID);
    }

//    @ApiOperation("Update Match")
//    @PostMapping("/updateMatch/{meetingID}")
//    public RestResult updateMatch(@PathVariable("meetingID")Long meetingID,@RequestParam("user")String user){
//        return cvUserService.updateMatch(meetingID,user);
//    }
//
//    @ApiOperation("Update Email")
//    @PostMapping("/updateEmail/{meetingID}")
//    public RestResult updateEmailSend(@PathVariable("meetingID")Long meetingID,@RequestParam("user")String user){
//        return cvUserService.updateEmailSend(meetingID,user);
//    }


}
