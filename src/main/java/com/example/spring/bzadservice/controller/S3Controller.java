package com.example.spring.bzadservice.controller;

import com.example.spring.bzadservice.service.ImgServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final ImgServiceImpl imgServiceImpl;
    /**
     * 그룹(팀) 생성
     * @param name
     * @param file
     * @return
     */
    @PostMapping(path = "/imgUpload", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity uploadImg(
            @RequestPart(value = "name") String name,
            @RequestPart(value = "file", required = false) MultipartFile file
    ){
        imgServiceImpl.uploadImg(name, file);
        return new ResponseEntity(null, HttpStatus.OK);
    }
}
