package com.aws.sync.config.common;

public enum SectionTeamInsightEnum {
    //0.Unified Energy
    //1.Serene Contentment
    //2.Driven Intensity
    //3.Calm Neutrality
    //4.Dynamic Passion
    //5.Harmonious Satisfaction
    //6.Mixed Intensity
    //7.Stable Equilibrium
    LABEL_0(0, "Unified Energy"),
    LABEL_1(1, "Serene Contentment"),
    LABEL_2(2, "Driven Intensity"),
    LABEL_3(3, "Calm Neutrality"),
    LABEL_4(4, "Dynamic Passion"),
    LABEL_5(5, "Harmonious Satisfaction"),
    LABEL_6(6, "Mixed Intensity"),
    LABEL_7(7, "Stable Equilibrium");
    private final int label;
    private final String sentence;

    SectionTeamInsightEnum(int label, String sentence) {
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
        for (SectionTeamInsightEnum labelEnum : SectionTeamInsightEnum.values()) {
            if (labelEnum.getLabel() == label) {
                return labelEnum.getSentence();
            }
        }
        throw new IllegalArgumentException("Invalid label: " + label);
    }
}
