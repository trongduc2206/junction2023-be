package com.ducvt.diabeater.account.service;

import com.ducvt.diabeater.account.models.Chats;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ChatService {
    List<Chats> getByUserId(Long userId);
    void send(Long senderId, Long receiverId, String message );
}
