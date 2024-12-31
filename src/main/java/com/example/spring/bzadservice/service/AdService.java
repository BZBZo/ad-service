package com.example.spring.bzadservice.service;

import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.dto.AdEditRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteRequestDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<Ad> getAdDetail(Long id) {
        return adRepository.findById(id);
    }

    //    public void saveAd(AdWriteRequestDTO adWriteRequestDTO) {
//        // 광고 이미지 저장
//        String imagePath = saveAdImage(adWriteRequestDTO.getAdImage());
//        adWriteRequestDTO.setAdImagePath(imagePath);
//
////        // DateTimeFormatter를 사용해 파싱
////        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
//
//        adRepository.save(adWriteRequestDTO.toAd());
//    }
    public void saveAd(String adPosition, LocalDateTime adStartDate, LocalDateTime adEndDate,
                       String adTitle, String adUrl, String imgUrl) {

        Ad ad = Ad.builder()
                .adPosition(adPosition)
                .adStart(adStartDate)
                .adEnd(adEndDate)
                .adTitle(adTitle)
                .adUrl(adUrl)
                .adImage(imgUrl) // S3에 저장된 이미지 url
                .build();

        adRepository.save(ad);
    }

    // 광고 이미지 저장 로직
    public String saveAdImage(MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                String uploadDir = "uploads/images/";
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + fileName);

                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());

                // 브라우저에서 접근 가능한 경로 반환
                return "/images/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void editAd(Long id, AdEditRequestDTO adEditRequestDTO) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다."));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        ad.setAdPosition(adEditRequestDTO.getAdPosition());
        ad.setAdStart(LocalDateTime.parse(adEditRequestDTO.getAdStart(), formatter));
        ad.setAdEnd(LocalDateTime.parse(adEditRequestDTO.getAdEnd(), formatter));
        ad.setAdTitle(adEditRequestDTO.getAdTitle());
        ad.setAdUrl(adEditRequestDTO.getAdUrl());

        if (adEditRequestDTO.getAdImage() != null && !adEditRequestDTO.getAdImage().isEmpty()) {
            String imagePath = saveAdImage(adEditRequestDTO.getAdImage());
            ad.setAdImage(imagePath);
        }

        adRepository.save(ad);
    }

}
