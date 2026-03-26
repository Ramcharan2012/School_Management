package com.school.management.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for STOMP-based real-time bus location broadcasting.
 *
 * Parents connect to: ws://localhost:8080/api/ws/bus-tracking
 * Subscribe to:       /topic/bus/{vehicleId}
 *
 * The backend pushes BusLocationEvent to the topic whenever Kafka consumer processes a new coordinate.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory message broker for /topic destinations
        config.enableSimpleBroker("/topic");
        // Prefix for messages FROM the client (not used in our case, but required)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The WebSocket handshake endpoint
        registry.addEndpoint("/ws/bus-tracking")
                .setAllowedOriginPatterns("*")  // Allow all origins (for dev)
                .withSockJS();                   // Fallback for browsers that don't support WS
    }
}
