package com.cnblogs.yjmyzz.freeswitch.esl.outbound;

import org.freeswitch.esl.client.outbound.SocketClient;

/**
 * @author 菩提树下的杨过
 */
public class OutboundApp {

    public static void main(String[] args) throws InterruptedException {

        new Thread(() -> {
            SocketClient socketClient = new SocketClient(8086, new SamplePipelineFactory());
            socketClient.start();
        }).start();

        while (true) {
            Thread.sleep(500);
        }
    }

}
