package com.aws.sync.entity.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMessage {
    private String model;
    private List<Message> messages;
    private double temperature;

    public RequestMessage(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
}