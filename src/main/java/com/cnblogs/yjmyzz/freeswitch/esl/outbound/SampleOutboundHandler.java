package com.cnblogs.yjmyzz.freeswitch.esl.outbound;

import org.freeswitch.esl.client.outbound.AbstractOutboundClientHandler;
import org.freeswitch.esl.client.transport.SendMsg;
import org.freeswitch.esl.client.transport.event.EslEvent;
import org.freeswitch.esl.client.transport.message.EslHeaders;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author 菩提树下的杨过
 */
public class SampleOutboundHandler extends AbstractOutboundClientHandler {

    @Override
    protected void handleConnectResponse(ChannelHandlerContext ctx, EslEvent event) {
        System.out.println("Received connect response :" + event);
        if (event.getEventName().equalsIgnoreCase("CHANNEL_DATA")) {
            // this is the response to the initial connect
            System.out.println("=======================  incoming channel data  =============================");
            System.out.println("Event-Date-Local: " + event.getEventDateLocal());
            System.out.println("Unique-ID: " + event.getEventHeaders().get("Unique-ID"));
            System.out.println("Channel-ANI: " + event.getEventHeaders().get("Channel-ANI"));
            System.out.println("Answer-State: " + event.getEventHeaders().get("Answer-State"));
            System.out.println("Caller-Destination-Number: " + event.getEventHeaders().get("Caller-Destination-Number"));
            System.out.println("=======================  = = = = = = = = = = =  =============================");

            //客人黑名单检测示例
            String callerNumber = event.getEventHeaders().get("Caller-Caller-ID-Number");
            if (callerNumber.equalsIgnoreCase("1002")) {
                System.out.println(callerNumber + " is in blacklist !");
                //挂断
                hangupCall(ctx.getChannel());

            } else {
                //放音乐
                playSound(ctx.getChannel(), event);

                //转接到客服
                bridgeCall(ctx.getChannel(), event);
            }

        } else {
            throw new IllegalStateException("Unexpected event after connect: [" + event.getEventName() + ']');
        }
    }

    private void hangupCall(Channel channel) {
        SendMsg hangupMsg = new SendMsg();
        hangupMsg.addCallCommand("execute");
        hangupMsg.addExecuteAppName("hangup");

        EslMessage response = sendSyncMultiLineCommand(channel, hangupMsg.getMsgLines());
        if (response.getHeaderValue(EslHeaders.Name.REPLY_TEXT).startsWith("+OK")) {
            System.out.println("Call hangup successful");
        } else {
            System.out.println("Call hangup failed: " + response.getHeaderValue(EslHeaders.Name.REPLY_TEXT));
        }
    }

    private void playSound(Channel channel, EslEvent event) {
        String uuid = event.getEventHeaders().get("Unique-ID");
        SendMsg playbackMsg = new SendMsg(uuid);
        playbackMsg.addCallCommand("execute");
        playbackMsg.addExecuteAppName("playback");
        playbackMsg.addExecuteAppArg("/Users/jimmy/Downloads/bgm.wav");
        //同步发送bridge命令接通
        EslMessage response = sendSyncMultiLineCommand(channel, playbackMsg.getMsgLines());
        if (response.getHeaderValue(EslHeaders.Name.REPLY_TEXT).startsWith("+OK")) {
            System.out.println("playback successful");
        } else {
            System.out.println("playback failed: " + response.getHeaderValue(EslHeaders.Name.REPLY_TEXT));
        }
    }


    private void bridgeCall(Channel channel, EslEvent event) {
        //模拟接通前，客人需要等待n秒
        try {
            Random rnd = new Random(System.currentTimeMillis());
            Thread.sleep(1 + rnd.nextInt(20) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<String> extNums = new ArrayList<>(2);
        extNums.add("1000");
        extNums.add("1010");
        //随机找1个目标接通
        String destNumber = extNums.get((int) System.currentTimeMillis() % 2);


        SendMsg bridgeMsg = new SendMsg();
        bridgeMsg.addCallCommand("execute");
        bridgeMsg.addExecuteAppName("bridge");
        bridgeMsg.addExecuteAppArg("user/" + destNumber);

        //同步发送bridge命令接通
        EslMessage response = sendSyncMultiLineCommand(channel, bridgeMsg.getMsgLines());
        if (response.getHeaderValue(EslHeaders.Name.REPLY_TEXT).startsWith("+OK")) {
            String originCall = event.getEventHeaders().get("Caller-Destination-Number");
            System.out.println(originCall + " bridge to " + destNumber + " successful");
        } else {
            System.out.println("Call bridge failed: " + response.getHeaderValue(EslHeaders.Name.REPLY_TEXT));
        }
    }

    @Override
    protected void handleEslEvent(ChannelHandlerContext ctx, EslEvent event) {
        System.out.println("received event:" + event);
    }

    @Override
    protected void handleDisconnectionNotice() {
        super.handleDisconnectionNotice();
        System.out.println("Received disconnection notice");
    }
}
