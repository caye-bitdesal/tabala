# Decisiones de implementación

Fuente de producto: [especificacion-tecnica.md](./especificacion-tecnica.md) (v1.3). Cuando la spec no detalla algo, se elige la opción más simple que cumpla los requisitos y se anota aquí.

## Módulos y paquete (§3.0 de la spec)

| Spec genérico | Tabala |
|---|---|
| `shared/` | `:core` |
| UI Compose | `:app:shared` |
| `webApp/` | `:app:webApp` |
| `server/` | `:server` |

Paquete: `com.bitdesal.tabala`. Android/Desktop fuera de v1.

## Persistencia

SQLDelight + Flyway (no Exposed).

## Autenticación

Google Sign-In + sesiones propias. Sin Firebase.

## Esquema

Sustituir V1 (0.1) por v0.2 antes de producción si no hay datos reales.

## RGPD (acuerdos con la junta)

1. **`consent.dataProcessing`:** setup inicial justo después del primer login; sin ello, `403 consent_required` en el resto de la API (ver spec §5.4).
2. **`users.notes`:** visibles en `GET /me`, export propio y para junta/admin en ficha (spec §5.4).
3. **Menores en organigrama privado:** cualquier `member` autenticado puede ver titulares menores; nunca en `/public/*`.
4. **Menor con cuenta:** `/me` para el menor; ficha completa solo board/admin; tutores member sin acceso especial.