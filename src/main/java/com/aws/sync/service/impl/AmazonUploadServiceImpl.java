package com.aws.sync.service.impl;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.aws.sync.advice.SystemException;
import com.aws.sync.config.common.ResultCodeEnum;
import com.aws.sync.constants.S3Prefix;
import com.aws.sync.service.AmazonUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.aws.sync.constants.CsvConstants.NLP_DATA_NAME;

@Service
public class AmazonUploadServiceImpl implements AmazonUploadService {

    private static final int BLINK_USER_DATA_START_INDEX = 3;

    private static final int BLINK_SINGLE_USER_SIZE = 5;

    private static final String MEETING_FILE_PREX = "test/meeting";

    @Value("${s3.storage.bucketName}")
    private String bucketName;

    @Value("${s3.storage.root-directory}")
    private String root;

    private final AmazonS3 s3;


    public AmazonUploadServiceImpl(AmazonS3 s3) {
        this.s3 = s3;
    }


    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        try {
            File file1 = convertMultiPartToFile(file);
            PutObjectResult putObjectResult = s3.putObject(bucketName, root + "/" + "video" + "/" + originalFilename, file1);
            return putObjectResult.getContentMd5();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String saveFileByMeetingID(MultipartFile file, Long ID) {
        String originalFilename = file.getOriginalFilename().replaceAll("\"", "");
        try {
            File file1 = convertMultiPartToFile(file);
            if(ID == null){
                PutObjectResult putObjectResult1 = s3.putObject(bucketName, root + "/" +"video"+"/"+ originalFilename, file1);
                return putObjectResult1.getContentMd5();
            }else {
                PutObjectResult putObjectResult = s3.putObject(bucketName, root + "/meeting" + ID +"/"+ originalFilename, file1);
                return putObjectResult.getContentMd5();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String[]> readCSV(String filename, String meetingID) throws IOException {
        S3Object o  = s3.getObject(bucketName, MEETING_FILE_PREX + meetingID + "/" + filename);
        InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String s = reader.readLine();
        //读取标题行
        List<String[]> data = new ArrayList<>();
        while ((s = reader.readLine()) != null) {
            String[] split = s.split(",",-1);
            data.add(split);
        }
        inputStreamReader.close();
        reader.close();
        return data;
    }

    @Override
    public List<String[]> readBlinkData(String filename, Long meetingID) {
        List<String[]> data = new ArrayList<>();
        String key = MEETING_FILE_PREX + meetingID + "/" + filename;
        try (S3Object o = s3.getObject(bucketName, key);
             InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
             BufferedReader reader = new BufferedReader(inputStreamReader)) {

            // 读取标题行
            String titleLine = reader.readLine();

            String s;
            while ((s = reader.readLine()) != null) {
                String[] split = s.split(",", -1);
                int userNum = (split.length - BLINK_USER_DATA_START_INDEX) / BLINK_SINGLE_USER_SIZE;
                split = Arrays.copyOfRange(split, 0, BLINK_SINGLE_USER_SIZE + userNum);
                data.add(split);
            }
        } catch (IOException e) {
            throw new SystemException(ResultCodeEnum.IO_EXCEPTION, "Failed to read data from S3");
        }
        return data;
    }

    @Override
    public List<String> readCsvLine(String filename, String meetingID) throws IOException {
        S3Object o  = s3.getObject(bucketName, MEETING_FILE_PREX + meetingID + "/" + filename);
        InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String s = null;
        //读取标题行
        s = reader.readLine();
        List<String> data = new ArrayList<>();
        while ((s = reader.readLine()) != null) {
            data.add(s);
        }
        inputStreamReader.close();
        reader.close();
        return data;
    }

    @Override
    public void saveNlpData(List<String[]> nlpData, Long meeting) {
        StringBuilder sb = new StringBuilder();
        sb.append("Speaker\tStart\tEnd\tSentence\tEmotion\tDialogueAct\n");
        for (String[] row : nlpData) {
            String line = String.join("\t", row); // 将数组的元素以制表符分隔
            sb.append(line).append("\n"); // 添加换行符
        }
        s3.putObject(bucketName, MEETING_FILE_PREX + meeting + "/" + NLP_DATA_NAME, sb.toString());

    }

    @Override
    /**
     * Description 读取nlp文本文件(跳过标题行)
     * @author madi
     * @param filename 文件名
     * @param meetingID meetingId
     * @return  List<String[]>
     * */
    public List<String[]> readNlp(String filename, String meetingID, List<String> speakers
    ) throws IOException {
        S3Object o  = s3.getObject(bucketName, MEETING_FILE_PREX + meetingID + "/" + filename);
        InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String s = null;
        s = reader.readLine();
        HashSet<String> speakerList = new HashSet<>();
        List<String[]> data = new ArrayList<>();
        while ((s = reader.readLine()) != null)
        {
            String[] nlpRow = s.split("\t",-1);

            //源文件为speaker_00
            String speaker = nlpRow[0];
            speaker = speaker.replace("_","");
/*            for (Map.Entry<String, List<String>> entry : match.entrySet()) {
                List<String> value = entry.getValue();
                String key = entry.getKey();
                if(value.contains(speaker)){
                    speaker = "speaker" + key.substring(key.length() - 2);
                    break;
                }
            }*/

            nlpRow[0] = speaker;
            data.add(nlpRow);

            speakerList.add(speaker);
        }
        for (String user : speakerList) {
            speakers.add(user);
        }
        inputStreamReader.close();
        return data;
    }

    @Override
    public List<String[]> readNlpLine(String filename, String meetingID) throws IOException {
        S3Object o  = s3.getObject(bucketName, MEETING_FILE_PREX + meetingID + "/" + filename);
        InputStreamReader inputStreamReader = new InputStreamReader(o.getObjectContent());
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String s = null;
        s = reader.readLine();
        List<String[]> data = new ArrayList<>();
        while ((s = reader.readLine()) != null) {
            String[] nlpRow = s.split("\t",-1);
            nlpRow[0] = nlpRow[0].replace("_", "");
            data.add(nlpRow);
        }
        inputStreamReader.close();
        return data;
    }

    @Override
    public byte[] downloadMeetingFile(String filename) {
        S3Object object = s3.getObject(bucketName, MEETING_FILE_PREX + filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadFile(String filename) {
        S3Object object = s3.getObject(bucketName, "test/video/"+filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadUserAvatar(String filename){
        S3Object object = s3.getObject(bucketName,"test/" + filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] downloadThumbnail(String filename) {
        S3Object object = s3.getObject(bucketName,S3Prefix.THUMBNAIL_IMG_PREFIX + filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadCSV(String filename) {
        S3Object object = s3.getObject(bucketName, "test/"+filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(objectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPresignedURL(String filename) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, filename);
        Date now = new Date();
        Date expirateDate = new Date(now.getTime() + 60000 * 60);
        urlRequest.setExpiration(expirateDate);
        URL url = s3.generatePresignedUrl(urlRequest);
        return url.toString();

    }

    public StreamingResponseBody downloadFileStream(String filename) {
        S3Object object = s3.getObject(bucketName, filename);
        S3ObjectInputStream objectContent = object.getObjectContent();
        final StreamingResponseBody body = outputStream -> {
            int numberOfBytesToWrite = 0;
            byte[] data = new byte[1024];
            while ((numberOfBytesToWrite = objectContent.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, numberOfBytesToWrite);
            }
            objectContent.close();
        };

        return body;


    }

    @Override
    public String deleteFile(String filename) {

        s3.deleteObject(bucketName, filename);
        return "File deleted";
    }

    @Override
    public void deleteFolder(String folderPrefix) {
        try {
            ListObjectsV2Result result;
            result = s3.listObjectsV2(bucketName, folderPrefix);

            for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
                deleteFile(objectSummary.getKey());
            }

        } catch (AmazonServiceException e) {
            System.err.println("Amazon service error: " + e.getMessage());
            throw e;
        } catch (SdkClientException e) {
            System.err.println("Amazon SDK error: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public List<String> listAllFiles() {

        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(bucketName);
        return listObjectsV2Result.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());

    }

    /**
     * 列出S3桶中指定会议ID下的所有文件。
     *
     * @param prefix 需要查询文件前缀。
     * @return List<String> 返回一个包含文件键（key）的列表。
     */
    @Override
    public List<String> listFiles(String prefix) {
        ListObjectsV2Result listObjectsV2Result = s3.listObjectsV2(bucketName, prefix);
        return listObjectsV2Result.getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }


    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }
}