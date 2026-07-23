package it.interno.platform.starter.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Duration;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Order(2)
public class LoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Value("${app.logging.max-body-length:10000}")
    private int maxBodyLength;

    @Value("${app.logging.cache-limit:10000}")
    private int cacheLimit;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request, cacheLimit);

        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper(response);

        long startNanos = System.nanoTime();

        try {

            filterChain.doFilter(requestWrapper, responseWrapper);

            long elapsedNanos = System.nanoTime() - startNanos;

            logRequestAndResponse(
                    requestWrapper,
                    responseWrapper,
                    Duration.ofNanos(elapsedNanos));

        } finally {

            responseWrapper.copyBodyToResponse();

        }
    }

    private void logRequestAndResponse(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            Duration duration
            ) {

        String method = request.getMethod();
        String uri = request.getRequestURI();

        if (log.isInfoEnabled()) {

            log.info(
                    " {} {} -> {} ({} ms)",
                    method,
                    uri,
                    response.getStatus(),
                    duration.toMillis());
        }

        if (!log.isDebugEnabled()) {
            return;
        }

        StringBuilder sb = new StringBuilder(4096);

        sb.append("\n================ REQUEST ================\n");
        sb.append("Method        : ").append(method).append("\n");
        sb.append("URI           : ").append(uri).append("\n");

        if (request.getQueryString() != null) {
            sb.append("Query         : ")
                    .append(request.getQueryString())
                    .append("\n");
        }

        sb.append("Client IP     : ")
                .append(getClientIp(request))
                .append("\n");

        sb.append("User          : ")
                .append(getUser())
                .append("\n");

        sb.append("User Roles    : ")
                .append(getUserRoles())
                .append("\n");

        sb.append("\nHeaders:\n");
        appendRequestHeaders(sb, request);

        String requestBody = getRequestBody(request);

        if (requestBody != null
                && isTextContentType(request.getContentType())) {

            sb.append("\nRequest Body:\n")
                    .append(truncate(requestBody))
                    .append("\n");
        }

        sb.append("\n================ RESPONSE ================\n");

        sb.append("Status        : ")
                .append(response.getStatus())
                .append("\n");

        sb.append("Duration      : ")
                .append(duration.toMillis())
                .append(" ms\n");

        sb.append("\nHeaders:\n");
        appendResponseHeaders(sb, response);

        String responseBody = getResponseBody(response);

        if (responseBody != null
                && isTextContentType(response.getContentType())) {

            sb.append("\nResponse Body:\n")
                    .append(truncate(responseBody))
                    .append("\n");
        }

        log.debug(sb.toString());
    }

    private String getUserRoles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                return authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(", "));
            }
        } catch (Exception ex) {
            log.error("Errore recupero ruoli utente da SecurityContext: {}", ex.getMessage());
        }
        return "NESSUN RUOLO";
    }

    private void appendRequestHeaders(
            StringBuilder sb,
            HttpServletRequest request) {

        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String name = headerNames.nextElement();
            String value = request.getHeader(name);

            if (isSensitiveHeader(name)) {
                value = "******";
            }

            sb.append(name)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }

    private void appendResponseHeaders(
            StringBuilder sb,
            HttpServletResponse response) {

        Collection<String> headerNames = response.getHeaderNames();

        for (String name : headerNames) {

            String value = response.getHeader(name);

            if (isSensitiveHeader(name)) {
                value = "******";
            }

            sb.append(name)
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
    }



    private String getClientIp(HttpServletRequest request) {

        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {

            String value = request.getHeader(header);

            if (value != null
                    && !value.isBlank()
                    && !"unknown".equalsIgnoreCase(value)) {

                return value.contains(",")
                        ? value.split(",")[0].trim()
                        : value;
            }
        }

        return request.getRemoteAddr();
    }

    private String getUser() {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication !=null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)){
                return authentication.getName();
            }
        } catch (Exception ex) {
            log.error("Errore recupero nome utente da SecurityContext: {}", ex.getMessage());
        }

        return "USER ANONIMO";
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {

        byte[] content = request.getContentAsByteArray();

        if (content.length == 0) {
            return null;
        }

        Charset charset =
                getCharset(request.getCharacterEncoding());

        return new String(content, charset);
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {

        byte[] content = response.getContentAsByteArray();

        if (content.length == 0) {
            return null;
        }

        Charset charset =
                getCharset(response.getCharacterEncoding());

        return new String(content, charset);
    }

    private Charset getCharset(String encoding) {

        try {

            return Optional.ofNullable(encoding)
                    .map(Charset::forName)
                    .orElse(StandardCharsets.UTF_8);

        } catch (Exception ex) {

            return StandardCharsets.UTF_8;
        }
    }

    private String truncate(String content) {

        if (content.length() <= maxBodyLength) {
            return content;
        }

        return content.substring(0, maxBodyLength)
                + "... [TRUNCATED]";
    }

    private boolean isSensitiveHeader(String headerName) {

        String header = headerName.toLowerCase();

        return header.contains("authorization")
                || header.contains("cookie")
                || header.contains("set-cookie")
                || header.contains("x-api-key")
                || header.contains("password")
                || header.contains("secret")
                || header.contains("token");
    }

    private boolean isTextContentType(String contentType) {

        if (contentType == null) {
            return false;
        }

        String ct = contentType.toLowerCase();

        return ct.startsWith("text/")
                || ct.contains("application/json")
                || ct.contains("+json")
                || ct.contains("application/xml")
                || ct.contains("+xml")
                || ct.contains("application/x-www-form-urlencoded");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/favicon.ico");
    }
}