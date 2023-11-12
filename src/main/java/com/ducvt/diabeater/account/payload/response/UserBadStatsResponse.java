package com.ducvt.diabeater.account.payload.response;

import lombok.Data;

import java.util.List;

@Data
public class UserBadStatsResponse {
    private Long userId;
    private String fullName;
    private String gender;
    private Integer age;
    private String status;
    private String type;
    private List<String> details;
}
