# Platform Spring Boot Starter

**Spring Boot Starter che aggrega l'enterprise platform.**

## 📦 Contenuto

Aggregatore che include tutti i moduli della platform:
- ✅ platform-core
- ✅ platform-autoconfigure
- ✅ platform-security
- ✅ platform-web

## 🚀 Quick Start

### 1. Aggiungi al tuo pom.xml

```xml
<dependency>
    <groupId>it.interno.platform</groupId>
    <artifactId>platform-spring-boot-starter</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```

### 2. Pronto!

Tutti i componenti sono auto-registrati via `PlatformWebAutoConfiguration`.

## ✨ Cosa Otieni

Dopo l'inclusione dello starter, hai accesso a:

### Response Standardizzata (platform-core)
```java
@GetMapping("/users/{id}")
public ApiResponse<UserDto> getUser(@PathVariable Long id) {
    return Responses.ok(userService.findById(id));
}
```

### Global Exception Handling (platform-web)
```java
throw new BusinessException("Invalid user", HttpStatus.BAD_REQUEST);
// Automaticamente gestito e serializzato
```

### JWT Authentication (platform-security)
```java
String token = jwtTokenProvider.getToken();
// Token propagato automaticamente su tutte le HTTP calls
```

### AutoConfiguration (platform-autoconfigure)
- Beans auto-registrati
- Zero configuration required
- Override su demand con `@ConditionalOnMissingBean`

## 📐 Architettura

```
platform-spring-boot-starter
├── platform-core           (Base utilities)
├── platform-autoconfigure  (Bean registration)
├── platform-security       (JWT + Spring Security)
└── platform-web           (REST/MVC + Exception handling)

↓ (All integrated)

Spring Boot 4.1.0+
Spring Security
Spring Web
OpenAPI 3.1
Prometheus Monitoring
OpenTelemetry Tracing
```

## 🎯 Use Cases

### Caso 1: REST API Service
```java
@SpringBootApplication
public class MyApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApiApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return Responses.ok(userService.findById(id));
    }
    
    @PostMapping
    public ApiResponse<UserDto> createUser(@RequestBody UserRequest req) {
        if (req.getName() == null) {
            throw new BusinessException("Name required");
        }
        return Responses.ok(userService.create(req));
    }
}
```

All error handling, JWT auth, response serialization è gestito automaticamente.

### Caso 2: Multi-Service Architecture
Includi lo starter in ogni microservizio per:
- Unificare il formato risposta API
- Unificare la gestione errori
- JWT propagation automatico
- Monitoring standardizzato

## 🔧 Requirements

- Java 25
- Spring Boot 4.1.0+
- Maven 3.9+

## 📖 Documentation

Vedi i README dei singoli moduli:
- [platform-core](../platform-core/README.md) - Response objects, utilities
- [platform-web](../platform-web/README.md) - REST components, exception handling
- [platform-security](../platform-security/README.md) - JWT, Spring Security
- [platform-autoconfigure](../platform-autoconfigure/README.md) - Auto-configuration

## 🚀 Next Steps

- [ ] Leggere la documentazione dei singoli moduli
- [ ] Implementare i controller con `Responses.ok()` e `Responses.error()`
- [ ] Configurare Spring Security per authorization
- [ ] Aggiungere test unitari
- [ ] Configurare CI/CD pipeline

## 📝 Version

`0.0.3-SNAPSHOT`

## 📄 License

Internal Use
