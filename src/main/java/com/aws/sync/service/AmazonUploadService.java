package com.aws.sync.service;

import com.aws.sync.config.common.RestResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface AmazonUploadService {

    String saveFile(MultipartFile file);

    byte[] downloadFile(String filename);

    byte[] downloadUserAvatar(String filename);

    byte[] downloadThumbnail(String filename);

    byte[] downloadCSV(String filename);

    String deleteFile(String filename);

    void deleteFolder(String folderPrefix);

    String getPresignedURL(String filename);

    List<String> listAllFiles();

    List<String> listFiles(String key);

    String saveFileByMeetingID(MultipartFile file,Long ID);

    List<String[]> readCSV(String filename, String meetingID) throws IOException;

    List<String[]> readBlinkData(String filename, Long meetingID);

    List<String> readCsvLine(String filename,String meetingID) throws IOException;

    List<String[]> readNlp(String filename, String meetingID, List<String> users) throws IOException;

    List<String[]> readNlpLine(String filename, String meetingID) throws IOException;

    byte[] downloadMeetingFile(String filename);

    void saveNlpData(List<String[]> nlpData, Long meetingId);
}