package com.example.spring.bzadservice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class GetResolvesTimesRequestDTO {
    private LocalDateTime adStart;
    private LocalDateTime adEnd;
}
