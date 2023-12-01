package com.hqx.nio.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/26 11:15
 */
public class TestClient {

    public static void main(String[] args) throws IOException {
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 8080));
        channel.write(StandardCharsets.UTF_8.encode("1234567890abcdef"));
        System.in.read();
    }

}
