package com.example.spring.bzadservice.config.cloud;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.PutRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Slf4j
public class CloudWatchScheduler {

    private final CloudWatchEventsClient cloudWatchEventsClient;

    public CloudWatchScheduler(CloudWatchEventsClient cloudWatchEventsClient) {
        this.cloudWatchEventsClient = cloudWatchEventsClient;
    }

    public void scheduleMessageToSQS(String ruleName, String queueArn, LocalDateTime startDateTime, String messageMap) {
        // Start 시간에 해당하는 Cron 표현식 생성
        String cronExpression = generateCronExpression(startDateTime);

        // CloudWatch 규칙 생성
        PutRuleRequest ruleRequest = PutRuleRequest.builder()
                .name(ruleName)
                .scheduleExpression(cronExpression)  // 동적으로 생성된 Cron 표현식
                .state("ENABLED")
                .build();

        log.info("CloudWatch rule created: {}", ruleName);
        cloudWatchEventsClient.putRule(ruleRequest);

        // 메시지를 JSON 형식으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = "";
        try {
            jsonMessage = objectMapper.writeValueAsString(messageMap);
            log.info("Converted message to JSON: {}", jsonMessage);  // 성공적으로 변환된 메시지 로깅
        } catch (JsonProcessingException e) {
            log.error("Error converting message to JSON: {}", e.getMessage(), e);  // 오류 로깅
            return;  // JSON 변환에 실패하면 메서드 종료
        }

        // SQS에 메시지를 보내는 트리거 추가
        String finalJsonMessage = jsonMessage;
        PutTargetsRequest putTargetsRequest = PutTargetsRequest.builder()
                .rule(ruleName)
                .targets(target -> target
                        .id("1")
                        .arn(queueArn)  // SQS 큐 ARN
                        .input(finalJsonMessage)  // JSON 형식으로 변환된 메시지
                )
                .build();

        cloudWatchEventsClient.putTargets(putTargetsRequest);
        log.info("Scheduled message to SQS with rule: {}", ruleName);
    }

    private String generateCronExpression(LocalDateTime startDateTime) {
        // `Start` 시간을 서울 시간에서 UTC로 변환 후 Cron 표현식 생성
        ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");
        ZoneId utcZoneId = ZoneId.of("UTC");

        // 서울 시간 -> UTC로 변환
        LocalDateTime utcDateTime = startDateTime.atZone(seoulZoneId).withZoneSameInstant(utcZoneId).toLocalDateTime();

        // Cron 표현식 생성 (UTC 기준)
        String cronExpression = String.format(
                "cron(%d %d %d %d ? %d)",  // cron(분 시 일 월 ? 년)
                utcDateTime.getMinute(),   // 분
                utcDateTime.getHour(),     // 시
                utcDateTime.getDayOfMonth(), // 일
                utcDateTime.getMonthValue(), // 월
                utcDateTime.getYear()       // 년
        );
        log.info("Generated cron expression: {}", cronExpression);

        return cronExpression;
    }
}