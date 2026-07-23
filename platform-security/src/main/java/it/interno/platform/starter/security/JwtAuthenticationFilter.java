package it.interno.platform.starter.security;

import it.interno.platform.starter.core.commons.TokenInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

import static it.interno.platform.starter.core.commons.BaseUtility.TOKEN_INFO_RESPONSE;

@Slf4j
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("JwtAuthenticationFilter: Processing request ");
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try{
                String [] parts = token.split("\\.");
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
                TokenInfo tokenInfo = objectMapper.readValue(payloadJson,TokenInfo.class);
                var listaRuoli = Arrays.stream(tokenInfo.gruppi().split(":")).toList();
                log.info("JwtAuthenticationFilter: listaRuoli: " + listaRuoli);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        tokenInfo.sub(), token, listaRuoli.stream().map(SimpleGrantedAuthority::new).toList());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtAuthenticationFilter: Authentication set for user: " + tokenInfo.sub());
                request.setAttribute(TOKEN_INFO_RESPONSE,tokenInfo);

            }catch (Exception e){
                log.error("Errore durante estrazione dati token OIM {}" ,e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
