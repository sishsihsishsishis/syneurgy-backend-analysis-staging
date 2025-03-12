package com.aws.sync.firefiles.audio;

import com.aws.sync.config.common.RestResult;
import com.aws.sync.entity.firefiles.AudioUploadInput;
import com.aws.sync.entity.firefiles.DeleteTranscriptResponse;
import com.aws.sync.entity.firefiles.UploadAudioResponse;
import com.aws.sync.entity.firefiles.SetUserRoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/file")
@RequestMapping()
public class FireFilesAudioController {

    @Value("${fireFiles.auth-token}")
    String token;

    @Autowired
    ApiService apiService;

    @PostMapping("/audio")
    public RestResult getUserInfo() {
        AudioUploadInput audioUploadInput = new AudioUploadInput();
        audioUploadInput.setUrl("https://url-to-the-audio-file");
        audioUploadInput.setTitle("test");
        UploadAudioResponse uploadAudioResponse = apiService.uploadAudio(token, audioUploadInput);
        return RestResult.success();
    }

    @PostMapping("/role")
    public RestResult role(){
        SetUserRoleResponse setUserRoleResponse = apiService.setUserRole(token, "u0ndbo8df6", "user");
        System.out.println("debug");
        return RestResult.success();
    }

    @PostMapping("/delete")
    public RestResult d(){
        DeleteTranscriptResponse deleteTranscriptResponse = apiService.deleteTranscript(token, "1");
        System.out.println("debug");
        return RestResult.success();
    }






}
