package com.condominio.authservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String TENANT_SIGNATURE_HEADER = "X-Tenant-Signature";

    private final String signatureSecret;

    public TenantContextFilter(@Value("${gateway.signature.secret}") String signatureSecret) {
        this.signatureSecret = signatureSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (shouldBypass(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            String tenantId = request.getHeader(TENANT_HEADER);
            String signature = request.getHeader(TENANT_SIGNATURE_HEADER);

            if (tenantId == null || signature == null || !isSignatureValid(tenantId, signature)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Signature realm=\"Tenant\"");
                return;
            }

            TenantContextHolder.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }

    private boolean shouldBypass(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }

    private boolean isSignatureValid(String tenantId, String signature) {
        String expected = sign(tenantId);
        return Objects.equals(expected, signature);
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signatureSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify tenant signature", ex);
        }
    }
}
