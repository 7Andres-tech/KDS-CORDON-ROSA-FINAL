# KDS El Cordón y la Rosa

Sistema KDS desarrollado para el restaurante **El Cordón y la Rosa**, orientado a la gestión digital de pedidos entre caja, cocina y administración.

El proyecto permite registrar pedidos desde caja, enviarlos a cocina, controlar estados de preparación, confirmar pagos mediante Mercado Pago, generar reportes PDF y enviarlos por WhatsApp al administrador.

---

## Arquitectura del sistema

El sistema fue desplegado usando una arquitectura distribuida con tres servicios principales:

```text
Frontend Angular        → Vercel
Backend principal       → Render
Microservicio de pagos  → Railway
Base de datos           → PostgreSQL en Render
