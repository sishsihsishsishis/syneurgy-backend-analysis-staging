package com.aws.sync.dto;

import lombok.Data;

import java.util.List;

@Data
public class MatchDTO {
    private List<String> users;
    private List<String> loginUsers;
}
