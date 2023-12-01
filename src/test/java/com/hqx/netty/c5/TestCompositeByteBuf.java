package com.hqx.netty.c5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;


/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/29 23:05
 */
public class TestCompositeByteBuf {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 组合两个 buf

        /* 产生了内存的复制 */
        //ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        //buffer.writeBytes(buf1).writeBytes(buf2);
        //ByteBufUtils.log(buffer);

        /* 避免了内存的复制 */
        CompositeByteBuf buffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        // true 表示增加写入指针索引，不加读不出来数据
        buffer.addComponents(true, buf1, buf2);

        ByteBufUtils.log(buffer);


    }
}
