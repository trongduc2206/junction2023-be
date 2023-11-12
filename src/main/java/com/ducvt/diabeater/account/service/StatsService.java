package com.ducvt.diabeater.account.service;

import com.ducvt.diabeater.account.models.Stats;
import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.payload.request.StatsRequest;
import com.ducvt.diabeater.account.payload.response.StatsFrontPage;
import com.ducvt.diabeater.account.payload.response.StatsResponse;
import com.ducvt.diabeater.account.payload.response.UserBadStatsResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface StatsService {
    void create(StatsRequest statsRequest);

    StatsResponse getByUserId(Long userId, String range);

    List<UserBadStatsResponse> getUserWithBadStat();

    List<StatsFrontPage> getStatsFrontPage(Long userId);
}
