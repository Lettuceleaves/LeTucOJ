package com.LetucOJ.common.mq;

import org.apache.rocketmq.tools.command.MQAdminStartup;

import java.util.stream.Stream;

public class RocketMqAdmin {

    private static final String NS = "127.0.0.1:9876";   // NameServer 地址
    private static final String CLUSTER = "DefaultCluster"; // 集群名，按实际改

    /** 创建 Topic，已存在会返回成功 */
    public static void createTopic(String topic) {
        run("updateTopic",
                "-n", NS,
                "-c", CLUSTER,
                "-t", topic);
    }

    /** 删除 Topic */
    public static void deleteTopic(String topic) {
        run("deleteTopic",
                "-n", NS,
                "-c", CLUSTER,
                "-t", topic);
    }

    private static void run(String cmd, String... args) {
        try {
            MQAdminStartup.main(
                    Stream.concat(Stream.of(cmd), Stream.of(args))
                            .toArray(String[]::new)
            );
        } catch (Exception e) {
            // 工具类会调用 System.exit，捕获后阻止退出 JVM
        }
    }
}
