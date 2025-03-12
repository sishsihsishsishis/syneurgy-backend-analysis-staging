package com.aws.sync.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PartETagWrapper {
    private int partNumber;

    @JsonProperty("eTag")
    private String eTag;

    public PartETagWrapper() {
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }
}