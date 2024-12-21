package com.example.spring.bzadservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
public class AdEditRequestDTO {
    private String adPosition;
    private String adStart;
    private String adEnd;
    private String adTitle;
    private String adUrl;
    private MultipartFile adImage;
}
