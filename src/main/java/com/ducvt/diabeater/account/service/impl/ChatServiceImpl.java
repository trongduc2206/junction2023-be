package com.ducvt.diabeater.account.service.impl;

import com.ducvt.diabeater.account.models.Chats;
import com.ducvt.diabeater.account.repository.ChatsRepository;
import com.ducvt.diabeater.account.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    ChatsRepository chatsRepository;

    @Override
    public List<Chats> getByUserId(Long userId) {
        Optional<List<Chats>> optionalChats = chatsRepository.findBySenderOrReceiverOrderByCreateTimeAsc(userId, userId);
        if(optionalChats.isPresent()) {
            return optionalChats.get();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void send(Long senderId, Long receiverId, String message) {
        Chats chats = new Chats();
        chats.setCreateTime(new Date());
        chats.setSender(senderId);
        chats.setReceiver(receiverId);
        chats.setMessage(message);
        chatsRepository.save(chats);
    }
}
