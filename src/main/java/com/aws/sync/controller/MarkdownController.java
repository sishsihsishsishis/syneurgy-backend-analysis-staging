package com.aws.sync.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;


@RestController
@CrossOrigin
public class MarkdownController {

    @GetMapping(value = "/markdown", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public String getMarkdown() throws IOException {
        Path markdownPath = Paths.get("/usr/notify.md");
        return Files.lines(markdownPath).collect(Collectors.joining("\n"));
    }
}



