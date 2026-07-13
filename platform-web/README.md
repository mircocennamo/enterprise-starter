# Platform Web

**REST/MVC components per l'enterprise platform.**

## 📦 Contenuto

### Global Exception Handling
- **GlobalExceptionAdvice** - @RestControllerAdvice centralizzata
  - RFC 7807 Problem Details format
  - Gestione automatica: BusinessException, PongException, validazioni
  - Response standardizzata ApiResponse

### Exception Classes
- **BusinessException** - Eccezioni business domain
- **PongException** - Eccezioni health-check

### Features
- Global error handling
- Unified error response format
- HTTP status mapping
- Stacktrace logging

## 🎯 Utilizzo

Lancia un'eccezione nel tuo controller, verrà automaticamente gestita:

```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    public ApiResponse<UserDto> createUser(@RequestBody UserRequest req) {
        if (req.getName() == null) {
            throw new BusinessException("Name is required", HttpStatus.BAD_REQUEST);
        }
        return Responses.ok(userService.create(req));
    }
}
```

**Risposta automatica in caso di errore:**
```json
{
  "success": false,
  "status": 400,
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    {
      "code": "VALIDATION_ERROR",
      "message": "Name is required",
      "field": null
    }
  ],
  "data": null,
  "metadata": {}
}
```

## 📐 Architettura

- Dipende da: `platform-core`, `platform-security`
- Auto-registrato via `PlatformWebAutoConfiguration`
- Include: Spring Web, Data JPA, Actuator, Prometheus, OpenTelemetry

## 🔧 Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```