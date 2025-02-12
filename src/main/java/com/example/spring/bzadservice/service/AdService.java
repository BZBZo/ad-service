package com.example.spring.bzadservice.service;

import com.example.spring.bzadservice.config.cloud.CloudWatchScheduler;
import com.example.spring.bzadservice.config.s3.S3Uploader;
import com.example.spring.bzadservice.dto.AdDTO;
import com.example.spring.bzadservice.dto.AdEditRequestDTO;
import com.example.spring.bzadservice.dto.AdWriteRequestDTO;
import com.example.spring.bzadservice.dto.GetResolvesTimesRequestDTO;
import com.example.spring.bzadservice.entity.Ad;
import com.example.spring.bzadservice.entity.nowAd;
import com.example.spring.bzadservice.repository.AdRepository;
import com.example.spring.bzadservice.repository.nowAdRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final nowAdRepository nowAdRepository;
    private final ImgServiceImpl imgServiceImpl;
    private final S3Uploader s3Uploader;
    private final CloudWatchScheduler cloudWatchScheduler;

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
        return adRepository.findAllByHits(1,pageable);
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

    public void saveAd(String adPosition, LocalDateTime adStartDate, Long seller_id, LocalDateTime adEndDate,
                       String adTitle, String adUrl, String imgUrl) {


        System.out.println("seller_id :: " + seller_id);
        System.out.println("adPosition :: " + adPosition);

        Ad ad = Ad.builder()
                .adPosition(adPosition)
                .adStart(adStartDate)
                .sellerId(seller_id)
                .adEnd(adEndDate)
                .adTitle(adTitle)
                .adUrl(adUrl)
                .adImage(imgUrl) // S3에 저장된 이미지 url
                .hits(1)
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

    public List<GetResolvesTimesRequestDTO> getReservedTimes(String nowArea) {
        return adRepository.findReservedTimes(nowArea);
    }


    public ResponseEntity<Map<String, String>> updateStatus(
            Long id, String newStatus
    ) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        ad.setStatus(newStatus); // 상태 업데이트
        adRepository.save(ad);

        // 3. 업데이트된 광고 데이터 다시 가져오기
        Ad updatedAd = adRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("광고를 찾을 수 없습니다. ID: " + id));

        // 4. Ad를 DTO로 변환
        AdDTO adDTO = AdDTO.builder()
                .id(updatedAd.getId())
                .adPosition(updatedAd.getAdPosition())
                .adStart(updatedAd.getAdStart())
                .adEnd(updatedAd.getAdEnd())
                .adTitle(updatedAd.getAdTitle())
                .adUrl(updatedAd.getAdUrl())
                .adImage(updatedAd.getAdImage())
                .status(updatedAd.getStatus())
                .hits(updatedAd.getHits())
                .build();

        System.out.println("adDTO ::: " + adDTO);

        if ("승인".equalsIgnoreCase(adDTO.getStatus())) {
            // CloudWatch에 로그 기록
            logToCloudWatch(adDTO);
            // CloudWatchScheduler를 통해 SQS로 메시지 전송
            String message = String.format("Ad Updated: ID=%d, Title=%s, Status=%s, Start=%s, End=%s, Position=%s, Image=%s, URL=%s",
                    adDTO.getId(), adDTO.getAdTitle(), adDTO.getStatus(), adDTO.getAdStart(), adDTO.getAdEnd(),
                    adDTO.getAdPosition(), adDTO.getAdImage(), adDTO.getAdUrl());

            // SQS로 메시지 전송
            String uniqueRuleName = "adUpdateRule-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            cloudWatchScheduler.scheduleMessageToSQS(uniqueRuleName, "arn:aws:sqs:ap-northeast-2:448049828702:Ad", adDTO.getAdStart(), message);
        }

        return ResponseEntity.ok(Map.of("message", "상태가 성공적으로 변경되었습니다."));
    }

    private void logToCloudWatch(AdDTO adDTO) {
        String logGroupName = "bzAd";
        String logStreamName = "bzAdStream";

        try (CloudWatchLogsClient cloudWatchLogsClient = CloudWatchLogsClient.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전
                .build()) {

            // 로그 그룹 확인 및 생성
            if (!logGroupExists(cloudWatchLogsClient, logGroupName)) {
                cloudWatchLogsClient.createLogGroup(r -> r.logGroupName(logGroupName));
                System.out.println("Created log group: " + logGroupName);
            }

            // 로그 스트림 확인 및 생성
            if (!logStreamExists(cloudWatchLogsClient, logGroupName, logStreamName)) {
                cloudWatchLogsClient.createLogStream(r -> r.logGroupName(logGroupName).logStreamName(logStreamName));
                System.out.println("Created log stream: " + logStreamName);
            }

            // JSON 형식의 메시지 생성
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", adDTO.getId());
            messageMap.put("title", adDTO.getAdTitle());
            messageMap.put("status", adDTO.getStatus());
            messageMap.put("start", adDTO.getAdStart().toString());
            messageMap.put("end", adDTO.getAdEnd().toString());
            messageMap.put("position", adDTO.getAdPosition());
            messageMap.put("image", adDTO.getAdImage());
            messageMap.put("url", adDTO.getAdUrl());

            // ObjectMapper를 사용하여 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(messageMap);

            // 로그 이벤트 생성 및 전송
            InputLogEvent logEvent = InputLogEvent.builder()
                    .message(jsonMessage)  // JSON 형식 메시지
                    .timestamp(System.currentTimeMillis())
                    .build();

            PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .logStreamName(logStreamName)
                    .logEvents(logEvent)
                    .build();

            cloudWatchLogsClient.putLogEvents(putLogEventsRequest);
            System.out.println("Logged to CloudWatch: " + jsonMessage);

        } catch (Exception e) {
            System.err.println("Failed to log to CloudWatch: " + e.getMessage());
        }
    }

    private boolean logGroupExists(CloudWatchLogsClient client, String logGroupName) {
        return client.describeLogGroups(r -> r.logGroupNamePrefix(logGroupName))
                .logGroups()
                .stream()
                .anyMatch(logGroup -> logGroup.logGroupName().equals(logGroupName));
    }

    private boolean logStreamExists(CloudWatchLogsClient client, String logGroupName, String logStreamName) {
        return client.describeLogStreams(r -> r.logGroupName(logGroupName).logStreamNamePrefix(logStreamName))
                .logStreams()
                .stream()
                .anyMatch(logStream -> logStream.logStreamName().equals(logStreamName));
    }


    public List<AdDTO> getAds() {
        // now_ad 테이블에서 모든 데이터 조회
        List<nowAd> nowAds = nowAdRepository.findAll();

        // nowAd 객체들을 AdDTO 객체로 변환
        List<AdDTO> adDTOs = nowAds.stream()
                .map(nowAd -> AdDTO.builder()
                        .id(nowAd.getId())
                        .adPosition(nowAd.getAdPosition())
                        .adStart(nowAd.getAdStart())
                        .adEnd(nowAd.getAdEnd())
                        .adTitle(nowAd.getAdTitle())
                        .adUrl(nowAd.getAdUrl())
                        .adImage(nowAd.getAdImage())
                        .build())
                .collect(Collectors.toList());

        return adDTOs;
    }


}
