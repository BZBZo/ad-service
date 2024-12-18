package com.example.spring.bzadservice.controller;

import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/ad")
public class AdApiController {

    @Autowired
    private AdRepository adRepository;

    @PostMapping("/write")
    public String saveAd(@RequestParam("adArea") String adPosition,
                         @RequestParam("startDate") String adStart,
                         @RequestParam("endDate") String adEnd,
                         @RequestParam("adName") String adTitle,
                         @RequestParam("adLink") String adUrl,
                         @RequestParam("adImage") MultipartFile adImage) {
        // 광고 이미지 저장
        String imagePath = saveAdImage(adImage);

        // DateTimeFormatter를 사용해 파싱
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        // 광고 엔티티 생성 및 저장
        Ad ad = new Ad();
        ad.setAdPosition(adPosition);
        ad.setAdStart(LocalDateTime.parse(adStart, formatter)); // 형식 지정
        ad.setAdEnd(LocalDateTime.parse(adEnd, formatter));     // 형식 지정
        ad.setAdTitle(adTitle);
        ad.setAdUrl(adUrl);
        ad.setAdImage(imagePath);

        adRepository.save(ad);
        return "redirect:/ad/list";
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