import { useEffect, useState } from 'react';
import websocketService from '../services/websocketService';

export const useWebSocket = (topic, onMessage) => {
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    websocketService.connect(() => {
      setConnected(true);
      if (topic && onMessage) {
        websocketService.subscribe(topic, onMessage);
      }
    });

    return () => {
      if (topic) {
        websocketService.unsubscribe(topic);
      }
    };
  }, [topic, onMessage]);

  return { connected };
};
