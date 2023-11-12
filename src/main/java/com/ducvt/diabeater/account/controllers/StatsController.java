package com.ducvt.diabeater.account.controllers;

import com.ducvt.diabeater.account.payload.request.StatsRequest;
import com.ducvt.diabeater.account.service.StatsService;
import com.ducvt.diabeater.fw.utils.ResponseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/stats")
public class StatsController {
    @Autowired
    StatsService statsService;
    @GetMapping
    public ResponseEntity getByUserId(@RequestParam(value = "userId") Long userId, @RequestParam(value = "range") String range) {
        return ResponseFactory.success(statsService.getByUserId(userId, range));
    }
    @PostMapping
    public ResponseEntity create(@RequestBody StatsRequest statsRequest) {
        statsService.create(statsRequest);
        return ResponseFactory.success();
    }

    @GetMapping(value = "/badStats")
    public ResponseEntity getBadStatsUsers() {
        return ResponseFactory.success(statsService.getUserWithBadStat());
    }

    @GetMapping(value = "/frontPage")
    public ResponseEntity getStatsFrontPage(@RequestParam(value = "userId") Long userId) {
        return ResponseFactory.success(statsService.getStatsFrontPage(userId));
    }
}
