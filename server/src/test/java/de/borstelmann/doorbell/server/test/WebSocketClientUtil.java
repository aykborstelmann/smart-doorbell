package de.borstelmann.doorbell.server.test;

import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class WebSocketClientUtil {
    public static <T> StompFrameHandler makeHandler(Class<T> payloadType, Consumer<T> payloadConsumer) {
        return new StompSessionHandlerAdapter() {
            @NotNull
            @Override
            public Type getPayloadType(@NotNull StompHeaders headers) {
                return payloadType;
            }

            @Override
            public void handleFrame(@NotNull StompHeaders headers, Object payload) {
                payloadConsumer.accept(payloadType.cast(payload));
            }
        };
    }
}
