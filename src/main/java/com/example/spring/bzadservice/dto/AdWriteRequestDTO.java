package com.example.spring.bzadservice.dto;

import com.example.spring.bzadservice.entity.Ad;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AdWriteRequestDTO {
    private String adPosition;
    private LocalDateTime adStart;
    private LocalDateTime adEnd;
    private String adTitle;
    private String adUrl;
    private MultipartFile adImage;
    private String adImagePath; // 이미지 경로 필드 추가

    public Ad toAd() {
        return Ad.builder()
                .adPosition(adPosition)
                .adStart(adStart)
                .adEnd(adEnd)
                .adTitle(adTitle)
                .adUrl(adUrl)
                .adImage(adImagePath) // 경로 설정
                .build();
    }
}