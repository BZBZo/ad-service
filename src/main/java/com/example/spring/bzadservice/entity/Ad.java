package com.example.spring.bzadservice.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

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

    @Column(updatable = false) // 등록 시에만 값 설정
    private LocalDateTime createdAt;

    @Column(name = "ad_image")
    private String adImage = "/images/default.jpg";

    @Column(name = "status")
    private String status = "보류"; // 기본값

    @Column(name = "hits")
    private Integer hits = 0; // 기본값

    // 엔티티 생성 시 자동으로 createdAt 설정
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}