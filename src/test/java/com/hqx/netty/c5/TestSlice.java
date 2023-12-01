package com.hqx.netty.c5;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/29 15:03
 */
public class TestSlice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'}); // 10字节
        ByteBufUtils.log(buf);

        // 在这个切片过程中，没有发生数据复制
        ByteBuf f1 = buf.slice(0, 5);
        ByteBuf f2 = buf.slice(5, 5);
        ByteBufUtils.log(f1);
        ByteBufUtils.log(f2);

        System.out.println("===============================");
        f1.setByte(0, 'b');
        ByteBufUtils.log(f1);
        ByteBufUtils.log(buf);


        ByteBufUtils.log(f1);
        f1.readByte(); // 读取一个字节
        ByteBufUtils.log(f1);
        ByteBufUtils.log(buf);

    }

}
