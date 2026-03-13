# Network Watcher - Libraries & Dependencies

## Current Libraries (Phase 1)

### Core Spring Boot
- **spring-boot-starter-web** (4.0.2)
  - Purpose: REST API, HTTP server
  - Includes: Spring MVC, Tomcat, Jackson JSON
  - Used for: Controllers, REST endpoints

- **spring-boot-starter-data-jpa** (4.0.2)
  - Purpose: Database access layer
  - Includes: Hibernate, JPA, Spring Data
  - Used for: Repository, Entity management

### Database
- **H2 Database** (runtime)
  - Purpose: In-memory database for development
  - Used for: Storing device data
  - Note: Switch to PostgreSQL/MySQL for production

### Development Tools
- **spring-boot-devtools** (runtime, optional)
  - Purpose: Hot reload, faster development
  - Features: Auto-restart, LiveReload

- **Lombok** (optional)
  - Purpose: Reduce boilerplate code
  - Generates: Getters, setters, constructors, @Slf4j logger
  - Used in: All model classes, services

### Testing
- **spring-boot-starter-test** (test scope)
  - Includes: JUnit 5, Mockito, AssertJ
  - Purpose: Unit and integration testing

---

## Libraries Needed for Phase 2-6

### Phase 2: Vulnerability Assessment

```xml
<!-- CVE Database Access -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
```

### Phase 3: Bandwidth Monitoring

```xml
<!-- Packet Capture (jNetPcap or Pcap4J) -->
<dependency>
    <groupId>org.pcap4j</groupId>
    <artifactId>pcap4j-core</artifactId>
    <version>1.8.2</version>
</dependency>

<dependency>
    <groupId>org.pcap4j</groupId>
    <artifactId>pcap4j-packetfactory-static</artifactId>
    <version>1.8.2</version>
</dependency>
```

### Phase 4: Packet Analysis

```xml
<!-- Already included in Phase 3 (Pcap4J) -->
```

### Phase 5: Device Control

```xml
<!-- SSH for remote device control -->
<dependency>
    <groupId>com.jcraft</groupId>
    <artifactId>jsch</artifactId>
    <version>0.1.55</version>
</dependency>

<!-- Email notifications -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### Phase 6: Reporting

```xml
<!-- PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>

<!-- Excel Generation -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>

<!-- Scheduling for automated reports -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-quartz</artifactId>
</dependency>
```

### Additional Recommended Libraries

```xml
<!-- Security (JWT authentication) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- WebSocket for real-time updates -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- API Documentation -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

---

## External Tools (Not Java Libraries)

### Already Installed
- **Nmap 7.98** - Network scanning
  - Used via: Runtime.exec()
  - Purpose: Device discovery, OS detection

### Will Need
- **WinPcap/Npcap** - Packet capture (Windows)
  - Required for: Pcap4J library
  - Purpose: Bandwidth monitoring, packet analysis

- **Wireshark** (Optional) - Packet analysis
  - Purpose: Manual packet inspection
  - Note: Can use tshark CLI for automation

---

## Summary by Phase

| Phase | New Libraries | Purpose |
|-------|--------------|---------|
| Phase 1 ✅ | None (using existing) | Network scanning via Nmap |
| Phase 2 | WebFlux, Gson | CVE API calls, JSON parsing |
| Phase 3 | Pcap4J | Packet capture, bandwidth tracking |
| Phase 4 | (Same as Phase 3) | HTTP packet analysis |
| Phase 5 | JSch, Mail | Device control, alerts |
| Phase 6 | iText, POI, Quartz | PDF/Excel reports, scheduling |

---

## Production Recommendations

### Replace H2 with Production Database
```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- OR MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Add Monitoring
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Add Caching
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```
