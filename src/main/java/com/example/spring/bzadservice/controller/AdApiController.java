package com.example.spring.bzadservice.controller;

import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.dto.AdWriteRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteResponseDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import com.example.spring.bzadservice.service.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ad")
public class AdApiController {
    private final AdService adService;

    @Autowired
    private AdRepository adRepository;

    @PostMapping("/write")
    public ResponseEntity<?> saveAd(@RequestBody AdWriteRequestDTO adWriteRequestDTO) {
        // 광고 이미지 저장
        String imagePath = saveAdImage(adWriteRequestDTO.getAdImage());
        adWriteRequestDTO.setAdImagePath(imagePath);

//        // DateTimeFormatter를 사용해 파싱
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        adRepository.save(adWriteRequestDTO.toAd());
        return ResponseEntity.ok(
                        AdWriteResponseDTO.builder()
                                .message("광고 신청이 완료되었습니다.")
                                .build()
                );
    }

    // 광고 이미지 저장 로직
    private String saveAdImage(MultipartFile file) {
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

    @GetMapping("/detail/{id}")
    AdDTO getAdDetail(@PathVariable Long id){
        // 광고 정보를 가져옴
        Ad ad = adService.getAdDetail(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        // AdDTO로 변환
        return AdDTO.builder()
                .id(ad.getId())
                .adPosition(ad.getAdPosition())
                .adStart(Optional.ofNullable(ad.getAdStart()).orElse(LocalDateTime.now()))
                .adEnd(Optional.ofNullable(ad.getAdEnd()).orElse(LocalDateTime.now()))
                .adTitle(ad.getAdTitle() != null ? ad.getAdTitle() : "광고명 없음")
                .adUrl(ad.getAdUrl())
                .adImage(ad.getAdImage() != null ? ad.getAdImage() : "/images/default.jpg") // 이미지 경로
                .status(ad.getStatus() != null ? ad.getStatus() : "미정")
                .hits(ad.getHits() != null ? ad.getHits() : 0)
                .build();
    }
    //여기까지 고정. 수정 ㄴㄴ바램

    @PostMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteAds(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "삭제할 광고를 선택하세요."));
        }
        try {
            adRepository.deleteAllById(ids);
            return ResponseEntity.ok(Map.of("message", "광고가 삭제되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/updateStatus/{id}")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");

        if (newStatus == null || newStatus.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "상태 값이 제공되지 않았습니다."));
        }

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        ad.setStatus(newStatus); // 상태 업데이트
        adRepository.save(ad);

        return ResponseEntity.ok(Map.of("message", "상태가 성공적으로 변경되었습니다."));
    }


}