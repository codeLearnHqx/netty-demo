package com.hqx.message;

/**
 * @Description 客户端心跳包
 * @Create by hqx
 * @Date 2023/12/4 14:25
 */
public class PingMessage extends Message{
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
