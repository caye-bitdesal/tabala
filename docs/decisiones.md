# Decisiones de implementación

Fuente de producto: [especificacion-tecnica.md](./especificacion-tecnica.md) (v1.3). Cuando la spec no detalla algo o se acuerda un ajuste concreto con producto, se anota aquí.

## Módulos y paquete (§3.0 de la spec)

| Spec genérico | Tabala |
|---|---|
| `shared/` (DTOs, validación, i18n, `MuixerangaApi`) | `:core` |
| UI Compose, ViewModels, repositorios, Koin | `:app:shared` |
| `webApp/` (`wasmJs` + `index.html`) | `:app:webApp` |
| `server/` | `:server` |

- Paquete base: `com.bitdesal.tabala`.
- Clientes no web (`:app:androidApp`, `:app:desktopApp`, target `js`) quedan **fuera de v1** y se especificarán/implementarán por separado en el futuro.

## Persistencia

- **SQLDelight + Flyway** (no Exposed).
- Sustituir `V1__initial_schema.sqm` (0.1) por esquema **v0.2** (`V1` + `V2__seed.sqm`) antes de producción (greenfield, sin datos reales).
- Pool SQLite (NFR-03): 1 conexión escritora dedicada (transacciones) + conexiones de lectura concurrentes en modo WAL con `foreign_keys = ON` y `busy_timeout = 5000`.
- Posiciones de figuras (§5.10): fijar en SQLite `UNIQUE (figure_id, x, y, z)` y `CHECK (x BETWEEN -20 AND 20 AND y BETWEEN -20 AND 20 AND z BETWEEN 0 AND 8)`.

## Autenticación

- **Google Sign-In (OpenID Connect) + sesiones propias** (HS256 JWT 15 min + refresh opaco rotatorio 30 días). **Sin Firebase** ni contraseñas.
- Desarrollo local con cookies `SameSite=Strict`: usar `http://localhost` tanto para la webapp como para la API (no mezclar `localhost` y `127.0.0.1`) y omitir el flag `Secure` cuando `PUBLIC_BASE_URL` sea `http://`.
- `POST /api/v1/auth/logout` acepta Bearer o cookie `mux_refresh` (idempotente) para poder cerrar sesión y limpiar la cookie incluso si el JWT de 15 min ya caducó.

## Contrato API y `:core`

- **Dinero (`money`):** no usar `Double` en `:core`; usar un tipo valor `MoneyCents(val cents: Long)` con serializer custom que emite/lee números JSON con 2 decimales en euros (p. ej. `450.00`).
- **JSON Merge Patch (RFC 7386):** en DTOs `*Patch`, usar `PatchField<T>` (`Absent` | `Null` | `Value(T)`) para distinguir entre campo omitido (no tocar) y `null` explícito (borrar valor).
- **`MemberStatus`:** sustituir `former` por `deleted` (`active`, `inactive`, `on_leave`, `deleted`; etiqueta valenciana `"Eliminat"`).
- **`POST /api/v1/contracts/{id}/create-event`:** si `contract.performanceDate` es `null`, exige `date` (y opcionalmente `endDate`/`allDay`) en el cuerpo JSON; si falta → `422 validation_failed`.
- **`POST /api/v1/documents`:** `multipart/form-data` con campos de formulario sueltos (`title`, `kind`, `date`, `eventId`, `userId`, `notes`) + parte `file` (más sencillo para Ktor Client y futuros clientes Android).
- **`PUT /api/v1/figures/{id}/positions`:** exige `version` de la figura para concurrencia optimista (`409 version_conflict` si difiere).
- **Miniaturas de ficheros (`FileRef.thumbUrl`):** ruta dedicada `GET /api/v1/files/{id}/thumb/{filename}` con las mismas reglas de visibilidad que `GET /api/v1/files/{id}/{filename}`. En la webapp, los ficheros protegidos (avatares privados, fotos de inventario, documentos) se descargan vía Ktor Client con `Authorization: Bearer` (a memoria / `Blob`).
- **Swagger UI (`GET /api/v1/docs`):** abierto públicamente por defecto como documentación estática (con "Try it out" deshabilitado o sin consultar datos reales sin Bearer propio; `ENABLE_API_DOCS` permite apagarlo si se desea).

## RGPD y ciclo de vida de miembros

