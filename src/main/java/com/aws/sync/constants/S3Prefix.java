package com.aws.sync.constants;

public class S3Prefix {
    public static final String VIDEO_FILE_PREFIX = "test/video/";
    public static final String THUMBNAIL_IMG_PREFIX = "test/thumbnail/";
    public static final String MEETING_FILE_PREFIX = "test/meeting";
//    public static final String VIDEO_BUCKET_NAME = "sync5";
    public static final String VIDEO_BUCKET_NAME = "sync-dev-server";
//    public static final String VIDEO_BUCKET_NAME = "hybrid-sync-server";
//    public static final String THUMBNAIL_URL_PREFIX = "https://sync5.s3.us-west-1.amazonaws.com/test/thumbnail/";
    public static final String THUMBNAIL_URL_PREFIX = "https://sync-dev-server.s3.us-west-1.amazonaws.com/test/thumbnail/";
//    public static final String THUMBNAIL_URL_PREFIX = "https://hybrid-sync-server.s3.us-west-1.amazonaws.com/test/thumbnail/";
//    public static final String VIDEO_URL_PREFIX = "https://sync5.s3.us-west-1.amazonaws.com/test/video/";
    public static final String VIDEO_URL_PREFIX = "https://sync-dev-server.s3.us-west-1.amazonaws.com/test/video/";
//    public static final String VIDEO_URL_PREFIX = "https://hybrid-sync-server.s3.us-west-1.amazonaws.com/test/video/";
}
