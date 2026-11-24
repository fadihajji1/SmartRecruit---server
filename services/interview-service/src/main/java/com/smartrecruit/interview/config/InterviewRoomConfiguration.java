package com.smartrecruit.interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.smartrecruit.interview.room.InterviewRoomHandler;
import com.smartrecruit.interview.room.InterviewRoomHandshakeInterceptor;

/** Interview rooms: {@code ws(s)://<gateway>/ws/interviews/{interviewId}?access_token=<JWT>}. */
@Configuration
@EnableWebSocket
public class InterviewRoomConfiguration implements WebSocketConfigurer {
    private final InterviewRoomHandler handler;
    private final InterviewRoomHandshakeInterceptor interceptor;
    private final String[] allowedOrigins;

    public InterviewRoomConfiguration(InterviewRoomHandler handler, InterviewRoomHandshakeInterceptor interceptor,
            @Value("${interview.rooms.allowed-origins:*}") String[] allowedOrigins) {
        this.handler = handler;
        this.interceptor = interceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/interviews/*")
                .addInterceptors(interceptor)
                .setAllowedOriginPatterns(allowedOrigins);
    }

    /** WebRTC session descriptions can be larger than the container's default 8 KB text frame. */
    @Bean
    ServletServerContainerFactoryBean webSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(128 * 1024);
        container.setMaxSessionIdleTimeout(120_000L);
        return container;
    }
}
