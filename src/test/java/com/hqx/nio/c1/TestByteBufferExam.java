package com.hqx.nio.c1;

import java.nio.ByteBuffer;

import static com.hqx.nio.c1.ByteBufferUtil.debugAll;


/**
 * @Description 模拟网络中的沾包、黏包问题的解决
 * @Create by hqx
 * @Date 2023/11/24 18:36
 */
public class TestByteBufferExam {

    public static void main(String[] args)  {
        ByteBuffer source = ByteBuffer.allocate(32);
        //                     11            24
        source.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);

        source.put("w are you?\nhaha!\n".getBytes());
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip(); // 切换读
        for (int i = 0; i < source.limit(); i++) {
            if (source.get(i) == '\n') { // position 位置不变
                int length = i + 1 - source.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(source.get()); // position 位置 ++
                }
                debugAll(target);
            }
        }
        source.compact(); // 将还未读取的数据压缩，并切换到写模式
    }
}
