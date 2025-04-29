package com.example.Secondhand.controller;

import com.example.Secondhand.service.AIChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AIChatController {

    @Autowired
    private AIChatService aiChatService;

    @PostMapping("/send")
    public Map<String, String> sendMessage(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String response = aiChatService.getAIResponse(message);
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("response", response);
        return responseMap;
    }
} 