package com.hqx.advence.c2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/11/30 22:04
 */
public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        // netty 提供的用于测试 handler 的工具
        EmbeddedChannel channel = new EmbeddedChannel(
                /*
                    int maxFrameLength,         数据的最大长度
                    int lengthFieldOffset,      长度字段偏移量
                    int lengthFieldLength,      长度字段长度
                    int lengthAdjustment,       长度字段为基准，还有几个字段是内容
                    int initialBytesToStrip     从头剥离几个字节
                 */
                // 下面的长度类型为 int，所以占4字节
                new LengthFieldBasedFrameDecoder(
                        1024, 0, 4, 1, 0),
                new LoggingHandler(LogLevel.DEBUG)
        );

        // 4 个字节的内容长度，实际内容
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        send(buf, "Hello, world");
        send(buf, "Hi");
        // 将内容写入 channel
        channel.writeInbound(buf);
    }


    private static void send(ByteBuf buf, String content) {
        // 实际内容
        byte[] bytes = content.getBytes();
        // 实际内容的长度
        int length = bytes.length;
        // 写入实际内容长度
        buf.writeInt(length);
        // 额外内容
        buf.writeByte(1);
        // 写入实际内容
        buf.writeBytes(bytes);
    }
}
