import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = 'http://localhost:8080/api/ws/bus-tracking';

let stompClient = null;

/**
 * Connect to the Spring Boot WebSocket and subscribe to a bus topic.
 * @param {number} vehicleId - The bus to track.
 * @param {function} onLocation - Callback with the location payload.
 * @returns {function} disconnect - Call this to unsubscribe & disconnect.
 */
export function subscribeToBus(vehicleId, onLocation) {
  stompClient = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    reconnectDelay: 3000,
    onConnect: () => {
      console.log('✅ WebSocket connected');
      stompClient.subscribe(`/topic/bus/${vehicleId}`, (message) => {
        const location = JSON.parse(message.body);
        onLocation(location);
      });
    },
    onDisconnect: () => console.log('❌ WebSocket disconnected'),
    onStompError: (frame) => console.error('STOMP error', frame),
  });

  stompClient.activate();

  // Return a cleanup function
  return () => {
    if (stompClient) stompClient.deactivate();
  };
}
