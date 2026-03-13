import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_BASE_URL } from '../utils/constants';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = {};
  }

  connect(onConnect) {
    if (this.client?.connected) {
      onConnect?.();
      return;
    }
    
    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_BASE_URL),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connected');
        onConnect?.();
      },
      onStompError: (frame) => {
        console.error('WebSocket error:', frame);
      }
    });

    this.client.activate();
  }

  subscribe(topic, callback) {
    if (!this.client?.connected) return;
    
    const subscription = this.client.subscribe(topic, (message) => {
      callback(JSON.parse(message.body));
    });
    
    this.subscriptions[topic] = subscription;
    return subscription;
  }

  unsubscribe(topic) {
    if (this.subscriptions[topic]) {
      this.subscriptions[topic].unsubscribe();
      delete this.subscriptions[topic];
    }
  }

  disconnect() {
    if (this.client) {
      Object.keys(this.subscriptions).forEach(topic => this.unsubscribe(topic));
      this.client.deactivate();
    }
  }
}

export default new WebSocketService();
