package com.hqx.RPC.service;

/**
 * @Description
 * @Create by hqx
 * @Date 2023/12/4 21:36
 */
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String msg) {
        int i = 1/0;
        return msg + "，你好啊！";
    }
}
