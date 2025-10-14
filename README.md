# 🌤️ Weather Microservice

Microservicio en Kotlin + Spring Boot que consulta el clima de Buenos Aires cada hora y expone la información mediante endpoints REST.

## 🚀 Características

- ✅ Consulta automática a OpenWeather API cada hora
- ✅ Almacenamiento en PostgreSQL con validación de duplicados
- ✅ Timezone configurado para Buenos Aires
- ✅ Endpoints REST para consultar histórico
- ✅ Dockerizado con Docker Compose
- ✅ Manejo robusto de errores

## 📋 Requisitos

- Docker y Docker Compose
- Cuenta en OpenWeather (API key gratuita)

## 🔧 Instalación

### 1. Clonar el repositorio

```bash
git clone 
cd weather-microservice
```

### 2. Configurar variables de entorno

Copia el archivo de ejemplo y edítalo con tus credenciales:

```bash
cp .env.example .env
```

Edita `.env` y agrega tu API key de OpenWeather:

```env
OPENWEATHER_API_KEY=tu_api_key_aqui
```

**Obtén tu API key gratuita en:** https://openweathermap.org/api

### 3. Levantar servicios con Docker Compose

```bash
docker-compose up --build
```

El microservicio estará disponible en `http://localhost:8080`

## 📡 Endpoints

### Health Check
```bash
GET http://localhost:8080/health
```

### Obtener último registro
```bash
GET http://localhost:8080/weather/latest
```

**Respuesta:**
```json
{
  "timestamp": "2025-10-14T15:00:00",
  "temperature": 22.5,
  "humidity": 65,
  "isRaining": false
}
```

### Obtener histórico
```bash
GET http://localhost:8080/weather?from=2025-10-14T00:00:00&to=2025-10-14T23:59:59
```

**Respuesta:**
```json
[
  {
    "timestamp": "2025-10-14T09:00:00",
    "temperature": 18.3,
    "humidity": 72,
    "isRaining": false
  },
  {
    "timestamp": "2025-10-14T10:00:00",
    "temperature": 20.1,
    "humidity": 68,
    "isRaining": true
  }
]
```

## 🔍 Ejemplos con cURL

```bash
# Health check
curl http://localhost:8080/health

# Último registro
curl http://localhost:8080/weather/latest

# Histórico del día
curl "http://localhost:8080/weather?from=2025-10-14T00:00:00&to=2025-10-14T23:59:59"
```

## 🗄️ Base de Datos

El microservicio crea automáticamente la tabla `weather_hourly`:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | ID autogenerado |
| timestamp | TIMESTAMP | Fecha y hora del registro |
| temperature | NUMERIC(5,2) | Temperatura en °C |
| humidity | INTEGER | Humedad en % |
| is_raining | BOOLEAN | Si está lloviendo |

## ⚙️ Configuración del Scheduler

El scheduler ejecuta la consulta cada hora en punto (cron: `0 0 * * * *`).

Para modificar la frecuencia, edita `WeatherScheduler.kt`:

```kotlin
@Scheduled(cron = "0 0 * * * *") // Formato: segundo minuto hora día mes día-semana
```

## 🛠️ Desarrollo Local (sin Docker)

Si prefieres ejecutar sin Docker:

### 1. Instalar PostgreSQL localmente

### 2. Crear base de datos

```sql
CREATE DATABASE weatherdb;
```

### 3. Configurar variables de entorno

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=weatherdb
export DB_USER=postgres
export DB_PASSWORD=tu_password
export OPENWEATHER_API_KEY=tu_api_key
```

### 4. Ejecutar con Gradle

```bash
./gradlew bootRun
```

## 📊 Logs

El microservicio genera logs informativos sobre:
- Consultas a OpenWeather
- Registros guardados en DB
- Errores de conexión
- Duplicados detectados

Ver logs del contenedor:
```bash
docker-compose logs -f weather-service
```

## 🧪 Testing

```bash
./gradlew test
```

## 🐛 Troubleshooting

### Error: "Cannot connect to OpenWeather"
- Verifica que tu API key sea correcta
- Confirma que tengas conexión a internet
- Revisa los límites de tu plan gratuito (60 llamadas/minuto)

### Error: "Connection refused to PostgreSQL"
- Espera a que PostgreSQL esté completamente iniciado
- Verifica con: `docker-compose ps`

### No se guardan registros
- Revisa los logs: `docker-compose logs weather-service`
- Verifica que el scheduler esté activo

## 📝 Licencia

MIT License

## 👨‍💻 Autor

Tu nombre - [tu@email.com]
```

---

## 🎯 Instrucciones de Uso Rápido

1. **Obtén tu API key de OpenWeather** (gratuita):
   - Ve a https://openweathermap.org/api
   - Regístrate y copia tu API key

2. **Crea el archivo .env**:
   ```bash
   cp .env.example .env
   # Edita .env y pega tu API key
   ```

3. **Levanta todo con Docker**:
   ```bash
   docker-compose up --build
   ```

4. **Prueba los endpoints**:
   ```bash
   curl http://localhost:8080/health
   curl http://localhost:8080/weather/latest