#!/bin/bash

echo "=========================================="
echo "Network Watcher Deployment Script"
echo "=========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Installing Docker..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    echo "✅ Docker installed successfully"
else
    echo "✅ Docker is already installed"
fi

# Check if Docker Compose is installed
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Installing..."
    apt install docker-compose-plugin -y
    echo "✅ Docker Compose installed successfully"
else
    echo "✅ Docker Compose is already installed"
fi

echo ""
echo "=========================================="
echo "Setting up environment..."
echo "=========================================="

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "Creating .env file from template..."
    cp .env.example .env
    echo "⚠️  Please edit .env file with your configuration"
    echo "   Run: nano .env"
    echo ""
    read -p "Press Enter to continue after editing .env file..."
fi

echo ""
echo "=========================================="
echo "Building and starting services..."
echo "=========================================="

# Build and start services
docker compose up -d --build

echo ""
echo "=========================================="
echo "Checking service status..."
echo "=========================================="

sleep 5
docker compose ps

echo ""
echo "=========================================="
echo "✅ Deployment Complete!"
echo "=========================================="
echo ""
echo "Access your application:"
echo "  Frontend: http://localhost"
echo "  Backend API: http://localhost:8080"
echo "  Default Login: admin / admin123"
echo ""
echo "Useful commands:"
echo "  View logs: docker compose logs -f"
echo "  Stop services: docker compose down"
echo "  Restart: docker compose restart"
echo ""
echo "For production deployment, see DEPLOYMENT.md"
echo "=========================================="
