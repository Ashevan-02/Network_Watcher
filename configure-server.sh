#!/bin/bash

echo "=========================================="
echo "Network Watcher Server Configuration"
echo "=========================================="
echo ""

# Get server IP
read -p "Enter your server IP address: " SERVER_IP

if [ -z "$SERVER_IP" ]; then
    echo "❌ Error: IP address cannot be empty"
    exit 1
fi

echo ""
echo "Configuring for IP: $SERVER_IP"
echo ""

# Update frontend environment
cat > frontend/.env.production << EOF
VITE_API_URL=http://${SERVER_IP}:8080
VITE_WS_URL=ws://${SERVER_IP}:8080
EOF

# Update docker-compose CORS settings
sed -i "s|cors.allowed.origins=.*|cors.allowed.origins=http://${SERVER_IP},http://localhost|g" backend/application-prod.properties

echo "✅ Configuration updated!"
echo ""
echo "Now run: docker compose up -d --build"
echo ""
