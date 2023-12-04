package com.hqx.RPC.handler;

import com.hqx.RPC.service.HelloService;
import com.hqx.RPC.service.ServiceFactory;
import com.hqx.message.RpcRequestMessage;
import com.hqx.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Description rpc请求消息处理器
 * @Create by hqx
 * @Date 2023/12/4 21:26
 */
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) throws Exception {
        // 结果返回对象
        RpcResponseMessage response = new RpcResponseMessage();
        try {
            // 获取bean
            HelloService service = (HelloService) ServiceFactory.getService(Class.forName(msg.getInterfaceName()));
            // 反射获取bean方法
            Method method =
                    service.getClass().getMethod(msg.getMethodName(), msg.getParameterTypes());
            // 反射调用bean方法得到结果
            Object invoke = method.invoke(service, msg.getParameterValue());
            // 设置成功返回结果
            response.setReturnValue(invoke);
            response.setSequenceId(msg.getSequenceId());

        } catch (Exception e) {
            e.printStackTrace();
            // 设置失败返回结果
            String exMsg = e.getCause().getMessage();
            response.setExceptionValue(new Exception("远程调用出错：" + exMsg));
        }
        // 响应返回结果对象给客户端
        ctx.writeAndFlush(response);
    }


    // 反射调用接口实现类
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        RpcRequestMessage message = new
                RpcRequestMessage(
                        1,
                        "com.hqx.RPC.service.HelloService",
                        "sayHello",
                                    String.class,
                                    new Class[]{String.class},
                                    new Object[]{"张三"}
                );

        // 获取bean
        HelloService service = (HelloService) ServiceFactory.getService(Class.forName(message.getInterfaceName()));
        // 反射获取bean方法
        Method method =
                service.getClass().getMethod(message.getMethodName(), message.getParameterTypes());
        // 反射调用bean方法
        Object result = method.invoke(service, message.getParameterValue());
        System.out.println(result);
    }

}
