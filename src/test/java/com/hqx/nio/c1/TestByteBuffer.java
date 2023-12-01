package com.hqx.nio.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description 通过 FileChannel + ByteBuffer 读取文件
 * @Create by hqx
 * @Date 2023/11/24 14:31
 */
@Slf4j
public class TestByteBuffer {

    public static void main(String[] args) {
        /*
            FileChannel
            获取方式：
            1. 输入输出流  2. RandomAccessFile
         */
        try (FileChannel channel = new FileInputStream("data.txt").getChannel()) {
            // 创建缓冲区，并设置缓冲区大小为10字节
            ByteBuffer buffer = ByteBuffer.allocate(10);
           while (true) {
               // 从 channel 读取数据，向 buffer 写入
               int len = channel.read(buffer);
               log.debug("读取到的字节 {}", len);
               // 达到文件末尾时，返回值为 -1
               if (len == -1) {
                   break;
               }
               // 打印 buffer 的内容
               buffer.flip(); // 切换至读模式
               while (buffer.hasRemaining()) { // 是否还有剩余数据未读
                   byte b = buffer.get();// 一次读一个字节
                   log.debug("实际字节 {}", (char) b);
               }
               buffer.clear(); // 切换至写模式
           }
            System.out.println();

        } catch (IOException e) {
            log.error(e.getMessage());
        }


    }

}
