# Network Watcher Deployment Guide

This guide will help you deploy Network Watcher using Docker on a VPS or local server.

## Prerequisites

- Docker installed (version 20.10+)
- Docker Compose installed (version 2.0+)
- At least 2GB RAM
- 10GB free disk space
- Open ports: 80 (HTTP), 8080 (Backend API), 5432 (PostgreSQL)

## Quick Start (Local Testing)

1. **Clone the repository**
```bash
git clone https://github.com/Ashevan-02/Network_Watcher.git
cd Network_Watcher
```

2. **Create environment file**
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. **Start all services**
```bash
docker-compose up -d
```

4. **Access the application**
- Frontend: http://localhost
- Backend API: http://localhost:8080
- Default login: admin / admin123

## VPS Deployment (DigitalOcean/AWS/Linode)

### Step 1: Create a VPS

**DigitalOcean (Recommended for beginners)**
- Create a Droplet with Docker pre-installed
- Choose: Ubuntu 22.04 with Docker
- Size: Basic plan ($12/month - 2GB RAM)
- Region: Choose closest to you

**AWS EC2**
- Launch t3.small instance
- AMI: Ubuntu 22.04 LTS
- Security Group: Allow ports 22, 80, 443, 8080

### Step 2: Connect to Your Server

```bash
ssh root@your-server-ip
```

### Step 3: Install Docker (if not pre-installed)

```bash
# Update system
apt update && apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose
apt install docker-compose-plugin -y

# Verify installation
docker --version
docker compose version
```

### Step 4: Deploy the Application

```bash
# Clone repository
git clone https://github.com/Ashevan-02/Network_Watcher.git
cd Network_Watcher

# Create .env file
cp .env.example .env
nano .env  # Edit with your settings

# Configure server IP (IMPORTANT!)
chmod +x configure-server.sh
./configure-server.sh
# Enter your server's public IP when prompted

# Start services
docker compose up -d --build

# Check status
docker compose ps
```

### Step 5: Configure Firewall

```bash
# Allow HTTP, HTTPS, SSH
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
```

### Step 6: Access Your Application

Open browser: `http://your-server-ip`

## Environment Variables

Edit `.env` file with your configuration:

```env
# Database
DB_PASSWORD=strong-password-here

# JWT Secret (generate random string)
JWT_SECRET=your-random-secret-key-min-32-chars

# Email (for alerts)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## SSL/HTTPS Setup (Optional but Recommended)

### Using Let's Encrypt with Nginx

1. **Install Certbot**
```bash
apt install certbot python3-certbot-nginx -y
```

2. **Get SSL Certificate**
```bash
certbot --nginx -d yourdomain.com
```

3. **Auto-renewal**
```bash
certbot renew --dry-run
```

## Docker Commands

```bash
# Start services
docker compose up -d

# Stop services
docker compose down

# View logs
docker compose logs -f

# View specific service logs
docker compose logs -f backend
docker compose logs -f frontend

# Restart services
docker compose restart

# Rebuild after code changes
docker compose up -d --build

# Remove everything (including data)
docker compose down -v
```

## Updating the Application

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker compose up -d --build

# Or rebuild specific service
docker compose up -d --build backend
```

## Troubleshooting

### Backend won't start
```bash
# Check logs
docker compose logs backend

# Check database connection
docker compose exec postgres psql -U admin -d networkwatcher
```

### Frontend can't connect to backend
- Check backend is running: `docker compose ps`
- Verify API URL in frontend environment
- Check firewall rules

### Database issues
```bash
# Access database
docker compose exec postgres psql -U admin -d networkwatcher

# Reset database
docker compose down -v
docker compose up -d
```

### Port conflicts
If ports 80 or 8080 are in use:
```bash
# Edit docker-compose.yml
# Change "80:80" to "8000:80"
# Change "8080:8080" to "8081:8080"
```

## Monitoring

### Check resource usage
```bash
docker stats
```

### Check disk space
```bash
df -h
docker system df
```

### Clean up unused resources
```bash
docker system prune -a
```

## Backup

### Backup database
```bash
docker compose exec postgres pg_dump -U admin networkwatcher > backup.sql
```

### Restore database
```bash
cat backup.sql | docker compose exec -T postgres psql -U admin networkwatcher
```

## Security Best Practices

1. **Change default passwords** in `.env`
2. **Use strong JWT secret** (min 32 characters)
3. **Enable firewall** (ufw)
4. **Use HTTPS** with SSL certificate
5. **Regular updates**: `apt update && apt upgrade`
6. **Backup database** regularly
7. **Monitor logs** for suspicious activity

## Performance Optimization

### For production with many devices:

Edit `docker-compose.yml`:
```yaml
backend:
  environment:
    SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE: 20
    SPRING_JPA_PROPERTIES_HIBERNATE_ORDER_INSERTS: true
  deploy:
    resources:
      limits:
        memory: 1G
      reservations:
        memory: 512M
```

## Cost Estimates

**DigitalOcean**
- Basic Droplet: $12/month (2GB RAM)
- Domain: $12/year (optional)

**AWS EC2**
- t3.small: ~$15/month
- Elastic IP: Free (if attached)
- Data transfer: ~$1-5/month

**Total**: ~$12-20/month

## Support

- GitHub Issues: https://github.com/Ashevan-02/Network_Watcher/issues
- Documentation: Check README.md files in backend/frontend folders

## Next Steps

1. Set up domain name (optional)
2. Configure SSL certificate
3. Set up automated backups
4. Configure email alerts
5. Add monitoring (Prometheus/Grafana)
