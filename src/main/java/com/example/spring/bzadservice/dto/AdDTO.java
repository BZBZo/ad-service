package com.example.spring.bzadservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
public class AdDTO {
    private Long id;
    private String adPosition;
    private LocalDateTime adStart; // LocalDateTime으로 변경
    private LocalDateTime adEnd;   // LocalDateTime으로 변경
    private String adTitle;
    private String adUrl;
    private String adImage;
    private String status;
    private Integer hits;

}


