package com.ducvt.diabeater.account.service.impl;

import com.ducvt.diabeater.account.models.ERole;
import com.ducvt.diabeater.account.models.Role;
import com.ducvt.diabeater.account.models.Stats;
import com.ducvt.diabeater.account.models.User;
import com.ducvt.diabeater.account.payload.request.StatsRequest;
import com.ducvt.diabeater.account.payload.response.StatsFrontPage;
import com.ducvt.diabeater.account.payload.response.StatsResponse;
import com.ducvt.diabeater.account.payload.response.UserBadStatsResponse;
import com.ducvt.diabeater.account.repository.RoleRepository;
import com.ducvt.diabeater.account.repository.StatsRepository;
import com.ducvt.diabeater.account.repository.UserRepository;
import com.ducvt.diabeater.account.service.StatsService;
import com.ducvt.diabeater.fw.constant.MessageEnum;
import com.ducvt.diabeater.fw.exceptions.BusinessLogicException;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    StatsRepository statsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void create(StatsRequest statsRequest) {
        Optional<User> user = userRepository.findById(statsRequest.getUserId());
        if(user.isPresent()) {
            Stats statsObject = new Stats();
            statsObject.setUserId(statsRequest.getUserId());
            statsObject.setCreateTime(new Date());
            if(statsRequest.getGlucoseLevel() != null && statsRequest.getA1cLevel() != null) {
                statsObject.setGlucoseLevel(statsRequest.getGlucoseLevel());
                statsObject.setA1cLevel(statsRequest.getA1cLevel());
                double eag = 28.7*statsObject.getA1cLevel() - 46.7;
                statsObject.setEag((float) eag);
                double gmi = 3.31 + 0.02392*statsRequest.getGlucoseLevel();
                statsObject.setGmi((float) gmi);
                double cv = 100/statsRequest.getGlucoseLevel();
                statsObject.setCv((float) cv);
            } else {
                throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
            }
            statsObject.setHeight(statsRequest.getHeight());
            statsObject.setWeight(statsRequest.getWeight());
            double bmi = statsObject.getWeight()/(Math.pow(statsObject.getHeight()/100,2));
            statsObject.setBmi((float) bmi);
            statsRepository.save(statsObject);
        } else {
            throw new BusinessLogicException(MessageEnum.NOT_FOUND_USER.getMessage());
        }
    }

    @Override
    public StatsResponse getByUserId(Long userId, String range) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if(!optionalUser.isPresent()) {
            throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
        } else {
            User user = optionalUser.get();
            Set<Role> roles = user.getRoles();
            if(!roles.contains(roleRepository.findByName(ERole.ROLE_PATIENT).get())) {
                throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
            }
        }
        Date end = new Date();
        Date start = null;
        if(range.equals("week")) {
            start = minusDate(end, 14);
        } else if(range.equals("month")) {
            start = minusDate(end, 30);
        } else if(range.equals("year")) {
            start = minusDate(end, 365);
        } else {
            throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
        }
//        Optional<List<Stats>> optionalStatsList = statsRepository.findByUserId(userId);
        StatsResponse statsResponse = new StatsResponse();
        Optional<List<Stats>> optionalStatsListDesc = statsRepository.findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(userId, start, end);
        if(!optionalStatsListDesc.isPresent()) {
            throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
        }
        statsResponse.setStats(optionalStatsListDesc.get());
        Optional<List<Stats>> optionalStatsListAsc = statsRepository.findByUserIdAndCreateTimeBetweenOrderByCreateTimeAsc(userId, start, end);


        Map<Date, Float> glucoseLevel = new LinkedHashMap<>();
        Map<Date, Float> ac1Level = new LinkedHashMap<>();
        Map<Date, Float> eag = new LinkedHashMap<>();
        Map<Date, Float> gmi = new LinkedHashMap<>();
        Map<Date, Float> cv = new LinkedHashMap<>();
        for(int i=0; i<optionalStatsListAsc.get().size(); i++) {
            List<Stats> statsList = optionalStatsListAsc.get();
            Stats stats = statsList.get(i);
            Date time = stats.getCreateTime();
            glucoseLevel.put(time, stats.getGlucoseLevel());
            ac1Level.put(time, stats.getA1cLevel());
            eag.put(time, stats.getEag());
            gmi.put(time, stats.getGmi());
            cv.put(time, stats.getCv());
        }
