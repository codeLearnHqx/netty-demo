package com.hqx.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @Description 封装LengthFieldBasedFrameDecoder进行使用
 * @Create by hqx
 * @Date 2023/12/3 13:59
 */
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * 根据 MessageCodecSharable 中的协议设计来填写参数
     */
    public ProtocolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }
    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
