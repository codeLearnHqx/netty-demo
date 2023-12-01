package com.hqx.nio.c2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/25 23:49
 */
public class WriterClient {

    public static void main(String[] args) throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));

        // 3. 接受数据
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            // 将服务端传过来的数据读入 buffer
            // （阻塞模式下，在sc无数据时 read 方法会阻塞当前线程）
            int read = sc.read(buffer); // 已经读的数据数量
            count += read;
            System.out.println(count);
            buffer.clear(); // 清空数据

        }

    }

}
