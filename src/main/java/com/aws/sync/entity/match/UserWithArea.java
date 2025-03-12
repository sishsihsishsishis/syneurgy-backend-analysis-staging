package com.aws.sync.entity.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithArea {
    private String speaker;
    private Double startTime;
    private Double endTime;
    private Double time;
    private int centerX;
    private int centerY;
    private int area;
    private int width;
    private int height;

    public UserWithArea(String speaker, int x1, int y1, int x2, int y2, double startTime, double endTime) {
        this.speaker = speaker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.time = (startTime + endTime) / 2;
        this.area = (x2 - x1) * (y2 - y1);
        this.centerX = (x1 + x2) / 2;
        this.centerY = (y1 + y2) / 2;
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
    }
}