package com.LetucOJ.practice.repos;

import com.LetucOJ.common.mq.impl.Message;
import com.LetucOJ.common.oss.MinioRepos;
import com.LetucOJ.practice.model.RecordDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "submission",
        consumerGroup = "submission",
        consumeMode = ConsumeMode.ORDERLY
)
public class RocketMQSubmissionConsumer implements RocketMQListener<Message> {

    @Resource
    private MybatisRepos mybatisRepos;

    @Resource
    private MinioRepos minioRepos;

    // 内部类：月份数据结构
    public static class MonthData {
        public int daysInMonth;
        // dailySubmissions 数组大小为 days + 1，索引 1 到 days 对应 1 号到 days 号
        public int[] dailySubmissions;
        public MonthData(int days) {
            this.daysInMonth = days;
            this.dailySubmissions = new int[days + 1];
        }
        public MonthData() {}
    }


    @Override
    public void onMessage(Message message) {
        String body = message.getBody();

        try {
            RecordDTO record = JSON.parseObject(body, RecordDTO.class);

            if (record == null) {
                System.err.println("consumer record null: " + body);
                return;
            }
            Integer res = mybatisRepos.insertRecord(record);
            if (res == null || res <= 0) {
                System.err.println("mq consumer insert error: " + res + " | Data: " + body);
            }
            System.out.println("mq consumer database process complete for record: " + record.getProblemName() + " by " + record.getUserName());

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
                    // TypeReference 用于正确反序列化泛型 Map<String, MonthData>
                    yearHeatmap = JSON.parseObject(jsonString, new TypeReference<Map<String, MonthData>>() {
                    });
                } catch (Exception e) {
                    System.err.println("mq consumer heatmap error: JSON deserialization failed, re-initializing: " + e.getMessage());
                    yearHeatmap = initializeYearHeatmap(year);
                }

            } else {
                yearHeatmap = initializeYearHeatmap(year);
            }

            // 【！！！修复点！！！】
            // yearHeatmap 的键是 String 类型 (如 "10")，必须使用 String 键来获取值
            String monthKey = String.valueOf(month);
            MonthData monthData = yearHeatmap.get(monthKey);

            // 检查 monthData 是否存在，以及 dayOfMonth 索引是否在数组范围内 (最大索引为 daysInMonth)
            if (monthData != null && dayOfMonth < monthData.dailySubmissions.length) {
                monthData.dailySubmissions[dayOfMonth]++;

                String updatedJson = JSON.toJSONString(yearHeatmap);
                minioRepos.addFile(bucketName, objectName, updatedJson.getBytes(StandardCharsets.UTF_8));

                System.out.println("mq consumer heatmap update success for " + record.getUserName() + " on " + submitDate);
            } else {
                // 如果 monthData 为 null，说明在 Map 中没找到该月份数据，或日期组件不合法
                System.err.println("mq consumer heatmap error: Invalid date components for update: Year=" + year + ", Month=" + month + ", Day=" + dayOfMonth + ". monthData is null: " + (monthData == null));
            }

        } catch (Exception e) {
            System.err.println("mq error: " + e.getMessage());
            System.err.println("message body: " + body);
            throw new RuntimeException("Consumer failed to process message and update record/heatmap: ", e);
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