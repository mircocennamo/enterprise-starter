# Enterprise Starter - Platform Libraries

**Librerie enterprise riutilizzabili per Spring Boot 4.1+**

## 📦 Moduli

### Core Components
- **platform-core** - Utilità base (response objects, constants, enums)
- **platform-web** - Componenti REST/MVC e gestione eccezioni globale
- **platform-security** - Configurazione security con JWT authentication
- **platform-autoconfigure** - Spring Boot AutoConfiguration

### Integration
- **platform-spring-boot-starter** - Spring Boot Starter che aggrega tutti i moduli

## 🚀 Quick Start

Aggiungi il starter al tuo `pom.xml`:

```xml
<dependency>
    <groupId>it.interno.platform</groupId>
    <artifactId>platform-spring-boot-starter</artifactId>
    <version>0.0.3-SNAPSHOT</version>
</dependency>
```

## ✨ Features

✅ **Response API standardizzata** - Formato uniforme per tutti gli endpoint  
✅ **Global Exception Handling** - RFC 7807 Problem Details  
✅ **JWT Authentication** - Token-based security  
✅ **Auto-configuration** - Zero configuration required  
✅ **HTTP Client** - RestClient con JWT propagation  
✅ **Monitoring** - Prometheus metrics + OpenTelemetry tracing  
✅ **OpenAPI 3.1** - Swagger UI integrato  

## 🏗️ Architettura

```
platform-parent
├── platform-core       (Base layer - no dependencies)
├── platform-security   (JWT + Spring Security)
├── platform-web        (REST/MVC + Exception handling)
├── platform-autoconfigure (AutoConfiguration)
└── platform-spring-boot-starter (Aggregator)
```

**Nessuna dipendenza circolare** ✓

## 📋 Requirements

- **Java:** 25
- **Spring Boot:** 4.1.0+
- **Maven:** 3.9+

## 📝 License

Internal Use