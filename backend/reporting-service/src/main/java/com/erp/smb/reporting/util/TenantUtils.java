package com.erp.smb.reporting.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

public final class TenantUtils {
    private TenantUtils() {}
    public static String getTenantId(HttpServletRequest request) {
        Object obj = request.getAttribute("jwtClaims");
        if (obj instanceof Claims claims) {
            Object t = claims.get("tenantId");
            if (t == null) t = claims.get("tenant_id");
            if (t == null) t = claims.get("tenant");
            if (t != null) return String.valueOf(t);
        }
        // Fallback to header for local/non-authenticated access
        String header = request.getHeader("X-Tenant-Id");
        if (header != null && !header.isBlank()) return header;
        return null;
    }
}
