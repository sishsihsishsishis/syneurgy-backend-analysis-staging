package com.aws.sync.entity.gpt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Choice {
    public int index;
    public Message message;
    public String finish_reason;
}