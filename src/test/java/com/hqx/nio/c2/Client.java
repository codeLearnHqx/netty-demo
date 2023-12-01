package com.hqx.nio.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Description 模拟客户端发请求
 * @Create by hqx
 * @Date 2023/11/25 0:53
 */
public class Client {


    public static void main(String[] args) throws IOException {
        // 建立连接
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        sc.write(StandardCharsets.UTF_8.encode("0123456789abcdef3333\n"));
        System.in.read(); // 阻塞线程
    }

}
