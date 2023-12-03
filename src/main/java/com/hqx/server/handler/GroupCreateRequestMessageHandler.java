package com.hqx.server.handler;

import com.hqx.message.GroupCreateRequestMessage;
import com.hqx.message.GroupCreateResponseMessage;
import com.hqx.server.session.Group;
import com.hqx.server.session.GroupSession;
import com.hqx.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @Description 创建群请求处理器
 * @Create by hqx
 * @Date 2023/12/3 22:43
 */
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName(); // 群名
        Set<String> members = msg.getMembers(); // 群成员
        // 群管理器
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        // group不存在就创建返回，存在了就返回 null
        Group group = groupSession.createGroup(groupName, members);
        if (group != null) {
            // 发送成功消息
            ctx.writeAndFlush(new GroupCreateResponseMessage(true, groupName + "创建成功"));
            // 发送拉群消息给组员
            List<Channel> channels = groupSession.getMembersChannel(groupName);// 获取在线人员中，属于该组的成员channel
            for (Channel channel : channels) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true, "您已被拉入" + groupName));
            }
        } else {
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, groupName + "已经存在"));
        }



    }
}
