package com.LetucOJ.practice.repos;

import com.LetucOJ.common.cache.Redis;
import com.LetucOJ.common.log.LogLevel;
import com.LetucOJ.common.log.Logger;
import com.LetucOJ.common.log.Type;
import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.practice.model.DTO.SubmitRecordDTO;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class RedisSubmissionConsumer {

    @Resource
    private MybatisRepos mybatisRepos;

    @Resource
    private MinioRepos minioRepos;

    // 内部类：月份数据结构
    public static class MonthData {
        public int daysInMonth;
        public int[] dailySubmissions;
        public MonthData(int days) {
            this.daysInMonth = days;
            this.dailySubmissions = new int[days + 1];
        }
    }

    @Scheduled(fixedRate = 1000) // 每秒检查一次
    public void consumeSubmissionMessages() {
        try {
            String messageJson = Redis.listPop("submission");
            while (messageJson != null) {
                Logger.log(Type.SERVER, LogLevel.INFO, "send record message: " + messageJson);
                Message message = JSON.parseObject(messageJson, Message.class);
                String body = message.getBody();

                try {
                    SubmitRecordDTO record = JSON.parseObject(body, SubmitRecordDTO.class);

                    if (record == null) {
                        Logger.log(Type.SERVER, LogLevel.ERROR, "consumer record null: " + body);
                        messageJson = Redis.listPop("submission");
                        continue;
                    }
                    Integer res = mybatisRepos.insertRecord(record);
                    if (res == null || res <= 0) {
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "mq consumer insert error: " + res + " | Data: " + body);
                    }
                    long submitTime = record.getSubmitTime();
                    LocalDate submitDate = Instant.ofEpochMilli(submitTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    int year = submitDate.getYear();
                    int month = submitDate.getMonthValue();
                    int dayOfMonth = submitDate.getDayOfMonth();

                    String bucketName = "letucoj";
                    String objectName = "user/" + record.getUserName() + "/heatmap/" + year + ".json";

                    Map<String, MonthData> yearHeatmap;
                    if (minioRepos.isObjectExist(bucketName, objectName)) {
                        byte[] data = minioRepos.getFile(bucketName, objectName);
                        String jsonString = new String(data, StandardCharsets.UTF_8);

                        try {
                            yearHeatmap = JSON.parseObject(jsonString, new com.alibaba.fastjson.TypeReference<>() {});
                        } catch (Exception e) {
                            Logger.log(Type.EXTERNAL, LogLevel.ERROR, "mq consumer heatmap error: JSON deserialization failed, re-initializing: " + e.getMessage());
                            yearHeatmap = initializeYearHeatmap(year);
                        }

                    } else {
                        yearHeatmap = initializeYearHeatmap(year);
                    }

                    String monthKey = String.valueOf(month);
                    MonthData monthData = yearHeatmap.get(monthKey);

                    if (monthData != null && dayOfMonth < monthData.dailySubmissions.length) {
                        monthData.dailySubmissions[dayOfMonth]++;

                        String updatedJson = JSON.toJSONString(yearHeatmap);
                        minioRepos.addFile(bucketName, objectName, updatedJson.getBytes(StandardCharsets.UTF_8));
                    } else {
                        Logger.log(Type.EXTERNAL, LogLevel.ERROR, "mq consumer heatmap error: Invalid date components for update: Year=" + year + ", Month=" + month + ", Day=" + dayOfMonth + ". monthData is null: " + (monthData == null));
                    }

                } catch (Exception e) {
                    Logger.log(Type.SERVER, LogLevel.ERROR, body + e.getMessage());
                    throw new RuntimeException("Consumer failed to process message and update record/heatmap: ", e);
                }
                messageJson = Redis.listPop("submission");
            }
        } catch (Exception e) {
            Logger.log(Type.SERVER, LogLevel.ERROR, e.getMessage());
        }
    }

    private Map<String, MonthData> initializeYearHeatmap(int year) {
        Map<String, MonthData> yearData = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int days = yearMonth.lengthOfMonth();
            yearData.put(String.valueOf(month), new MonthData(days));
        }

        return yearData;
    }
}
