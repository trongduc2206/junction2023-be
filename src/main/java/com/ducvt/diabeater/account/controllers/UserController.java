package com.ducvt.diabeater.account.controllers;

import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.models.dto.UserPageDto;
import com.ducvt.diabeater.account.service.UserService;
import com.ducvt.diabeater.fw.utils.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity getAll(@RequestParam int page, @RequestParam int offset) {
        UserPageDto userPage = userService.getAll(page, offset);
        return ResponseFactory.success(userPage);
    }

    @GetMapping(value = "/search")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity searchByUserName(@RequestParam int page, @RequestParam int offset, @RequestParam String username) {
        UserPageDto userPage = userService.searchByUsername(username, page, offset);
        return ResponseFactory.success(userPage);
    }

    @PutMapping(value = "/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity update(@RequestBody User user) {
        userService.update(user);
        return ResponseFactory.success();
    }

    @GetMapping(value = "/info")
    public ResponseEntity getById(@RequestParam Long userId) {
        return ResponseFactory.success(userService.getById(userId));
    }



}
