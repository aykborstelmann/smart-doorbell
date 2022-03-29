package de.borstelmann.doorbell.server.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@TestConfiguration
public class StompConfig {
    @Bean
    public WebSocketStompClient stompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        stompClient.setTaskScheduler(taskScheduler);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        return stompClient;
    }

    //SimpleBrokerMessageHandler doesn't support RECEIPT frame, hence we emulate it this way
    @Bean
    public ApplicationListener<SessionSubscribeEvent> webSocketEventListener(final AbstractSubscribableChannel clientOutboundChannel) {
        return event -> {
            Message<byte[]> message = event.getMessage();
            StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(message);
            if (stompHeaderAccessor.getReceipt() != null) {
                stompHeaderAccessor.setHeader("stompCommand", StompCommand.RECEIPT);
                stompHeaderAccessor.setReceiptId(stompHeaderAccessor.getReceipt());
                clientOutboundChannel.send(MessageBuilder.createMessage(new byte[0], stompHeaderAccessor.getMessageHeaders()));
            }
        };
    }
}
