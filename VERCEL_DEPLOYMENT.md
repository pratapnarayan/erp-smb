# Vercel Deployment Guide

This guide explains how to deploy the ERP SMB UI frontend to Vercel.

## Prerequisites

1. A Vercel account (sign up at https://vercel.com)
2. Your backend gateway service deployed and accessible via HTTPS
3. GitHub repository connected to Vercel

## Deployment Steps

### Option 1: Using Vercel Dashboard (Recommended)

1. **Connect Repository**
   - Go to https://vercel.com/new
   - Import your GitHub repository (`pratapnarayan/erp-smb`)
   - Vercel will auto-detect the project

2. **Configure Project Settings**
   - **Root Directory:** Set to `frontend` (IMPORTANT!)
   - **Framework Preset:** Vite
   - **Build Command:** `npm run build` (should auto-detect)
   - **Output Directory:** `dist` (should auto-detect)
   - **Install Command:** `npm install` (should auto-detect)

3. **Environment Variables**
   - Go to Project Settings → Environment Variables
   - Add `VITE_API_BASE_URL` with your backend gateway URL (e.g., `https://your-gateway.herokuapp.com/api` or your deployed backend URL)
   - **Note:** If your backend is on a different domain, you may need to configure CORS

4. **Deploy**
   - Click "Deploy"
   - Wait for the build to complete

### Option 2: Using Vercel CLI

1. **Install Vercel CLI**
   ```bash
   npm i -g vercel
   ```

2. **Login to Vercel**
   ```bash
   vercel login
   ```

3. **Deploy**
   ```bash
   vercel --prod
   ```
   - When prompted, set:
     - **Root Directory:** `frontend`
     - **Build Command:** `npm run build`
     - **Output Directory:** `dist`

## Configuration Files

### Root `package.json`
This file delegates build commands to the `frontend` directory.

### `vercel.json`
Configuration file for Vercel deployment. Currently configured to:
- Build from root using `npm run build`
- Output to `frontend/dist`
- Install dependencies via `npm install`

### `.vercelignore`
Excludes backend code, build artifacts, and unnecessary files from deployment.

## Backend API Configuration

### Update API Base URL for Production

If your backend is deployed separately, update `frontend/src/api/config/baseUrl.js`:

```javascript
// For production, use environment variable or hardcode your backend URL
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
```

Then set `VITE_API_BASE_URL` in Vercel environment variables.

### CORS Configuration

Ensure your backend gateway service allows requests from your Vercel domain:

```java
// In gateway-service SecurityConfig
.cors(cors -> cors
    .allowedOrigins("https://your-app.vercel.app")
    .allowedMethods("*")
    .allowedHeaders("*")
)
```

## Troubleshooting

### Error: "vite: command not found"
- **Solution:** Ensure Root Directory is set to `frontend` in Vercel dashboard
- Or use the root `package.json` approach (already configured)

### Error: Build fails
- Check that `frontend/package.json` has correct build scripts
- Verify all dependencies are listed in `package.json`
- Check Vercel build logs for specific errors

### API calls failing
- Verify `VITE_API_BASE_URL` environment variable is set correctly
- Check CORS configuration on backend
- Ensure backend gateway is accessible from Vercel's servers

### 404 errors on routes
- Ensure `vercel.json` has proper rewrite rules (if using client-side routing)
- Vercel should auto-detect Vite SPA routing, but you may need to add:
  ```json
  {
    "rewrites": [
      { "source": "/(.*)", "destination": "/index.html" }
    ]
  }
  ```

## Post-Deployment

1. **Update Frontend API Configuration**
   - Update `frontend/src/api/config/baseUrl.js` to use production backend URL
   - Or use environment variables for flexibility

2. **Test Deployment**
   - Visit your Vercel deployment URL
   - Test login and API calls
   - Verify all routes work correctly

3. **Set Custom Domain** (Optional)
   - Go to Project Settings → Domains
   - Add your custom domain
   - Configure DNS as instructed

## Notes

- The backend services are **not** deployed to Vercel (they're separate microservices)
- Only the React frontend is deployed to Vercel
- Backend should be deployed separately (e.g., Heroku, AWS, Railway, etc.)
- Ensure backend gateway is publicly accessible and has CORS configured

