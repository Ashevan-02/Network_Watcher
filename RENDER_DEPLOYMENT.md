# Deploy Network Watcher to Render.com (FREE)

## Step-by-Step Deployment Guide

### Step 1: Sign Up for Render

1. Go to https://render.com
2. Click **"Get Started"**
3. Sign up with **GitHub** (easiest option)
4. Authorize Render to access your GitHub

### Step 2: Create PostgreSQL Database

1. Click **"New +"** → **"PostgreSQL"**
2. **Name**: `network-watcher-db`
3. **Database**: `networkwatcher`
4. **User**: `admin`
5. **Region**: Choose closest to you
6. **Plan**: **Free**
7. Click **"Create Database"**
8. Wait 2-3 minutes for it to be ready
9. **Copy the "Internal Database URL"** (you'll need this)

### Step 3: Deploy Backend

1. Click **"New +"** → **"Web Service"**
2. **Connect Repository**: Select `Network_Watcher`
3. **Configure**:
   - **Name**: `network-watcher-backend`
   - **Region**: Same as database
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Environment**: `Docker`
   - **Dockerfile Path**: `backend/Dockerfile`
   - **Plan**: **Free**

4. **Add Environment Variables** (click "Advanced"):
   ```
   SPRING_DATASOURCE_URL = [paste your database Internal URL]
   SPRING_DATASOURCE_USERNAME = admin
   SPRING_DATASOURCE_PASSWORD = [from database page]
   JWT_SECRET = your-random-secret-key-min-32-characters
   SPRING_JPA_HIBERNATE_DDL_AUTO = update
   SPRING_JPA_SHOW_SQL = false
   ```

5. Click **"Create Web Service"**
6. Wait 5-10 minutes for build and deploy
7. **Copy your backend URL**: `https://network-watcher-backend.onrender.com`

### Step 4: Deploy Frontend

1. Click **"New +"** → **"Static Site"**
2. **Connect Repository**: Select `Network_Watcher`
3. **Configure**:
   - **Name**: `network-watcher-frontend`
   - **Branch**: `main`
   - **Root Directory**: Leave empty
   - **Build Command**: 
     ```
     cd frontend && npm install && npm run build
     ```
   - **Publish Directory**: 
     ```
     frontend/dist
     ```

4. **Add Environment Variables**:
   ```
   VITE_API_URL = https://network-watcher-backend.onrender.com
   VITE_WS_URL = wss://network-watcher-backend.onrender.com
   ```

5. Click **"Create Static Site"**
6. Wait 3-5 minutes for build

### Step 5: Update Backend CORS

1. Go to your **backend service** on Render
2. Click **"Environment"**
3. Add new environment variable:
   ```
   CORS_ALLOWED_ORIGINS = https://network-watcher-frontend.onrender.com
   ```
4. Click **"Save Changes"** (this will redeploy)

### Step 6: Access Your Application

Your app is now live! 🎉

**Frontend URL**: `https://network-watcher-frontend.onrender.com`

**Default Login**:
- Username: `admin`
- Password: `admin123`

---

## Important Notes

### Free Tier Limitations:
- Services **sleep after 15 minutes** of inactivity
- First request after sleep takes **30-60 seconds** to wake up
- Database has **1GB storage limit**
- **90 days** of inactivity will delete the service

### Keep Services Awake (Optional):
Use a free uptime monitor:
1. Go to https://uptimerobot.com (free)
2. Add monitor for your frontend URL
3. Check every 5 minutes
4. This keeps your app awake

### Custom Domain (Optional):
1. Buy domain from Namecheap/GoDaddy (~$10/year)
2. In Render, go to your frontend service
3. Click "Custom Domains" → Add your domain
4. Update DNS records as instructed

---

## Troubleshooting

### Backend won't start:
- Check logs in Render dashboard
- Verify database URL is correct
- Make sure JWT_SECRET is set

### Frontend can't connect to backend:
- Check VITE_API_URL matches your backend URL
- Verify CORS_ALLOWED_ORIGINS includes frontend URL
- Check browser console for errors

### Database connection failed:
- Use "Internal Database URL" not "External"
- Check username/password are correct
- Verify database is running

### App is slow:
- Normal on free tier (services sleep)
- Use UptimeRobot to keep awake
- Or upgrade to paid plan ($7/month)

---

## Updating Your App

When you push changes to GitHub:
1. Render **auto-deploys** automatically
2. Check deployment logs in dashboard
3. Wait 5-10 minutes for rebuild

Or manually trigger:
1. Go to service in Render
2. Click **"Manual Deploy"** → **"Deploy latest commit"**

---

## Costs

**Render Free Tier**:
- Backend: FREE
- Frontend: FREE  
- Database: FREE
- Total: **$0/month** ✅

**Optional Upgrades**:
- Starter plan: $7/month (no sleep, faster)
- Custom domain: ~$10/year

---

## Support

- Render Docs: https://render.com/docs
- GitHub Issues: https://github.com/Ashevan-02/Network_Watcher/issues

---

## Next Steps After Deployment

1. ✅ Change default admin password
2. ✅ Set up UptimeRobot to prevent sleep
3. ✅ Test all features
4. ✅ Share your app URL with others!

**Your app is now accessible worldwide!** 🌍
