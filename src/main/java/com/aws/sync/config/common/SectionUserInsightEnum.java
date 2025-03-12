package com.aws.sync.config.common;

public enum SectionUserInsightEnum {
    //0.Energetic Inspiration
    //1.Tranquil Positivity
    //2.Determined Energy
    //3.Resilient Composure
    //4.Passionate Motivation
    //5.Equilibrium
    //6.Urgent Resolve
    //7.Steadfast Resilience
    LABEL_0(0, "Energetic Inspiration"),
    LABEL_1(1, "Tranquil Positivity"),
    LABEL_2(2, "Determined Energy"),
    LABEL_3(3, "Resilient Composure"),
    LABEL_4(4, "Passionate Motivation"),
    LABEL_5(5, "Equilibrium"),
    LABEL_6(6, "Urgent Resolve"),
    LABEL_7(7, "Steadfast Resilience");
    private final int label;
    private final String sentence;

    SectionUserInsightEnum(int label, String sentence) {
        this.label = label;
        this.sentence = sentence;
    }

    public int getLabel() {
        return this.label;
    }

    public String getSentence() {
        return this.sentence;
    }

    public static String getSentenceFromLabel(int label) {
        for (SectionUserInsightEnum labelEnum : SectionUserInsightEnum.values()) {
            if (labelEnum.getLabel() == label) {
                return labelEnum.getSentence();
            }
        }
        throw new IllegalArgumentException("Invalid label: " + label);
    }
}
