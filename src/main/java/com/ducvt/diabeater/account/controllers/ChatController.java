package com.ducvt.diabeater.account.controllers;

import com.ducvt.diabeater.account.payload.request.SendMessageRequest;
import com.ducvt.diabeater.account.service.ChatService;
import com.ducvt.diabeater.fw.utils.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    ChatService chatService;

    @PostMapping
    public ResponseEntity send(@RequestBody SendMessageRequest sendMessageRequest) {
        chatService.send(sendMessageRequest.getSenderId(), sendMessageRequest.getReceiverId(), sendMessageRequest.getMessage());
        return ResponseFactory.success();
    }

    @GetMapping
    public ResponseEntity get(@RequestParam Long userId) {
        return ResponseFactory.success(chatService.getByUserId(userId));
    }
}
