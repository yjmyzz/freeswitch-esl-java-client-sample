package com.cnblogs.yjmyzz.freeswitch.esl.outbound;

import org.freeswitch.esl.client.outbound.AbstractOutboundClientHandler;
import org.freeswitch.esl.client.outbound.AbstractOutboundPipelineFactory;

/**
 * @author 菩提树下的杨过
 */
public class SamplePipelineFactory extends AbstractOutboundPipelineFactory {

    @Override
    protected AbstractOutboundClientHandler makeHandler() {
        return new SampleOutboundHandler();
    }
}
