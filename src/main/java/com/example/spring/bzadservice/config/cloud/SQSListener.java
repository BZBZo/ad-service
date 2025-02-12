package com.example.spring.bzadservice.config.cloud;

import com.example.spring.bzadservice.entity.nowAd;
import com.example.spring.bzadservice.repository.AdRepository;
import com.example.spring.bzadservice.repository.nowAdRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SQSListener {

    private final ObjectMapper objectMapper;
    private final nowAdRepository nowAdRepository;  // AdRepository 추가
    private final AdRepository AdRepository;  // AdRepository 추가
    private final AdRepository adRepository;

    @SqsListener("Ad")
    public void processMessage(Message message) {
        try {
            // 메시지 본문 출력
            String messageBody = message.body();
            System.out.println("Received message body: " + messageBody);

            System.out.println("Original messageBody: '" + messageBody + "'");
            messageBody = messageBody.trim(); // 앞뒤 공백 제거

            if (messageBody.startsWith("\"Ad Updated:")) {
                messageBody = messageBody.substring(1); // 첫 번째 쌍따옴표 제거
            }

            messageBody = messageBody.trim(); // 다시 공백 제거
            if (messageBody.startsWith("Ad Updated:")) {
                messageBody = messageBody.substring("Ad Updated:".length()).trim();
            }

            // 메시지에서 데이터를 파싱
            String[] parts = messageBody.split(",\\s*");  // ","와 공백을 기준으로 분리
            Map<String, String> messageData = new HashMap<>();
            for (String part : parts) {
                String[] keyValue = part.split("=", 2); // 최대 2개의 부분으로 나눠서 키와 값 분리
                if (keyValue.length == 2) {
                    messageData.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            // 다른 데이터 추출 (ID 제외)
            int id = Integer.parseInt(messageData.get("ID"));
            String title = messageData.get("Title");
            String start = messageData.get("Start");
            String end = messageData.get("End");
            String position = messageData.get("Position");
            String imageUrl = messageData.get("Image");
            String url = messageData.get("URL");

            System.out.println("id :: " + id);

            // LocalDateTime 변환
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime adStart = LocalDateTime.parse(start, formatter);
            LocalDateTime adEnd = LocalDateTime.parse(end, formatter);

            // nowAd 엔티티 객체 생성
            nowAd ad = nowAd.builder()
                    .adTitle(title)
                    .adPosition(position)
                    .adStart(adStart)
                    .adEnd(adEnd)
                    .adImage(imageUrl)
                    .adUrl(url)
                    .build();

            // DB에 저장
            long count = nowAdRepository.countByAdPosition(position);

            if (count != 0) {
                nowAdRepository.deleteByAdPosition(position);
            }

            nowAdRepository.save(ad);
            adRepository.updateHitsToZero((long) id);
            // 데이터 확인 (디버깅 용)
            System.out.println("Saved Ad: " + ad);

        } catch (Exception e) {
            System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
        }
    }
}