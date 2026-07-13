# Platform Security

**JWT Authentication e Spring Security configuration.**

## 📦 Contenuto

### JWT Authentication
- **JwtTokenProvider** - Gestione token JWT
  - Estrae token da SecurityContext
  - Fornisce token per HTTP client propagation

- **JwtAuthenticationFilter** - Filtro autenticazione
  - Intercetta richieste
  - Valida JWT token
  - Auto-registrato via AutoConfiguration

### Security Configuration
- **SecurityConfig** - @Configuration per Spring Security
  - CSRF disabled (stateless REST API)
  - Session management stateless
  - JWT filter chain
  - H2 console support (dev-only)

## 🎯 Utilizzo

### 1. Auto-registrato
Il modulo viene automaticamente registrato quando incluso nel classpath.

### 2. Default: permettAll()
Attualmente tutte le richieste sono permesse (`.anyRequest().permitAll()`).

⚠️ **Per production, implementare autorizzazione:**

```java
@Configuration
@EnableWebSecurity
public class ProductionSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 3. JWT Token Usage

```java
@RestController
@RequestMapping("/api/protected")
public class ProtectedController {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    @GetMapping
    public ApiResponse<String> getProtected() {
        String token = jwtTokenProvider.getToken();
        // Token è già propagato automaticamente su HTTP calls
        return Responses.ok("Protected resource");
    }
}
```

## 📐 Architettura

- Dipende da: `platform-core` (via `platform-web`)
- Include: Spring Security, Spring Web, Spring Context
- DevTools incluso (optional, runtime scope)
- Configuration processor per IDE support

## 🔧 Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
</dependency>
```

## ⚙️ Configuration

**Current State (Dev):**
```java
.authorizeHttpRequests(auth -> auth
    .anyRequest().permitAll()  // ⚠️ No authentication required
)
```

**Recommended (Prod):**
Vedi sezione "Utilizzo > 2" per esempio di configurazione sicura.

## 🔐 Security Considerations

- JWT tokens sono estratti dal SecurityContext
- CSRF è disabilitato (appropriato per stateless REST API)
- Session management è stateless
- DevTools è incluso in scope runtime/optional (dev-only)
- H2 Console è permessa (rimuovere in produzione)

## 🚀 Next Steps

- [ ] Implementare @PreAuthorize su controller/service
- [ ] Configurare endpoint pubblici vs. protetti
- [ ] Aggiungere test per SecurityConfig
- [ ] Implementare token refresh strategy
- [ ] Configurare CORS if needed
