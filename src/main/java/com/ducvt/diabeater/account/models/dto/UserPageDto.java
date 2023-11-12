package com.ducvt.diabeater.account.models.dto;

import com.ducvt.diabeater.account.models.User;
import lombok.Data;

import java.util.List;

@Data
public class UserPageDto {
    private List<User> content;
    private long totalElements;
}
