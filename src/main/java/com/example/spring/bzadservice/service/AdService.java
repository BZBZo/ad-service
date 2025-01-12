package com.example.spring.bzadservice.service;

import com.example.spring.bzadservice.config.s3.S3Uploader;
import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.dto.AdEditRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteRequestDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import lombok.RequiredArgsConstructor;
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
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final ImgServiceImpl imgServiceImpl;
    private final S3Uploader s3Uploader;

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

    public void editAd(Long id,
                       String adPosition, String adStart, String adEnd,
                       String adTitle, String adUrl, MultipartFile adImage) {
        // 광고 엔티티 조회
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다."));

        // 날짜 형식 변환 및 데이터 설정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        ad.setAdPosition(adPosition);
        ad.setAdStart(LocalDateTime.parse(adStart, formatter));
        ad.setAdEnd(LocalDateTime.parse(adEnd, formatter));
        ad.setAdTitle(adTitle);
        ad.setAdUrl(adUrl);

        // 이미지가 비어 있지 않으면 S3에 업로드 및 URL 설정
        if (adImage != null && !adImage.isEmpty()) {
            try {
                String uniqueFileName = "static/bz-image/" + UUID.randomUUID();
                String imgUrl = imgServiceImpl.uploadImg(uniqueFileName, adImage); // S3 업로드
                ad.setAdImage(imgUrl); // 새로운 이미지 URL 설정
            } catch (Exception e) {
                throw new RuntimeException("이미지 업로드 중 오류 발생: " + e.getMessage());
            }
        }

        // 변경된 광고 정보 저장
        adRepository.save(ad);
    }

    private boolean isSameImage(String existingImagePath, MultipartFile newImage) {
        if (existingImagePath == null || existingImagePath.isEmpty()) {
            return false; // 기존 이미지가 없는 경우 무조건 새로운 이미지로 저장
        }

        try {
            // 기존 이미지 파일과 새로운 이미지 파일의 내용을 비교
            byte[] existingImageBytes = Files.readAllBytes(Paths.get(existingImagePath));
            byte[] newImageBytes = newImage.getBytes();

            return Arrays.equals(existingImageBytes, newImageBytes);
        } catch (IOException e) {
            throw new RuntimeException("이미지 비교 중 오류 발생", e);
        }
    }

    public void deleteAdImages(Long id) {
        Optional<Ad> ad = adRepository.findById(id);
        String filePath = ad.get().getAdImage();
        try {
            s3Uploader.deleteS3(filePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
