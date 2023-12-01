package com.hqx.nio.c1;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description 分散读入多个ByteBuffer
 * @Create by hqx
 * @Date 2023/11/24 18:08
 */
public class TestScatteringReads {

    public static void main(String[] args) {
        try (RandomAccessFile accessFile = new RandomAccessFile("words.txt", "r")) {
            FileChannel channel = accessFile.getChannel();
            // 创建缓冲区
            ByteBuffer a = ByteBuffer.allocate(3);
            ByteBuffer b = ByteBuffer.allocate(3);
            ByteBuffer c = ByteBuffer.allocate(5);
            // 读取文件
            channel.read(new ByteBuffer[]{a, b, c});
            // 切换读
            a.flip();
            b.flip();
            c.flip();

            // 工具类查看
            ByteBufferUtil.debugAll(a);
            ByteBufferUtil.debugAll(b);
            ByteBufferUtil.debugAll(c);

        } catch (IOException e) {

        }
    }

}
