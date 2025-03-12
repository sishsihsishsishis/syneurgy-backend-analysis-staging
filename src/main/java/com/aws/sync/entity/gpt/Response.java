package com.aws.sync.entity.gpt;

import lombok.Data;

import java.util.List;

@Data
class Response {
    private String id;
    private List<ChatCompletion.Choice> choices;
}