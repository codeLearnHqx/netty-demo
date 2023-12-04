package com.hqx.config;

import com.hqx.protocol.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Description 配置类
 * @Create by hqx
 * @Date 2023/12/4 15:14
 */
public abstract class Config {
    static Properties properties;
    static {
        try (InputStream in = Config.class.getResourceAsStream("/application.properties")) {
            properties = new Properties();
            // 将配置信息读入 properties
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取端口号
     */
    public static int getServerPort() {
        String port = properties.getProperty("server.port");
        if (port == null) {
            return 8080;
        } else {
            return Integer.parseInt(port);
        }
    }

    /**
     * 获取序列化算法
     */
    public static Serializer.Algorithm getSerializerAlgorithm() {
        String algorithm = properties.getProperty("serializer.algorithm");
        if (algorithm == null) {
            return Serializer.Algorithm.Java;
        } else {
            return Serializer.Algorithm.valueOf(algorithm);
        }
    }
}
