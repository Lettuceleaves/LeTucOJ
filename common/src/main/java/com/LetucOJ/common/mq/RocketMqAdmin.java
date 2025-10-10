package com.LetucOJ.common.mq;

import org.apache.rocketmq.tools.command.MQAdminStartup;

import java.util.stream.Stream;

public class RocketMqAdmin {

    private static final String NAME_SERVER = "127.0.0.1:9876";
    private static final String CLUSTER = "DefaultCluster";

    public static void createTopic(String topic) {
        run("updateTopic",
                "-n", NAME_SERVER,
                "-c", CLUSTER,
                "-t", topic);
    }

    public static void deleteTopic(String topic) {
        run("deleteTopic",
                "-n", NAME_SERVER,
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
        }
    }
}
