package it.interno.platform.starter.web.filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;



@Slf4j
@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    private static final int MAX_BODY_LENGTH = 10000;
    private static final int CACHE_LIMIT = 10000;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap per leggere il body multiple volte
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request,CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log richiesta in ingresso
            logRequest(requestWrapper);

            // Esegui la catena
            filterChain.doFilter(requestWrapper, responseWrapper);

            // Log risposta
            logResponse(responseWrapper, startTime);

        } finally {
            // Copia il body nella response originale (ESSENZIALE!)
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        if (!log.isInfoEnabled()) return;

        StringBuilder sb = new StringBuilder("\n REQUEST\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Metodo e URI
        sb.append("Method : ").append(request.getMethod()).append("\n");
        sb.append("URI : ").append(request.getRequestURI()).append("\n");

        String query = request.getQueryString();
        if (query != null) {
            sb.append("Query : ").append(query).append("\n");
        }

        // IP Sorgente (con supporto proxy)
        String clientIp = getClientIp(request);
        sb.append("Client IP : ").append(clientIp).append("\n");

        // User (da Principal o header)
        String user = getUser(request);
        sb.append("User : ").append(user).append("\n");

        // Headers (tutti)
        sb.append("Headers :\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            if (isSensitiveHeader(name)) {
                value = "******";
            }
            sb.append(" ").append(name).append(": ").append(value).append("\n");
        }

        // Body
        String body = getRequestBody(request);
        if (body != null && !body.isEmpty()) {
            sb.append("Body : ").append(truncate(body)).append("\n");
        }

        log.info(sb.toString());
    }

    private void logResponse(ContentCachingResponseWrapper response, long startTime) {
        if (!log.isInfoEnabled()) return;

        long duration = System.currentTimeMillis() - startTime;

        StringBuilder sb = new StringBuilder("\n RESPONSE\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Status
        int status = response.getStatus();
        sb.append("Status : ").append(status).append("\n");
        sb.append("Duration : ").append(duration).append(" ms\n");

        // Headers
        sb.append("Headers :\n");
        Collection<String> headerNames = response.getHeaderNames();
        for (String name : headerNames) {
            String value = response.getHeader(name);
            if (isSensitiveHeader(name)) {
                value = "******";
            }
            sb.append(" ").append(name).append(": ").append(value).append("\n");
        }

        // Body (solo per risposte non binarie)
        String body = getResponseBody(response);
        if (body != null && !body.isEmpty() && !isBinaryContentType(response.getContentType())) {
            sb.append("Body : ").append(truncate(body)).append("\n");
        }

        log.info(sb.toString());
    }

    // --- Metodi helper ---

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Se multipli IP, prendi il primo
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String getUser(HttpServletRequest request) {
        // Da Principal (Spring Security)
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            return principal.getName();
        }

        // Da header custom
        String userHeader = request.getHeader("X-User-Id");
        if (userHeader != null) {
            return userHeader;
        }

        return "anonymous";
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) return null;
        return new String(content, StandardCharsets.UTF_8);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) return null;
        return new String(content, StandardCharsets.UTF_8);
    }

    private String truncate(String text) {
        if (text.length() <= MAX_BODY_LENGTH) return text;
        return text.substring(0, MAX_BODY_LENGTH) + "... [TRUNCATED]";
    }

    private boolean isSensitiveHeader(String name) {
        String lower = name.toLowerCase();
        return lower.contains("authorization") ||
                lower.contains("cookie") ||
                lower.contains("set-cookie") ||
                lower.contains("x-api-key") ||
                lower.contains("password") ||
                lower.contains("secret");
    }

    private boolean isBinaryContentType(String contentType) {
        if (contentType == null) return false;
        String lower = contentType.toLowerCase();
        return lower.contains("image") ||
                lower.contains("pdf") ||
                lower.contains("zip") ||
                lower.contains("octet-stream") ||
                lower.contains("video") ||
                lower.contains("audio");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Escludi endpoint di health, metrics, swagger, ecc.
        String path = request.getRequestURI();
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/favicon.ico");
    }
}