1. **`consent.dataProcessing`:** setup inicial justo después del primer login; sin ello, `403 consent_required` en el resto de la API (allowlist: `GET/PATCH /me`, `GET /me/export`, `GET/DELETE /me/sessions/*`, `POST /auth/logout`, `POST /auth/logout-all`).
2. **`GET /me` y `GET /me/export`:** en `GET /me`, la propia persona puede ver `emergencyContact`, `isMinor`, `notes` y (si es menor) `guardians`. `GET /me/export` incluye además la lista/metadatos de `documents` vinculados (`userId = me`, p. ej. consentimiento firmado).
3. **Menores en organigrama privado:** cualquier `member` autenticado puede ver titulares menores; nunca en `/public/*`.
4. **Menor con cuenta:** `/me` para el menor; ficha completa solo `board`/`admin`; tutores `member` sin acceso especial.
5. **Baja (`DELETE /users/{id}`):** marca `status = deleted`, quita a la persona de `org_role_holders` y desactiva automáticamente su cuenta (`account.status = disabled` + revoca todas sus sesiones), conservando datos personales e históricos por si vuelve.
6. **Anonimización (`POST /users/{id}/anonymize`):** borra datos identificables (`name = "Persona eliminada"`, `status = deleted`, elimina cuenta, vínculo Google, sesiones, tutores y avatar, quita de `org_role_holders`, pone `userId = null` en `positions`), pero mantiene el registro `User`, sus `documents` y sus `attendance` para preservar el histórico.

## Navegación y división de slices de configuración

- **`/configuracio`:** visible también para `technical` (que tiene `achievements:write`), mostrando la gestión de Logros y ocultando/bloqueando la edición de Grupos (`groups:write` / `config:write`, solo `board`/`admin`).
- **`spec-groups-achievements` vs `spec-configuracio`:** `spec-groups-achievements` implementa **todo el backend** (CRUD completo de `groups` y `achievements`, colores `#RRGGBB`, redes sociales, logos/imágenes, campos calculados y `/public/*`); `spec-configuracio` implementa **toda la UI web** de `/configuracio`.

## Diseño visual oficial y ajustes de modelo v0.2 (desde mockups)

- **Diseño visual oficial (`platform-web-shell` y pantallas v1):** prevalece el diseño de los mockups de referencia sobre las notas iniciales de §7.2–§7.3:
  - Barra superior **roja (`#BB2029`)** con escudo + «Muixeranga d'Elx / Gestió», pestañas con icono+texto en el centro (pestaña activa en **amarillo `#FFC432`** con línea inferior amarilla) y pastilla de usuario azul marino (`#14005E`) a la derecha con menú desplegable (siempre por encima del contenido).
  - Cabecera de sección con *kicker* rojo `GESTIÓ INTERNA`, título negro, subtítulo gris y botón principal rojo `+ ...` a la derecha.
  - **Tira de tarjetas KPI clicables** (en contenedor gris claro con tarjetas blancas; tarjeta activa con fondo crema suave y borde/barra superior azul marino `#14005E`; alertas como `Atrasada`/`Estoc baix`/`Vacants` en crema suave y cifra roja) que actúan como filtros rápidos.
  - **Edición en panel lateral derecho (*side sheet*)** para Membres, Contractacions, Organigrama y «Tots els pròxims esdeveniments»; **modales centrados** para altas (`Nova ...`) y acciones rápidas (`Registra venda`, `Informació de comanda`, `Edita l'article`, `Edita l'esdeveniment`).
  - **Móvil:** barra superior roja compacta (escudo + avatar `TP` a la izquierda, sección centrada), tira KPI con scroll horizontal, FAB rojo `+` abajo a la derecha y barra inferior blanca de 4 pestañas (`Calendari`, `Contractacions`, `Membres`, `Més`) con indicador superior rojo.
- **Inventari (`inventory_items` + ventas):** se añaden al esquema v0.2 las columnas de reposición/proveedor (`supplier_contact`, `supplier_reference`, `min_order_qty`, `lead_time`, `last_ordered_at`) y contadores acumulados de venta (`units_sold`, `revenue_cents`), junto con el endpoint atómico `POST /api/v1/inventory-items/{id}/sell` (decrementa stock de la variante elegida y suma `units_sold` e `revenue_cents` en una transacción).
- **Idioma de interfaz en usuario (`users.preferred_locale`):** se añade `preferred_locale TEXT NOT NULL DEFAULT 'ca' CHECK (preferred_locale IN ('ca', 'es'))` editable por la propia persona en `PATCH /me` («El meu perfil») y por `board`/`admin` en `PATCH /users/{id}`.
- **Frecuencia de reunión en áreas (`areas.meeting_frequency`):** se conserva la columna opcional `meeting_frequency IN ('weekly', 'biweekly', 'monthly', 'as_needed')` en `areas` para soportar el campo «Freqüència de reunió» del panel «Edita l'àrea».

