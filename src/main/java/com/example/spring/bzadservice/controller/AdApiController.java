package com.example.spring.bzadservice.controller;

import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.dto.AdEditRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteResponseDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import com.example.spring.bzadservice.service.AdService;
import com.example.spring.bzadservice.service.ImgServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ad")
public class AdApiController {
    private final AdService adService;
    private final AdRepository adRepository;
    private final ImgServiceImpl imgServiceImpl;

    @PostMapping(value = "/write", consumes = "multipart/form-data")
    public ResponseEntity<?> saveAd(
            @RequestPart("adArea") String adPosition,
            @RequestPart("startDate") String adStart,
            @RequestPart("endDate") String adEnd,
            @RequestPart("adName") String adTitle,
            @RequestPart("adLink") String adUrl,
            @RequestPart("adImage") MultipartFile adImage) {
        try {
            // 날짜 파싱
            LocalDateTime adStartDate = LocalDateTime.parse(adStart);
            LocalDateTime adEndDate = LocalDateTime.parse(adEnd);

            // S3에 업로드할 고유한 파일 이름 생성
            String uniqueFileName = "static/bz-image/" + UUID.randomUUID();

            // S3에 이미지 업로드
            String imgUrl = imgServiceImpl.uploadImg(uniqueFileName, adImage);

            // 광고 정보 저장
            adService.saveAd(adPosition, adStartDate, adEndDate, adTitle, adUrl, imgUrl);

            // 성공 응답 반환
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "광고 신청이 완료되었습니다."
            ));
        } catch (Exception e) {
            // 예외 발생 시 에러 응답 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "광고 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage() // 에러 메시지 추가 (디버깅용)
            ));
        }
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

    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editAd(
            @PathVariable Long id,
            @RequestPart("adArea") String adPosition,
            @RequestPart("startDate") String adStart,
            @RequestPart("endDate") String adEnd,
            @RequestPart("adName") String adTitle,
            @RequestPart("adLink") String adUrl,
            @RequestPart("adImage") MultipartFile adImage
    ) {

        try {
            // 광고 수정 처리
            adService.editAd(id, adPosition, adStart, adEnd, adTitle, adUrl, adImage);

            // 성공 응답
            Map<String, String> response = new HashMap<>();
            response.put("message", "수정이 완료되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 요청 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "수정 중 오류가 발생했습니다.",
                    "details", e.getMessage() // 필요 시 세부 정보 포함
            ));
        }
    }

    @GetMapping("/list")
    public Page<AdDTO> getAds(@RequestParam("page") int page, @RequestParam("size") int size) {
        Page<Ad> adsPage = adService.getAds(page, size);

        Page<AdDTO> dtoPage = adsPage.map(ad -> AdDTO.builder()
                .id(ad.getId())
                .adPosition(ad.getAdPosition())
                .adStart(ad.getAdStart())
                .adEnd(ad.getAdEnd())
                .adTitle(ad.getAdTitle())
                .adUrl(ad.getAdUrl())
                .adImage(ad.getAdImage())
                .status(ad.getStatus())
                .hits(ad.getHits())
                .build()
        );

        return dtoPage;
    }

    @DeleteMapping("/erase")
    public ResponseEntity<Map<String, String>> deleteAds(@RequestBody List<Long> ids) {
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
            @RequestBody String newStatus) {

        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        ad.setStatus(newStatus); // 상태 업데이트
        adRepository.save(ad);

        return ResponseEntity.ok(Map.of("message", "상태가 성공적으로 변경되었습니다."));
    }

}