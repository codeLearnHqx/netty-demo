package com.hqx.nio.c1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @Description 两个 Channel 传输数据
 * @Create by hqx
 * @Date 2023/11/24 21:36
 */
public class TestChannelTransferTo {

    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("data.txt").getChannel();
                FileChannel to = new FileOutputStream("to.txt").getChannel()) {
            // 效率高，底层会利用操作系统的零拷贝进行优化
            long size = from.size();
            // left表示剩下的（大于2G的这部分数据）
            for (long left = size; left > 0; ) {
                long transferred = from.transferTo(size - left, left, to);
                left -= transferred;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
