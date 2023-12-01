package com.hqx.netty.c5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;


import static com.hqx.netty.c5.ByteBufUtils.log;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/29 13:42
 */
public class TestByteBuf {
    public static void main(String[] args) {
        // ByteBuf 是动态扩容的，默认容量为 256
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        System.out.println(buf.getClass());
        log(buf); // 256

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        // 往 ByteBuf 中填充 300 字节
        buf.writeBytes(sb.toString().getBytes());
        log(buf); // 512


    }
}
