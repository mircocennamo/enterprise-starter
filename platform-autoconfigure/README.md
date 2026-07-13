# Platform AutoConfiguration

**Spring Boot AutoConfiguration per l'enterprise platform.**

## 📦 Contenuto

### PlatformWebAutoConfiguration

Auto-registra automaticamente tutti i bean della platform quando incluso nel classpath.

**ComponentScan:**
```java
@ComponentScan({
    "it.interno.platform.starter.core",
    "it.interno.platform.starter.web",
    "it.interno.platform.starter.security"
})
```

**Beans Auto-Registrati:**

1. **JwtTokenProvider** - Gestione JWT
   - Estrae token da SecurityContext
   - Riutilizzabile per autenticazione

2. **JwtAuthenticationFilter** - Filtro autenticazione JWT
   - Valida token su ogni request
   - Condizionale: `@ConditionalOnMissingBean`

3. **GlobalExceptionAdvice** - Gestione eccezioni globale
   - RFC 7807 Problem Details
   - Auto-registrato

4. **RestClientHttpServiceGroupConfigurer** - HTTP Client globale
   - Default headers: `X-Platform: interno-platform`
   - JWT propagation automatico
   - Timeout: 2s connect, 5s read
   - Error logging

5. **ClientHttpRequestFactory** - HTTP factory
   - HttpClient moderno (Java 11+)
   - Timeout configurati

## 🎯 Utilizzo

### 1. Basta aggiungere il starter:
```xml
<dependency>
    <groupId>it.interno.platform</groupId>
    <artifactId>platform-spring-boot-starter</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```

### 2. Zero configurazione richiesta
Tutti i bean vengono registrati automaticamente.

### 3. Override opzionale
Se vuoi usare una configurazione custom:

```java
@Configuration
public class MyCustomConfig {
    
    @Bean
    public JwtTokenProvider customJwtProvider() {
        // Custom JWT provider
        return new CustomJwtTokenProvider();
    }
}
```

Grazie a `@ConditionalOnMissingBean`, la tua implementazione sarà usata.

## 📐 Architettura

- Meta-module che coordina core, web, security
- Dipende da: `platform-core`, `platform-web`
- Imports: GlobalExceptionAdvice, JwtTokenProvider, JwtAuthenticationFilter
- Configuration processor per IDE support

## 🔧 Configuration

Non richiede configurazione nel `application.properties`.  
Tuttavia, il JWT è propagato automaticamente su tutte le richieste HTTP.

## ⚙️ Advanced Usage

### Custom HTTP Service Group

```java
@Configuration
public class MyHttpServices {
    
    @Bean
    public RestClientHttpServiceGroupConfigurer customHttpConfigurer() {
        return groups -> groups.forEachClient((group, builder) -> {
            builder.defaultHeader("X-Custom-Header", "value");
            // Custom interceptors, etc
        });
    }
}
```

Verranno applicate TUTTE le configurazioni (globale + custom).