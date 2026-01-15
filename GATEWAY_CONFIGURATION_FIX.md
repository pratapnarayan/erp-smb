# Gateway Configuration Fix - Environment-Specific Routing

## Problem Identified

The original gateway configuration was breaking because:
1. Services running locally don't register with Eureka (`register-with-eureka: false`)
2. Gateway was configured to use Eureka load balancing (`lb://service-name`)
3. This caused 404 errors when trying to route requests

## Solution - Profile-Based Configuration

We now have **two separate configurations** that work for different environments:

### 1. Docker/Production (`application.yml`)
```yaml
proxy:
  routes:
    - id: auth
      path: /api/auth/**
      uri: lb://auth-service        # ← Uses Eureka
      stripPrefix: false
    - id: customers
      path: /api/customers/**
      uri: lb://sales-service        # ← New route for data import
      stripPrefix: false
```

**Used when**: Running with Docker Compose or in production with Eureka
**How it works**: Services register with Eureka, gateway uses load balancing

### 2. Local Development (`application-local.yml`)
```yaml
proxy:
  routes:
    - id: auth
      path: /api/auth/**
      url: http://localhost:8081     # ← Direct URL
      stripPrefix: false
    - id: customers
      path: /api/customers/**
      url: http://localhost:8085     # ← Direct URL to sales-service
      stripPrefix: false
```

**Used when**: Running services individually on local machine
**How it works**: Gateway routes directly to localhost ports

## Key Changes Made

### Gateway Service

**File**: `backend/gateway-service/src/main/resources/application.yml`
- ✅ Restored `lb://` URIs for Eureka-based routing
- ✅ Added `/api/customers/**` route → `lb://sales-service`
- ✅ Changed `stripPrefix: false` for all routes (except reports if needed)

**File**: `backend/gateway-service/src/main/resources/application-local.yml`
- ✅ Uses direct URLs (`http://localhost:PORT`)
- ✅ Added `/api/customers/**` route → `http://localhost:8085`
- ✅ Changed `stripPrefix: false` for all routes

## How to Use

### Local Development (Current Setup)

Since your services are running locally without Eureka registration:

1. **Active Profile**: `local` (spring.profiles.active=local)
2. **Configuration Used**: `application-local.yml`
3. **Restart Gateway**:
   ```bash
   # Stop current gateway process
   # Start with local profile:
   java -jar gateway-service.jar --spring.profiles.active=local
   ```

### Docker Deployment

When using Docker Compose:

1. **Active Profile**: Default (no profile specified)
2. **Configuration Used**: `application.yml`
3. **Services Register**: All services register with Eureka
4. **Gateway Uses**: Load balancing via `lb://service-name`

## StripPrefix Explanation

### Before Fix: `stripPrefix: true`
- Request: `GET /api/auth/login`
- Forwarded to service: `GET /login` ❌ (path not found)

### After Fix: `stripPrefix: false`
- Request: `GET /api/auth/login`
- Forwarded to service: `GET /api/auth/login` ✅ (correct path)

## Port Mapping Reference

| Service | Port | Route Path | Local URL |
|---------|------|------------|-----------|
| Gateway | 8080 | - | http://localhost:8080 |
| Auth | 8081 | /api/auth/** | http://localhost:8081 |
| User | 8082 | /api/users/** | http://localhost:8082 |
| Product | 8083 | /api/products/** | http://localhost:8083 |
| Order | 8084 | /api/orders/** | http://localhost:8084 |
| Sales | 8085 | /api/sales/** | http://localhost:8085 |
| Sales | 8085 | /api/customers/** | http://localhost:8085 |
| Finance | 8086 | /api/finance/** | http://localhost:8086 |
| HRMS | 8087 | /api/hrms/** | http://localhost:8087 |
| Enquiry | 8088 | /api/enquiry/** | http://localhost:8088 |
| Reporting | 8089 | /api/reports/** | http://localhost:8089 |

## Testing the Fix

### Test Gateway Routing (After Restart)

```powershell
# Test auth endpoint
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method Post `
  -Body '{"username":"admin","password":"admin"}' `
  -ContentType "application/json"

# Should return: { accessToken, refreshToken, username, role }
```

### Test from Frontend

```powershell
# Frontend at http://localhost:5173
# Vite proxy forwards /api/** to http://localhost:8080/api/**
# Gateway then forwards to appropriate service
```

## Impact on Data Import Feature

The new `/api/customers/**` route is now properly configured in both profiles:

- **Local**: Routes to `http://localhost:8085` (sales-service)
- **Docker**: Routes to `lb://sales-service`

This ensures the data import endpoints work correctly:
- `POST /api/customers/import`
- `GET /api/customers/import/template`

## Restart Instructions

### For Local Development

**Option 1**: If running as IDE (IntelliJ/Eclipse)
- Stop the gateway-service run configuration
- Start it again (it will auto-reload configuration)

**Option 2**: If running from command line
```bash
# Find the process
tasklist | findstr java

# Kill the gateway process
taskkill /F /PID <gateway-pid>

# Restart
cd backend/gateway-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Option 3**: Restart all services (if needed)
```powershell
# Stop all services
# Then start in order:
# 1. discovery-service (Eureka) - if using
# 2. gateway-service (with local profile)
# 3. auth-service
# 4. Other microservices
```

### For Docker

```bash
cd backend
docker-compose restart gateway-service

# Or rebuild if needed
docker-compose up --build gateway-service
```

## Verification Checklist

After restarting gateway:

- [ ] Gateway starts without errors
- [ ] Can access http://localhost:8080/actuator/health
- [ ] Login works from frontend (http://localhost:5173)
- [ ] Can navigate to Admin → Data Import
- [ ] Can download CSV templates
- [ ] Can upload test CSV files
- [ ] Errors are properly displayed for invalid data

## Future Considerations

### If Enabling Eureka for Local Development

If you want to use Eureka locally:

1. Update `application-local.yml` in all services:
   ```yaml
   eureka:
     client:
       enabled: true
       register-with-eureka: true
       fetch-registry: true
   ```

2. Use `application.yml` configuration (with `lb://`)

3. Restart all services

### Environment Variables

For production, consider using environment variables:

```yaml
proxy:
  routes:
    - id: auth
      path: /api/auth/**
      uri: ${AUTH_SERVICE_URL:lb://auth-service}
```

This allows overriding via env vars without code changes.

## Summary

✅ **Docker deployment is safe** - Uses Eureka load balancing
✅ **Local development works** - Uses direct URLs
✅ **Data import feature integrated** - Routes configured in both profiles
✅ **No breaking changes** - Both environments supported

The configuration is now environment-aware and won't break Docker deployments.
