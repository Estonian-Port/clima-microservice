# 🌤️ Clima Microservice

Microservicio en **Go** que consulta la API de **OpenWeather**, almacena datos climáticos en **PostgreSQL** y expone un endpoint HTTP para obtener el último registro.

Diseñado para ejecutarse **sin cambios** en local, Docker y Railway.

---

## 🚀 Características

- Consulta automática **cada hora en punto**
- Timezone explícito: **America/Argentina/Buenos_Aires**
- Guarda temperatura, humedad y estado climático
- Estados climáticos mapeados a español
- API REST simple
- Docker image liviana (~6 MB)
- Variables de entorno unificadas

---

## 📦 Stack

- Go 1.21
- PostgreSQL
- OpenWeather API
- Docker
- Railway

---

## 🔌 Endpoint

### `GET /clima/latest`

```json
{
  "id": 1,
  "timestamp": "2026-01-08T22:00:00-03:00",
  "temperatura": 24.3,
  "humedad": 65,
  "estado_clima": "SOLEADO"
}
