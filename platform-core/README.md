# Platform Core

**Core utilities e base classes per l'enterprise platform.**

## 📦 Contenuto

### Response Objects
- **ApiResponse<T>** - Record per risposta API standardizzata
  ```java
  public record ApiResponse<T>(
      boolean success,
      Instant timestamp,
      int status,
      List<ApiError> errors,
      T data,
      Map<String,Object> metadata
  )
  ```

- **ApiError** - Dettagli errori
- **Responses** - Factory per creare response standardizzate

### Commons
- **ClientGroup** - Enum per i gruppi client
- Utility classes per costanti e enumerazioni
- Exception base classes

## 🎯 Utilizzo

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUser(@PathVariable Long id) {
        return Responses.ok(userService.findById(id));
    }
}
```

## 📐 Architettura

- **Nessuna dipendenza** verso Spring (pure Java)
- Scope: `provided` per Lombok
- Base layer - utilizzato da tutti gli altri moduli

## 🔧 Dependencies

```xml
<!-- Solo Lombok (provided scope) -->
```