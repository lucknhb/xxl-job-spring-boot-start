package com.nhb.autoconfigure.listener;

import com.nhb.autoconfigure.utils.InterNetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/11 15:42
 * @description:  InterNetUtil 关闭
 */
@Slf4j
public class InterNetCloseHandler implements ApplicationListener<ContextClosedEvent> {
    private final InterNetUtil  interNetUtil;
    public InterNetCloseHandler(InterNetUtil interNetUtil){
        this.interNetUtil = interNetUtil;
    }
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.interNetUtil.shutdown();
    }
}
