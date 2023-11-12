package com.ducvt.diabeater.account.service;

import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.models.dto.UserPageDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
//    Page<User> getAll(int page, int offset);
    UserPageDto getAll(int page, int offset);

    UserPageDto searchByUsername(String username, int page, int offset);

    void update(User user);

    User getById(Long userId);
}
