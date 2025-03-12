package com.aws.sync.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeakerUserVO {
    private String user_name;
    private List<String> speaker_name;
}
