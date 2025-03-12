package com.aws.sync.config.common;

public enum SynchronyMomentInsightEnum {
    //0.1st in Sync
    //1.2nd in Sync
    //2.3rd in Sync
    LABEL_0(0, "1st in Sync"),
    LABEL_1(1, "2nd in Sync"),
    LABEL_2(2, "3rd in Sync");
    private final int label;
    private final String sentence;

    SynchronyMomentInsightEnum(int label, String sentence) {
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
        for (SynchronyMomentInsightEnum labelEnum : SynchronyMomentInsightEnum.values()) {
            if (labelEnum.getLabel() == label) {
                return labelEnum.getSentence();
            }
        }
        throw new IllegalArgumentException("Invalid label: " + label);
    }
}
