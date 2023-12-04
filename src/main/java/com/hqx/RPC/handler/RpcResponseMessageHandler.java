package com.hqx.RPC.handler;

import com.hqx.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description rpc响应消息处理器 <br/> 虽然这个handler是有状态的，但是我们处理了线程安全的问题，所以加@ChannelHandler.Sharable也是可以的
 * @Create by hqx
 * @Date 2023/12/4 21:27
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //                   SequenceId   用来接收结果的 promise 对象
    public static final Map<Integer, Promise<Object>> PROMISES = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        // 获取到代理对象调用方法时放入的空 promise 对象，并从 map 中移除这个 promise
        Promise<Object> promise = PROMISES.remove(msg.getSequenceId());
       if (promise != null) {
           Object returnValue = msg.getReturnValue();
           Exception exceptionValue = msg.getExceptionValue();
           if (exceptionValue != null) {
               // 异常结果不为空
               promise.setFailure(exceptionValue);
           } else {
               // 异常结果为空
               promise.setSuccess(returnValue);
           }
       }

        log.debug("{}", msg);
    }
}