//        for(Stats stats : optionalStatsListAsc.get()) {
//            Date time = stats.getCreateTime();
//            glucoseLevel.put(time, stats.getGlucoseLevel());
//            ac1Level.put(time, stats.getA1cLevel());
//            eag.put(time, stats.getEag());
//            gmi.put(time, stats.getGmi());
//            cv.put(time, stats.getCv());
//        }

        Map<String, String> dynamicAnalysis = new HashMap<>();
        dynamicAnalysis(dynamicAnalysis, glucoseLevel, "glucoseLEvel");
        dynamicAnalysis(dynamicAnalysis, ac1Level, "a1cLevel");
        dynamicAnalysis(dynamicAnalysis, eag, "eag");
        dynamicAnalysis(dynamicAnalysis, gmi, "gmi");
        dynamicAnalysis(dynamicAnalysis, cv, "cv");
        statsResponse.setDynamicAnalysis(dynamicAnalysis);

        statsResponse.setStaticAnalysis(staticAnalyze(optionalStatsListDesc.get().get(0)));
        return statsResponse;
    }

    @Override
    public List<UserBadStatsResponse> getUserWithBadStat() {
        List<UserBadStatsResponse> userBadStatsResponses = new ArrayList<>();
        List<User> users = userRepository.findAll();
        for(User user : users) {
            Set<Role> roles = user.getRoles();
            if(roles.contains(roleRepository.findByName(ERole.ROLE_PATIENT).get())) {
                int highCnt = 0;
                List<String> details = new ArrayList<>();
                StatsResponse statsResponse = getByUserId(user.getId(), "week");
                Map<String, String> staticAnalysis = statsResponse.getStaticAnalysis();
                for (Map.Entry<String, String> entry : staticAnalysis.entrySet()) {
                    if (entry.getValue().equals("high")) {
                        highCnt++;
                        if(entry.getKey().equals("glucoseLevel")) {
                            details.add("Glucose Level is high");
                        } else if(entry.getKey().equals("a1cLevel")) {
                            details.add("A1c level is high");
                        } else {
                            details.add(entry.getKey().toUpperCase() + " is high");
                        }
                    }
                }
                Map<String, String> dynamicAnalysis = statsResponse.getDynamicAnalysis();
                for (Map.Entry<String, String> entry : dynamicAnalysis.entrySet()) {
                    if (entry.getValue().equals("upward")) {
                        highCnt++;
//                        details.add(entry.getKey().toUpperCase() + " is increasing");
                    }
                }
                UserBadStatsResponse userBadStatsResponse = new UserBadStatsResponse();
                userBadStatsResponse.setUserId(user.getId());
                userBadStatsResponse.setFullName(user.getFullName());
                userBadStatsResponse.setGender(user.getGender());
                userBadStatsResponse.setAge(user.getAge());
                userBadStatsResponse.setDetails(details );
                userBadStatsResponse.setType(user.getDiseaseType());
                if (highCnt >= 5) {
                    userBadStatsResponse.setStatus("Danger");
                    userBadStatsResponses.add(userBadStatsResponse);
                } else if (highCnt >= 3) {
                    userBadStatsResponse.setStatus("High");
                    userBadStatsResponses.add(userBadStatsResponse);
                } else if (highCnt >= 1) {
                    userBadStatsResponse.setStatus("Warning");
                    userBadStatsResponses.add(userBadStatsResponse);
                }
            }
        }
        return userBadStatsResponses;
    }

    @Override
    public List<StatsFrontPage> getStatsFrontPage(Long userId) {
        StatsFrontPage glucose = new StatsFrontPage();
        glucose.setMetricName("Glucose Level");
        StatsFrontPage a1c = new StatsFrontPage();
        a1c.setMetricName("A1C Level");
        StatsFrontPage eag = new StatsFrontPage();
        eag.setMetricName("EAG");
        StatsFrontPage gmi = new StatsFrontPage();
        gmi.setMetricName("GMI");
        StatsFrontPage cv = new StatsFrontPage();
        cv.setMetricName("CV");
        StatsFrontPage bmi = new StatsFrontPage();
        bmi.setMetricName("BMI");

        float sumGlucose = 0;
        float sumA1c = 0;
        float sumEag = 0;
        float sumGmi = 0;
        float sumCv = 0;
        float sumBmi = 0;

        Date end = new Date();
        Date start = minusDate(end, 7);
        Optional<List<Stats>> optionalStatsListDesc = statsRepository.findByUserIdAndCreateTimeBetweenOrderByCreateTimeDesc(userId, start, end);
        if(optionalStatsListDesc.isPresent()) {
            List<Stats> statsList = optionalStatsListDesc.get();
            glucose.setLatest(round(statsList.get(0).getGlucoseLevel()));
            a1c.setLatest(round(statsList.get(0).getA1cLevel()));
            eag.setLatest(round(statsList.get(0).getEag()));
            gmi.setLatest(round(statsList.get(0).getGmi()));
            cv.setLatest(round(statsList.get(0).getCv()));
            bmi.setLatest(round(statsList.get(0).getBmi()));
            for(Stats stats : statsList) {
                sumGlucose+=stats.getGlucoseLevel();
                sumA1c+=stats.getA1cLevel();
                sumEag+=stats.getEag();
                sumGmi+=stats.getGmi();
                sumCv+=stats.getCv();
                sumBmi+=stats.getBmi();
            }
            glucose.setAverage(round(sumGlucose/statsList.size()));
            a1c.setAverage(round(sumA1c/statsList.size()));
            eag.setAverage(round(sumEag/statsList.size()));
            gmi.setAverage(round(sumGmi/statsList.size()));
            cv.setAverage(round(sumCv/statsList.size()));
            bmi.setAverage(round(sumBmi/statsList.size()));

            if(statsList.get(0).getGlucoseLevel() > 180) {
                glucose.setStaticAnalysis("high");
                gmi.setStaticAnalysis("high");
                cv.setStaticAnalysis("high");
            } else if(statsList.get(0).getGlucoseLevel() >= 70 && statsList.get(0).getGlucoseLevel() <= 180) {
                glucose.setStaticAnalysis("normal");
                gmi.setStaticAnalysis("normal");
                cv.setStaticAnalysis("normal");
            } else {
                glucose.setStaticAnalysis("low");
                gmi.setStaticAnalysis("low");
                cv.setStaticAnalysis("high");
            }

            if(statsList.get(0).getA1cLevel() > 6.5) {
                a1c.setStaticAnalysis("high");
                eag.setStaticAnalysis("high");
            } else if(statsList.get(0).getA1cLevel() >= 5.7 && statsList.get(0).getA1cLevel() <=6.5) {
                a1c.setStaticAnalysis("normal");
                eag.setStaticAnalysis("normal");
            } else {
                a1c.setStaticAnalysis("low");
                eag.setStaticAnalysis("low");
            }

            if(statsList.get(0).getBmi() > 30) {
                bmi.setStaticAnalysis("too high");
            } else if(statsList.get(0).getBmi() <= 30 && statsList.get(0).getBmi() >= 25) {
                bmi.setStaticAnalysis("high");
            } else if(statsList.get(0).getBmi() < 25 && statsList.get(0).getBmi() >=18.5) {
                bmi.setStaticAnalysis("normal");
            } else {
                bmi.setStaticAnalysis("low");
            }

            Optional<List<Stats>> optionalStatsListAsc = statsRepository.findByUserIdAndCreateTimeBetweenOrderByCreateTimeAsc(userId, start, end);


            Map<Date, Float> glucoseLevelMap = new LinkedHashMap<>();
            Map<Date, Float> ac1LevelMap = new LinkedHashMap<>();
            Map<Date, Float> eagMap = new LinkedHashMap<>();
            Map<Date, Float> gmiMap = new LinkedHashMap<>();
            Map<Date, Float> cvMap = new LinkedHashMap<>();
            Map<Date, Float> bmiMap = new LinkedHashMap<>();
            for(int i=0; i<optionalStatsListAsc.get().size(); i++) {
                List<Stats> statsListAsc = optionalStatsListAsc.get();
                Stats stats = statsListAsc.get(i);
                Date time = stats.getCreateTime();
                glucoseLevelMap.put(time, stats.getGlucoseLevel());
                ac1LevelMap.put(time, stats.getA1cLevel());
                eagMap.put(time, stats.getEag());
                gmiMap.put(time, stats.getGmi());
                cvMap.put(time, stats.getCv());
                bmiMap.put(time, stats.getBmi());
            }
            if(trendDetect(glucoseLevelMap) > 0) {
                glucose.setDynamicAnalysis("upward");
            } else if(trendDetect(glucoseLevelMap )< 0) {
                glucose.setDynamicAnalysis("downward");
            } else {
                glucose.setDynamicAnalysis("stable");
            }

            if(trendDetect(ac1LevelMap) > 0) {
                a1c.setDynamicAnalysis("upward");
            } else if(trendDetect(ac1LevelMap )< 0) {
                a1c.setDynamicAnalysis("downward");
            } else {
                a1c.setDynamicAnalysis("stable");
            }

            if(trendDetect(eagMap) > 0) {
                eag.setDynamicAnalysis("upward");
            } else if(trendDetect(eagMap )< 0) {
                eag.setDynamicAnalysis("downward");
            } else {
                eag.setDynamicAnalysis("stable");
            }

            if(trendDetect(gmiMap) > 0) {
                gmi.setDynamicAnalysis("upward");
            } else if(trendDetect(gmiMap )< 0) {
                gmi.setDynamicAnalysis("downward");
            } else {
                gmi.setDynamicAnalysis("stable");
            }

            if(trendDetect(cvMap) > 0) {
                cv.setDynamicAnalysis("upward");
            } else if(trendDetect(eagMap )< 0) {
                cv.setDynamicAnalysis("downward");
            } else {
                cv.setDynamicAnalysis("stable");
            }

            if(trendDetect(bmiMap) > 0) {
                bmi.setDynamicAnalysis("upward");
            } else if(trendDetect(bmiMap )< 0) {
                bmi.setDynamicAnalysis("downward");
            } else {
                bmi.setDynamicAnalysis("stable");
            }

            glucose.setChange(round(100*(glucose.getLatest() - glucose.getAverage())/glucose.getAverage()));
            a1c.setChange(round(100*(a1c.getLatest() - a1c.getAverage())/a1c.getAverage()));
            eag.setChange(round(100*(eag.getLatest() - eag.getAverage())/eag.getAverage()));
            gmi.setChange(round(100*(gmi.getLatest() - gmi.getAverage())/gmi.getAverage()));
            cv.setChange(round(100*(cv.getLatest() - cv.getAverage())/cv.getAverage()));
            bmi.setChange(round(100*(bmi.getLatest() - bmi.getAverage())/bmi.getAverage()));

            glucose.setUnit("mg/dL");
            glucose.setDescription("Glucose level refers to the concentration of glucose, a type of sugar, in the blood.  The measurement of glucose levels is often used in medical contexts to assess and monitor an individual's health, particularly in the management of conditions such as diabetes.");
            a1c.setUnit("mg/dL");
            a1c.setDescription("A1C, or glycated hemoglobin, is a blood test that measures the average blood sugar level over the past two to three months. It provides a longer-term perspective on blood glucose control compared to daily glucose monitoring, which gives a snapshot of current levels");
            eag.setUnit("mg/dL");
            eag.setDescription("'Estimated average glucose (eAG) is an estimated average of your blood sugar (glucose) levels over a period of 2 to 3 months.\\nKnowing your eAG helps you know your blood sugar levels over time. It shows how well you are controlling your diabetes.");
            gmi.setUnit("%");
            gmi.setDescription("The glucose management indicator (GMI) is a metric that helps people with diabetes understand the current state of their glucose management.");
            cv.setUnit("%");
            cv.setDescription("Coefficient of Variation is a measure of glucose variability.");
            bmi.setUnit("kg/m2");
            bmi.setDescription("BMI is a measure used to determine whether a person has a healthy body weight for their height");


            List<StatsFrontPage> response = new ArrayList<>();
            response.add(glucose);
            response.add(a1c);
            response.add(eag);
            response.add(gmi);
            response.add(cv);
            response.add(bmi);
            return response;
        } else {
            throw new BusinessLogicException(MessageEnum.BAD_REQUEST.getMessage());
        }
    }


    private Map<String, String> staticAnalyze(Stats stats) {
        Map<String, String> analysis = new HashMap<>();
        if(stats.getGlucoseLevel() > 180) {
            analysis.put("glucoseLevel", "high");
            analysis.put("gmi", "high");
            analysis.put("cv", "low");
        } else if(stats.getGlucoseLevel() >= 70 && stats.getGlucoseLevel() <= 180) {
            analysis.put("glucoseLevel", "normal");
            analysis.put("gmi", "normal");
            analysis.put("cv", "normal");
        } else {
            analysis.put("glucoseLevel", "low");
            analysis.put("gmi", "low");
            analysis.put("cv", "high");
        }

        if(stats.getA1cLevel() > 6.5) {
            analysis.put("a1cLevel","high");
            analysis.put("eag","high");
        } else if(stats.getA1cLevel() >= 5.7 && stats.getA1cLevel() <=6.5) {
            analysis.put("a1cLevel", "normal");
            analysis.put("eag", "normal");
        } else {
            analysis.put("a1cLevel", "low");
            analysis.put("eag", "low");
        }

        if(stats.getBmi() > 30) {
            analysis.put("bmi", "too high");
        } else if(stats.getBmi() <= 30 && stats.getBmi() >= 25) {
            analysis.put("bmi", "high");
        } else if(stats.getBmi() < 25 && stats.getBmi() >=18.5) {
            analysis.put("bmi", "normal");
        } else {
            analysis.put("bmi", "low");
        }
        return analysis;
    }

    private Date minusDate(Date end, Integer numOfDays) {
        Instant instant = end.toInstant();

        // Convert Instant to LocalDate
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        // Subtract 30 days from the specified date
        LocalDate thirtyDaysAgo = localDate.minusDays(numOfDays);

        // Convert LocalDate back to Date
        Date resultDate = Date.from(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());

        return resultDate;
    }


    private double trendDetect(Map<Date, Float> stats) {

        SimpleRegression simpleRegression = new SimpleRegression();


        WeightedObservedPoints points = new WeightedObservedPoints();
        for(Map.Entry<Date, Float> entry: stats.entrySet()) {
            simpleRegression.addData(entry.getKey().getTime(), entry.getValue());
        }
        return simpleRegression.getSlope();
    }

    private void dynamicAnalysis(Map<String, String> analysis, Map<Date, Float> input, String key) {
        if(trendDetect(input) > 0) {
            analysis.put(key, "upward");
        } else if(trendDetect(input) < 0) {
            analysis.put(key, "downward");
        } else {
            analysis.put(key, "stable");
        }
    }

    private Float round(Float old) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String roundedValue = decimalFormat.format(old);
        float roundedFloatValue = Float.parseFloat(roundedValue);
        return roundedFloatValue;
    }


}
