package com.aws.sync.entity.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithCenter {
    private String userName;
    private Double time;
    private int centerX;
    private int centerY;
    private int width;
    private int height;

    public UserWithCenter(String userName, int x1, int y1, int x2, int y2) {
        this.userName = userName;
        this.centerX = (x1 + x2) / 2;
        this.centerY = (y1 + y2) / 2;
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
    }

    public UserWithCenter(Double time, int x1, int y1, int x2, int y2) {
        this.time = time;
        this.centerX = (x1 + x2) / 2;
        this.centerY = (y1 + y2) / 2;
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
    }
}