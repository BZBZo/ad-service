package com.example.spring.bzadservice.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "now_ad")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class nowAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ad_position")
    private String adPosition = "위치 없음";

    @Column(name = "ad_start")
    private LocalDateTime adStart; // LocalDateTime으로 변경

    @Column(name = "ad_end")
    private LocalDateTime adEnd;   // LocalDateTime으로 변경

    @Column(name = "ad_title")
    private String adTitle = "광고명 없음";

    @Column(name = "ad_url")
    private String adUrl;

    @Column(name = "ad_image")
    private String adImage = "/images/default.jpg";

}