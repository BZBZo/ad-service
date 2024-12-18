package com.example.spring.bzadservice.service;

import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdService {
    @Autowired
    private AdRepository adRepository;

    public AdDTO getAdDTO(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Ad ID"));

        // Ad -> AdDTO 변환
        return AdDTO.builder()
                .adPosition(ad.getAdPosition())
                .adStart(Optional.ofNullable(ad.getAdStart()).orElse(LocalDateTime.now())) // 기본값 설정
                .adEnd(Optional.ofNullable(ad.getAdEnd()).orElse(LocalDateTime.now()))     // 기본값 설정
                .adTitle(ad.getAdTitle() != null ? ad.getAdTitle() : "광고명 없음")
                .adUrl(ad.getAdUrl())
                .adImage(ad.getAdImage() != null ? ad.getAdImage() : "/images/default.jpg")
                .status(ad.getStatus() != null ? ad.getStatus() : "미정")
                .hits(ad.getHits() != null ? ad.getHits() : 0)
                .build();
    }
    public Page<Ad> getAds(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return adRepository.findAll(pageable);
    }
}
