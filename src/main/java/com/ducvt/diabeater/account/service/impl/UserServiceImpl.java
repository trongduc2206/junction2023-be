package com.ducvt.diabeater.account.service.impl;

import com.ducvt.diabeater.account.models.ERole;
import com.ducvt.diabeater.account.models.Role;
import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.models.dto.UserPageDto;
import com.ducvt.diabeater.account.repository.UserRepository;
import com.ducvt.diabeater.account.service.UserService;
import com.ducvt.diabeater.fw.constant.MessageEnum;
import com.ducvt.diabeater.fw.exceptions.BusinessLogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Override
    public UserPageDto getAll(int page, int offset) {
        Pageable pageable = PageRequest.of(page, offset, Sort.by(Sort.Direction.ASC,"createTime"));
        Page<User> userPage = userRepository.findAllBy(pageable);
        List<User> userListFiltered = new ArrayList<>();
        for(User user: userPage.getContent()) {
            Boolean isUser = false;
            for(Role roleCheck : user.getRoles()) {
                if(roleCheck.getName().equals(ERole.ROLE_PATIENT)) {
                    isUser = true;
                }
                if(isUser) {
                    userListFiltered.add(user);
                }
            }
        }
        UserPageDto userPageDto = new UserPageDto();
        userPageDto.setContent(userListFiltered);
        userPageDto.setTotalElements(userPage.getTotalElements());

//        List<User> userList = userPage.getContent();
//        List<User> userListDecodePassword
//        return userPage;
        return userPageDto;
    }

    @Override
    public UserPageDto searchByUsername(String username, int page, int offset) {
        Pageable pageable = PageRequest.of(page, offset, Sort.by(Sort.Direction.ASC, "createTime"));
        Page<User> userPage = userRepository.findByUsernameContains(username, pageable);
        List<User> userListFiltered = new ArrayList<>();
        for(User user: userPage.getContent()) {
            Boolean isUser = false;
            for(Role roleCheck : user.getRoles()) {
                if(roleCheck.getName().equals(ERole.ROLE_PATIENT)) {
                    isUser = true;
                }
                if(isUser) {
                    userListFiltered.add(user);
                }
            }
        }
        UserPageDto userPageDto = new UserPageDto();
        userPageDto.setContent(userListFiltered);
        userPageDto.setTotalElements(userPage.getTotalElements());

//        return userPage;
        return userPageDto;
    }

    @Override
    public void update(User user) {
        if(user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BusinessLogicException(MessageEnum.EMPTY_USERNAME.getMessage());
        }
        User oldUser = userRepository.findById(user.getId()).get();
        if(!user.getUsername().equals(oldUser.getUsername())) {
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new BusinessLogicException(MessageEnum.DUPLICATE_USERNAME.getMessage());
            }
            oldUser.setUsername(user.getUsername());
        }
        if(user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        if(user.getStatus() != null) {
            oldUser.setStatus(user.getStatus());
        }
        oldUser.setUpdateTime(new Date());

        userRepository.save(oldUser);
    }

    @Override
    public User getById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
        }
    }


}
