package com.example.Secondhand.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.client.RestClientException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AIChatService {
    private static final Logger logger = LoggerFactory.getLogger(AIChatService.class);
    private static final String AI_SERVICE_URL = "http://localhost:5000/chat";
    private final RestTemplate restTemplate = new RestTemplate();

    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public String getAIResponse(String message) {
        try {
            logger.info("开始发送消息到AI服务: {}", message);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("message", message);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
            logger.info("发送请求到AI服务: {}", request);
            
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(AI_SERVICE_URL, request, Map.class);
            logger.info("收到AI服务响应: status={}, body={}", responseEntity.getStatusCode(), responseEntity.getBody());

            Map<String, String> responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("response")) {
                logger.info("成功获取AI响应");
                return responseBody.get("response");
            } else if (responseBody != null && responseBody.containsKey("error")) {
                logger.error("AI服务返回错误: {}", responseBody.get("error"));
                return "AI Service Error: " + responseBody.get("error");
            }
            
            logger.warn("AI服务返回意外的响应格式: {}", responseBody);
            return "Sorry, I couldn't process your message.";
        } catch (RestClientException e) {
            logger.error("连接AI服务失败: {}", e.getMessage(), e);
            return "AI Service is not available. Please try again later.";
        } catch (Exception e) {
            logger.error("AI聊天服务发生意外错误: {}", e.getMessage(), e);
            return "Sorry, there was an error processing your message: " + e.getMessage();
        }
    }
} 