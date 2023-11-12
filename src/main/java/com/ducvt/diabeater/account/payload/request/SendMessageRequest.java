package com.ducvt.diabeater.account.payload.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long senderId;
    private Long receiverId;
    private String message;
}
