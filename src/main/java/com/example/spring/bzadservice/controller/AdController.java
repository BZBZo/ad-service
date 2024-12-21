package com.example.spring.bzadservice.controller;

import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.repository.AdRepository;
import com.example.spring.bzadservice.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

@Controller
@RequestMapping("/ad")
public class AdController {

    @Autowired
    private AdRepository adRepository;

    // 광고 등록 페이지
    @GetMapping("/write")
    public String showWritePage(Model model) {
        return "ad_write";
    }

    // 광고 상세 페이지
    @GetMapping("/detail/{id}")
    public String getAdDetail(@PathVariable Long id, Model model) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        // DTO 변환 및 포맷 처리
        AdDTO adDTO = convertToDTO(ad);

        model.addAttribute("ad", adDTO);
        return "ad_detail";
    }

    @GetMapping("/edit/{id}")
    public String getEditPage(@PathVariable Long id, Model model) {
        // 광고 데이터를 DB에서 조회
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        // 엔티티를 DTO로 변환
        AdDTO adDTO = convertToDTO(ad);

        // 모델에 DTO 추가
        model.addAttribute("ad", adDTO);
        return "ad_edit"; // 수정 페이지
    }
    @PostMapping("/edit/{id}")
    public String updateAd(
            @PathVariable Long id,
            @RequestParam("adPosition") String adPosition,
            @RequestParam("adStart") String adStart,
            @RequestParam("adEnd") String adEnd,
            @RequestParam("adTitle") String adTitle,
            @RequestParam("adUrl") String adUrl,
            @RequestParam(value = "adImage", required = false) MultipartFile adImage
    ) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다."));

        ad.setAdPosition(adPosition);
        ad.setAdStart(LocalDateTime.parse(adStart));
        ad.setAdEnd(LocalDateTime.parse(adEnd));
        ad.setAdTitle(adTitle);
        ad.setAdUrl(adUrl);

        if (adImage != null && !adImage.isEmpty()) {
            String imagePath = saveAdImage(adImage); // 이미지 저장 메서드 호출
            ad.setAdImage(imagePath);
        }

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
    // 광고 데이터를 DTO로 변환하는 메서드
    private AdDTO convertToDTO(Ad ad) {
        return AdDTO.builder()
                .id(ad.getId())
                .adPosition(ad.getAdPosition())
                .adStart(Optional.ofNullable(ad.getAdStart()).orElse(LocalDateTime.now()))
                .adEnd(Optional.ofNullable(ad.getAdEnd()).orElse(LocalDateTime.now()))
                .adTitle(ad.getAdTitle() != null ? ad.getAdTitle() : "광고명 없음")
                .adUrl(ad.getAdUrl())
                .adImage(ad.getAdImage() != null ? ad.getAdImage() : "/images/default.jpg")
                .status(ad.getStatus() != null ? ad.getStatus() : "미정")
                .hits(ad.getHits() != null ? ad.getHits() : 0)
                .build();
    }


    @Autowired
    private AdService adService;

    @GetMapping("/list")
    public String showListPage(

            @RequestParam(defaultValue = "0") int page, // 페이지 번호
            @RequestParam(defaultValue = "10") int size, // 한 페이지에 보여줄 데이터 수
            Model model) {
        Page<Ad> adsPage = adService.getAds(page, size);

        model.addAttribute("ads", adsPage.getContent()); // 현재 페이지의 데이터
        model.addAttribute("currentPage", page); // 현재 페이지 번호
        model.addAttribute("totalPages", adsPage.getTotalPages()); // 전체 페이지 수

        return "ad_list";
    }




}
