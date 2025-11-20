# VitaLink v2 - Plataforma de Salud Digital

VitaLink v2 es una plataforma integral de gestión sanitaria que conecta pacientes, médicos y administradores a través de una interfaz moderna y funcionalidades en tiempo real.

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Módulos Principales](#módulos-principales)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Base de Datos](#base-de-datos)
- [API REST](#api-rest)
- [Despliegue](#despliegue)
- [Seguridad](#seguridad)
- [Contribución](#contribución)

## Descripción General

VitaLink v2 es una aplicación web de gestión sanitaria que permite:

- **Pacientes**: Registrar síntomas, ver tratamientos, solicitar citas, recibir recordatorios y comunicarse con su médico
- **Médicos**: Gestionar pacientes, crear tratamientos, revisar síntomas, configurar disponibilidad y mantener comunicación en tiempo real
- **Administradores**: Supervisar usuarios, gestionar citas, ver estadísticas y configurar el sistema

### Características Destacadas

- Chat en tiempo real con WebSockets
- Sistema de recordatorios multicanal (App, Email, SMS)
- Estadísticas y análisis de datos de salud
- Gestión de disponibilidad médica
- Panel de administración completo
- Diseño responsive y temas (claro/oscuro)

## Tecnologías

### Backend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.5.3 | Framework de aplicación |
| Spring Security | 6 | Autenticación y autorización |
| Spring Data JPA | - | Acceso a datos |
| Spring WebSocket | - | Mensajería en tiempo real |
| Spring Mail | - | Notificaciones por email |
| Thymeleaf | - | Motor de plantillas |
| Lombok | - | Reducción de código boilerplate |
| Hibernate | - | ORM |

### Base de Datos
- **MySQL 8** (Principal)
- **PostgreSQL** (Alternativa)

### Frontend
- **HTML5** con Thymeleaf
- **CSS3** personalizado
- **JavaScript** vanilla (33 archivos JS)
- **WebSocket** con STOMP y SockJS

### Infraestructura
- **Docker** para containerización
- **Maven** para gestión de dependencias
- **Gmail SMTP** para notificaciones

## Arquitectura

El proyecto sigue una arquitectura en capas MVC:

```
┌─────────────────────────────────────────────────────┐
│                    Presentación                      │
│   (Thymeleaf Templates + JavaScript + WebSocket)     │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                   Controladores                      │
│        (REST Controllers + View Controllers)         │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                    Servicios                         │
│              (Lógica de Negocio)                     │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                  Repositorios                        │
│               (Spring Data JPA)                      │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                  Base de Datos                       │
│                 (MySQL/PostgreSQL)                   │
└─────────────────────────────────────────────────────┘
```

### Patrones Utilizados

- **MVC** (Model-View-Controller)
- **Repository Pattern** (Spring Data JPA)
- **DTO Pattern** (Data Transfer Objects)
- **Service Layer Pattern**
- **Dependency Injection**

## Requisitos Previos

- **Java JDK 17** o superior
- **Maven 3.8+**
- **MySQL 8.0** o **PostgreSQL 13+**
- **Docker** (opcional, para despliegue containerizado)
- **Git**

## Instalación

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd demoVitalink_v2
```

### 2. Configurar Base de Datos

Crear una base de datos MySQL:

```sql
CREATE DATABASE vitalink_v2;
CREATE USER 'vitalink_user'@'localhost' IDENTIFIED BY 'tu_contraseña';
GRANT ALL PRIVILEGES ON vitalink_v2.* TO 'vitalink_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurar Variables de Entorno

Crear un archivo `.env` o configurar las siguientes variables:

```bash
# Base de Datos
DATABASE_URL=jdbc:mysql://localhost:3306/vitalink_v2?useSSL=false&serverTimezone=UTC
DB_USER=vitalink_user
DB_PASSWORD=tu_contraseña

# Email (Gmail SMTP)
EMAIL_USERNAME=tu_email@gmail.com
EMAIL_PASSWORD=tu_app_password
```

### 4. Compilar el Proyecto

```bash
./mvnw clean install
```

### 5. Ejecutar la Aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: `http://localhost:8080`

## Configuración

### application.properties

El archivo de configuración principal se encuentra en `src/main/resources/application.properties`:

```properties
# Nombre de la aplicación
spring.application.name=demoVitalink_v2

# Base de Datos
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Configuración de Seguridad

La seguridad está configurada en `SecurityConfiguration.java` con tres roles:

| Rol | Descripción | Acceso |
|-----|-------------|--------|
| `ADMIN` | Administrador del sistema | Panel completo, gestión de usuarios |
| `MEDICO` | Profesional médico | Pacientes, tratamientos, agenda |
| `USER` | Paciente | Su perfil, citas, síntomas |

## Módulos Principales

### 1. Gestión de Usuarios
- Registro y autenticación
- Perfiles de paciente, médico y administrador
- Configuración de preferencias (tema, idioma, notificaciones)

### 2. Citas Médicas
Estados de cita:
- `PENDIENTE` - Solicitada por el paciente
- `CONFIRMADA` - Aceptada por el médico
- `CANCELADA` - Cancelada
- `COMPLETADA` - Realizada

Características:
- Calendario de disponibilidad del médico
- Duración configurable (60 min por defecto)
- Notas y descripciones

### 3. Tratamientos
- Fórmula del medicamento
- Dosis y frecuencia
- Duración (fecha inicio/fin)
- Interacciones con alimentos
- Observaciones clínicas
- Asociación con síntomas

### 4. Registro de Síntomas
- Categorización por tipo
- Zonas corporales: `CABEZA`, `TORAX`, `ABDOMEN`, `EXTREMIDADES`, etc.
- Descripción detallada (hasta 500 caracteres)
- Marcas de tiempo

### 5. Chat en Tiempo Real
- Comunicación bidireccional médico-paciente
- Conversaciones directas y grupales
- Recibos de lectura
- Archivo y silenciamiento de conversaciones
- Tecnología: WebSocket + STOMP + SockJS

### 6. Recordatorios
Tipos:
- `MEDICAMENTO` - Toma de medicación
- `CITA` - Próximas citas
- `TRATAMIENTO` - Seguimiento de tratamiento
- `OTRO` - Personalizados

Recurrencia:
- Ninguna (único)
- Diario
- Semanal
- Mensual

Canales:
- In-App
- Email
- SMS

### 7. Estadísticas y Análisis
- Métricas de salud del paciente
- Cumplimiento de tratamientos
- Prevalencia de síntomas
- KPIs para administradores
- Gráficos de series temporales

### 8. Configuración Médico
- Perfil profesional (especialidad, colegiado, biografía)
- Preferencias de interfaz
- Configuración de agenda
- Bloques de disponibilidad

### 9. Panel de Administración
- CRUD de usuarios
- Gestión de clientes/pacientes
- Supervisión de tratamientos
- Reportes y estadísticas
- Moderación de chat
- Configuración global

## Estructura del Proyecto

```
demoVitalink_v2/
├── src/
│   ├── main/
│   │   ├── java/com/ceatformacion/demovitalink_v2/
│   │   │   ├── controller/          # 38 controladores
│   │   │   │   ├── Admin*           # Controladores de administración
│   │   │   │   ├── *Controller      # Controladores de vista
│   │   │   │   └── *RestController  # APIs REST
│   │   │   ├── services/            # 26 servicios
│   │   │   ├── model/               # 23 entidades JPA
│   │   │   ├── repository/          # 14 repositorios
│   │   │   ├── dto/                 # 22+ DTOs
│   │   │   ├── mapper/              # 6 mappers
│   │   │   ├── security/            # Clases de seguridad
│   │   │   ├── utils/               # Utilidades
│   │   │   ├── DemoVitalinkV2Application.java
│   │   │   ├── SecurityConfiguration.java
│   │   │   ├── WebSocketConfig.java
│   │   │   └── MvcConfiguration.java
│   │   └── resources/
│   │       ├── templates/           # 39 plantillas HTML
│   │       │   ├── *.html           # Páginas principales
│   │       │   ├── header.html      # Fragmento de cabecera
│   │       │   └── footer.html      # Fragmento de pie
│   │       ├── static/
│   │       │   ├── css/             # 2 hojas de estilo
│   │       │   ├── js/              # 33 archivos JavaScript
│   │       │   └── media/           # Imágenes, iconos, videos
│   │       └── application.properties
│   └── test/                        # Tests
├── pom.xml                          # Configuración Maven
├── Dockerfile                       # Containerización
├── mvnw, mvnw.cmd                   # Maven wrapper
└── .gitignore
```

### Controladores Principales

| Controlador | Propósito |
|-------------|-----------|
| `UsuariosController` | Registro, login, configuración de usuario |
| `CitasController` | Gestión de citas (vista) |
| `CitasRestController` | API REST de citas |
| `TratamientosController` | Gestión de tratamientos |
| `SintomasController` | Registro de síntomas |
| `ChatRestController` | API de mensajería |
| `ChatStompController` | Mensajes WebSocket |
| `RecordatoriosController` | Gestión de recordatorios |
| `EstadisticasController` | Estadísticas y métricas |
| `AdminViewController` | Panel de administración |
| `ConfigMedicoController` | Configuración médico |

### Servicios Principales

| Servicio | Responsabilidad |
|----------|-----------------|
| `CitasService` | Lógica de citas médicas |
| `ChatService` | Mensajería y conversaciones |
| `TratamientoService` | Gestión de tratamientos |
| `SintomasService` | Procesamiento de síntomas |
| `RecordatoriosService` | Creación de recordatorios |
| `EstadisticasService` | Agregación de métricas |
| `UsuariosService` | Gestión de cuentas |
| `EmailService` | Envío de notificaciones |
| `DisponibilidadService` | Horarios médicos |
| `AuditLogger` | Registro de auditoría |

## Base de Datos

### Entidades Principales

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Usuarios   │────▶│   Clientes   │     │    Citas    │
│  (id, rol)  │     │  (perfil)    │     │  (estado)   │
└──────┬──────┘     └──────────────┘     └─────────────┘
       │
       ├─────────────────┬─────────────────┬─────────────────┐
       ▼                 ▼                 ▼                 ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│Tratamientos │   │  Sintomas   │   │Recordatorios│   │Conversacion │
└─────────────┘   └─────────────┘   └─────────────┘   └─────────────┘
```

### Tablas

| Tabla | Descripción |
|-------|-------------|
| `usuarios` | Cuentas de usuario con roles |
| `clientes` | Perfiles de pacientes |
| `citas` | Citas médicas |
| `tratamientos` | Tratamientos asignados |
| `sintomas` | Síntomas registrados |
| `recordatorios` | Recordatorios programados |
| `conversacion` | Conversaciones de chat |
| `mensaje` | Mensajes individuales |
| `lectura` | Recibos de lectura |
| `config_medico` | Configuración de médicos |
| `config_notificaciones` | Preferencias de notificación |
| `config_agenda` | Configuración de calendario |
| `config_global` | Configuración del sistema |
| `disponibilidad_medica` | Horarios disponibles |

### Enumeraciones

```java
// Roles de usuario
enum Rol { ADMIN, MEDICO, USER }

// Estados de cita
enum EstadoCita { PENDIENTE, CONFIRMADA, CANCELADA, COMPLETADA }

// Estados de tratamiento
enum EstadoTratamiento { ACTIVO, COMPLETADO, SUSPENDIDO }

// Tipos de recordatorio
enum TipoRecordatorio { MEDICAMENTO, CITA, TRATAMIENTO, OTRO }

// Zonas corporales
enum ZonaCorporal { CABEZA, CUELLO, TORAX, ABDOMEN, ESPALDA,
                    BRAZOS, MANOS, PIERNAS, PIES, GENERAL }

// Canales de notificación
enum Canal { INAPP, EMAIL, SMS }

// Recurrencia
enum Repeticion { NONE, DAILY, WEEKLY, MONTHLY }
```

## API REST

### Endpoints Principales

#### Autenticación
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/inicioSesion` | Página de login |
| POST | `/login` | Procesar login |
| GET | `/logout` | Cerrar sesión |
| POST | `/registro` | Registrar nuevo usuario |

#### Citas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/citas` | Listar citas |
| POST | `/api/citas` | Crear cita |
| PUT | `/api/citas/{id}` | Actualizar cita |
| DELETE | `/api/citas/{id}` | Eliminar cita |
| PATCH | `/api/citas/{id}/estado` | Cambiar estado |

#### Tratamientos
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/tratamientos` | Listar tratamientos |
| POST | `/api/tratamientos` | Crear tratamiento |
| PUT | `/api/tratamientos/{id}` | Actualizar |
| DELETE | `/api/tratamientos/{id}` | Eliminar |

#### Síntomas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/sintomas` | Listar síntomas |
| POST | `/api/sintomas` | Registrar síntoma |
| GET | `/api/sintomas/{id}` | Obtener detalle |

#### Chat
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/chat/conversaciones` | Listar conversaciones |
| POST | `/api/chat/conversaciones` | Nueva conversación |
| GET | `/api/chat/mensajes/{id}` | Mensajes de conversación |
| POST | `/api/chat/mensajes` | Enviar mensaje |

#### Recordatorios
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/recordatorios` | Listar recordatorios |
| POST | `/api/recordatorios` | Crear recordatorio |
| PUT | `/api/recordatorios/{id}` | Actualizar |
| DELETE | `/api/recordatorios/{id}` | Eliminar |

#### Estadísticas
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/estadisticas` | Métricas del usuario |
| GET | `/api/admin/estadisticas` | KPIs de administración |

#### Administración
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/usuarios` | Listar usuarios |
| POST | `/api/admin/usuarios` | Crear usuario |
| PUT | `/api/admin/usuarios/{id}` | Actualizar usuario |
| DELETE | `/api/admin/usuarios/{id}` | Eliminar usuario |
| GET | `/api/admin/clientes` | Listar clientes |

### WebSocket Endpoints

| Endpoint | Propósito |
|----------|-----------|
| `/ws-chat` | Conexión WebSocket principal |
| `/app/chat.send` | Enviar mensaje |
| `/topic/messages/{id}` | Suscripción a conversación |

## Despliegue

### Docker

El proyecto incluye un `Dockerfile` para despliegue containerizado:

```bash
# Construir imagen
docker build -t vitalink-v2 .

# Ejecutar contenedor
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:mysql://host:3306/vitalink \
  -e DB_USER=user \
  -e DB_PASSWORD=password \
  -e EMAIL_USERNAME=email@gmail.com \
  -e EMAIL_PASSWORD=app_password \
  --name vitalink \
  vitalink-v2
```

### Variables de Entorno para Producción

| Variable | Descripción | Requerida |
|----------|-------------|-----------|
| `DATABASE_URL` | URL de conexión JDBC | Sí |
| `DB_USER` | Usuario de base de datos | Sí |
| `DB_PASSWORD` | Contraseña de base de datos | Sí |
| `EMAIL_USERNAME` | Email para notificaciones | Sí |
| `EMAIL_PASSWORD` | App password de Gmail | Sí |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring (prod) | No |

### Despliegue en Railway

El proyecto está configurado para Railway.app:

1. Conectar repositorio a Railway
2. Configurar variables de entorno
3. Railway detectará el `Dockerfile` automáticamente

## Seguridad

### Características de Seguridad

- **Autenticación**: Spring Security 6 con formulario de login
- **Autorización**: RBAC (Role-Based Access Control)
- **Contraseñas**: BCrypt + soporte para encoding legacy
- **CSRF**: Protección habilitada (excepto APIs)
- **CORS**: Configurado para endpoints de API
- **WebSocket**: Endpoints protegidos

### Rutas Públicas

- `/` - Página principal
- `/inicioSesion` - Login
- `/registro*` - Registro
- `/servicios*` - Información de servicios
- `/contacto` - Formulario de contacto
- `/politica*`, `/termino*`, `/baseLegal` - Páginas legales
- `/css/**`, `/js/**`, `/media/**` - Recursos estáticos

### Rutas Protegidas por Rol

```java
// Solo ADMIN
/admin/**, /api/admin/**

// Solo MEDICO
/medico/**, /api/medico/**

// Solo USER
/usuario/**, /api/usuario/**

// ADMIN o MEDICO
/pacientes/**, /tratamientos/crear/**
```

## Contribución

### Estándares de Código

- **Idioma**: Nombres de entidades y variables en español
- **Framework**: Seguir convenciones de Spring Boot
- **DTOs**: Usar DTOs para transferencia de datos
- **Servicios**: Lógica de negocio en capa de servicios
- **Controladores**: Solo orquestación, no lógica

### Flujo de Trabajo

1. Crear rama desde `main`
2. Implementar cambios
3. Escribir/actualizar tests
4. Crear Pull Request
5. Code review
6. Merge a `main`

### Commits

Usar mensajes descriptivos:
- `feat:` Nueva funcionalidad
- `fix:` Corrección de bug
- `docs:` Documentación
- `style:` Formateo
- `refactor:` Refactorización
- `test:` Tests

## Soporte

Para reportar problemas o sugerir mejoras, crear un Issue en el repositorio.

---

**VitaLink v2** - Conectando salud y tecnología

*Desarrollado por CEAT Formación*
