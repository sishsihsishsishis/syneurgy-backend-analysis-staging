package com.aws.sync.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Data
public class TeamScoreDTO {
    private Double coefficientBrain;
    private Double coefficientBody;
    private Double coefficientBehaviour;
    private Double coefficientTotal;
    private double weightBody;
    private double weightBrain;
    private double weightBehaviour;
    private double weighNlpTime;
    private double weightEqualParticipation;
    private List<Long> teamIds;
}

//                                 @RequestParam("coefficientBody")Double coefficientBody,
//                                  @RequestParam("coefficientBehaviour")Double coefficientBehaviour,
//                                  @RequestParam("coefficientTotal")Double coefficientTotal,
//                                  @RequestParam("weightBody")double weightBody,
//                                  @RequestParam("weightBehaviour")double weightBehaviour,
//                                  @RequestParam("weightNlpTime")double weighNlpTime,
//                                  @RequestParam("weightEqualParticipation")double weightEqualParticipation
