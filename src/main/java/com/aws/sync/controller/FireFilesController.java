package com.aws.sync.controller;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.dto.firefiles.UserDTO;
import com.aws.sync.entity.firefiles.AudioUploadInput;
import com.aws.sync.entity.firefiles.DeleteTranscriptResponse;
import com.aws.sync.entity.firefiles.UploadAudioResponse;
import com.aws.sync.entity.firefiles.SetUserRoleResponse;
import com.aws.sync.service.firefiles.FireFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/firefiles")
public class FireFilesController {

    @Value("${fireFiles.auth-token}")
    String token;

    @Autowired
    private FireFilesService fireFilesService;

    @GetMapping("/userinfo")
    public RestResult getUserInfo() {
        return  fireFilesService.getUserinfo(token);
    }

    @GetMapping("/userinfo/{id}")
    public RestResult getUserInfoById(@PathVariable("id")String id) {
        return fireFilesService.getUserinfoById(token,id);
    }

    @GetMapping("/transcripts")
    public RestResult getTranscripts() {
        return fireFilesService.getTranscripts(token);
    }

    @GetMapping("/transcript/{id}")
    public RestResult getTranscriptById(@PathVariable("id")String transcriptId) {
        return fireFilesService.getTranscriptById(token, transcriptId);
    }

    @PostMapping("/audio")
    public RestResult uploadAudio(@RequestBody AudioUploadInput audioUploadInput) {
        return fireFilesService.uploadAudio(token, audioUploadInput);
    }

    @PostMapping("/role")
    public RestResult setRole(@RequestBody UserDTO user){
        return fireFilesService.setUserRole(token, user.getUserId(), user.getRole());
    }

    @PostMapping("/delete/{id}")
    public RestResult deleteTranscript(@PathVariable("id")String id){
        return fireFilesService.deleteTranscript(token, id);
    }

}
