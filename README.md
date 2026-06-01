# 🏪 Mercaditosh — Backend API

> Sistema de Gestión de Mercados Públicos de la CDMX  
> **ESCOM — IPN | Grupo 4BM2**

---

## 🚀 Estado Actual del Backend

El backend está **desplegado y en vivo en producción**. No necesitas correr nada localmente para consumirlo desde Angular.

| Dato | Valor |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 |
| Base de datos | YugabyteDB Cloud (PostgreSQL-compatible) |
| Infraestructura | Docker + Render |
| **URL de producción** | `🔲 _pendiente de agregar_` |
| **Swagger UI** | `🔲 _pendiente de agregar_/swagger-ui/index.html` |

> 💡 **Para el frontend:** Antes de escribir una sola línea de Angular, abre Swagger UI en la URL de arriba. Desde ahí puedes probar cada endpoint en vivo, ver los contratos de request/response y confirmar que la API responde correctamente.

---

## 🔧 Cambios y Correcciones Clave

Esta sección documenta las correcciones técnicas recientes que cerraron bugs críticos. Te las explico con contexto para que entiendas por qué la API ahora se comporta diferente a como estaba originalmente especificada.

---

### 1. Archivos binarios: de `@Lob` a `bytea`

**¿Qué pasaba?**

Al arrancar el backend contra YugabyteDB aparecía este error que rompía toda la aplicación:

```
wrong column type encountered in column [datos] in table [archivo];
found [bytea], but expecting [oid]
```

**¿Por qué?**

La anotación `@Lob` de Hibernate tiene un comportamiento diferente según el motor de base de datos:

- En **MySQL** → mapea a `LONGBLOB` ✅
- En **PostgreSQL / YugabyteDB** → mapea a `oid` (Object Identifier), un tipo legado ❌

El script SQL del proyecto crea la columna como `bytea` (Byte Array nativo de PostgreSQL), por lo que Hibernate y la BD estaban en conflicto.

**Solución aplicada en `Archivo.java`:**

```java
// ❌ ANTES — causaba el conflicto de tipos
@Lob
@Column(name = "datos", nullable = false)
private byte[] datos;

// ✅ AHORA — fuerza el tipo nativo de PostgreSQL/YugabyteDB
@Column(name = "datos", nullable = false, columnDefinition = "bytea")
private byte[] datos;
```

**Impacto para Angular:** Ninguno directo. La subida y descarga de archivos funcionan igual desde el cliente. Este fue un fix interno de la capa de persistencia.

---

### 2. Consultas JPQL de metadatos: eliminación de `nativeQuery = true`

**¿Qué pasaba?**

El backend no arrancaba — el contexto de Spring fallaba al validar los repositorios:

```
Validation failed for query for method [...] findAllMetadatos()
```

**¿Por qué?**

En `ArchivoRepository` los métodos `findAllMetadatos()` y `findMetadatosByIdLocal()` usaban simultáneamente:

- Una expresión de constructor JPQL: `SELECT new com.ipn.mx.mercaditosh...RespuestaDTO(...)`
- La propiedad `nativeQuery = true`

Estas dos cosas son **incompatibles**. Las queries nativas (`nativeQuery = true`) hablan SQL puro del motor de BD — no conocen clases Java ni DTOs. Al mezclarlas, Spring no podía compilar las consultas.

**Solución aplicada en `ArchivoRepository.java`:**

```java
// ❌ ANTES — mezcla inválida de JPQL con nativeQuery
@Query(value = "SELECT new com.ipn.mx...RespuestaDTO(...) FROM Archivo a",
       nativeQuery = true)  // ← esto rompe todo
List<RespuestaDTO> findAllMetadatos();

// ✅ AHORA — JPQL puro, sin nativeQuery
@Query("SELECT new com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO(" +
       "a.idArchivo, a.nombreArchivo, a.tipoContenido, a.fechaSubida, ...) " +
       "FROM Archivo a")
List<RespuestaDTO> findAllMetadatos();
```

**Impacto para Angular:** El endpoint `GET /api/archivos` ahora funciona correctamente y devuelve la lista ligera de metadatos sin los bytes.

---

### 3. Filtro de locales: convención de nombres en Spring Data JPA

**¿Qué pasaba?**

```
Could not create query for public abstract List findByMercadoYEstado(...)
```

**¿Por qué?**

Spring Data JPA genera SQL automáticamente a partir del nombre del método usando palabras clave en **inglés**: `findByFieldAnd`, `findByFieldOr`, etc. El conector en español `Y` no es una palabra clave reconocida — Spring intentaba buscar una columna llamada `mercadoYEstado` que no existe en la BD.

**Solución aplicada en `LocalComercialRepository.java`:**

```java
// ❌ ANTES — Spring no entiende "Y" como conector lógico
List<LocalComercial> findByMercadoYEstado(Integer idMercado, String estado);

// ✅ AHORA — @Query explícita que preserva el nombre del método
@Query("SELECT l FROM LocalComercial l " +
       "WHERE l.mercado.idMercado = :idMercado AND l.estado = :estado")
List<LocalComercial> findByMercadoYEstado(
        @Param("idMercado") Integer idMercado,
        @Param("estado") String estado);
```

**Impacto para Angular:** El endpoint `GET /api/locales/mercado/{idMercado}/estado/{estado}` ya funciona correctamente. Ver la sección de integración más abajo.

---

## 📦 Guía de Integración para el Frontend (Angular)

Esta es la parte que más te importa. Aquí está todo lo que necesitas saber para consumir la API desde Angular.

---

### Configuración base — `environment.ts`

Define la URL base una sola vez para no repetirla en cada servicio:

```typescript
// src/environments/environment.ts  (desarrollo local apuntando a Render)
export const environment = {
  production: false,
  apiUrl: 'https://TU-URL-EN-RENDER.onrender.com/api'
};

// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: 'https://TU-URL-EN-RENDER.onrender.com/api'
};
```

---

### 📁 Módulo de Archivos — Listado ligero con `RespuestaDTO`

**¿Por qué no pedimos directamente la entidad completa?**

Cada archivo puede pesar varios MB. Si el endpoint de listado devolviera el `byte[]` de cada archivo serializado como Base64, una lista de 20 archivos podría devolver **cientos de MB de JSON** — congelando el navegador del usuario.

La solución es un endpoint de **metadatos ligeros** que devuelve solo los datos descriptivos:

**`GET /api/archivos`**

```json
[
  {
    "idArchivo": 1,
    "nombreArchivo": "contrato_local_5.pdf",
    "tipoContenido": "application/pdf",
    "fechaSubida": "2025-06-01T10:30:00",
    "idLocal": 3,
    "urlDescarga": "/api/archivos/1/descargar"
  },
  {
    "idArchivo": 2,
    "nombreArchivo": "foto_mercado.jpg",
    "tipoContenido": "image/jpeg",
    "fechaSubida": "2025-06-02T09:15:00",
    "idLocal": null,
    "urlDescarga": "/api/archivos/2/descargar"
  }
]
```

**Interfaz TypeScript recomendada:**

```typescript
// src/app/models/archivo.model.ts
export interface ArchivoMetadata {
  idArchivo: number;
  nombreArchivo: string;
  tipoContenido: string;
  fechaSubida: string;     // ISO 8601 — usa new Date(fechaSubida) para parsear
  idLocal: number | null;
  urlDescarga: string;
}
```

**Servicio Angular:**

```typescript
// src/app/services/archivo.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ArchivoMetadata } from '../models/archivo.model';

@Injectable({ providedIn: 'root' })
export class ArchivoService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listar(): Observable<ArchivoMetadata[]> {
    return this.http.get<ArchivoMetadata[]>(`${this.base}/archivos`);
  }

  listarPorLocal(idLocal: number): Observable<ArchivoMetadata[]> {
    return this.http.get<ArchivoMetadata[]>(`${this.base}/archivos/local/${idLocal}`);
  }

  subir(file: File, idLocal?: number): Observable<ArchivoMetadata> {
    const formData = new FormData();
    formData.append('archivo', file);           // nombre del campo: 'archivo'
    const url = idLocal
      ? `${this.base}/archivos?idLocal=${idLocal}`
      : `${this.base}/archivos`;
    return this.http.post<ArchivoMetadata>(url, formData);
    // No pongas Content-Type — el navegador lo setea automáticamente
    // con el boundary correcto para multipart/form-data
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/archivos/${id}`);
  }
}
```

---

### ⬇️ Descarga y renderizado de archivos binarios

Usa la propiedad `urlDescarga` que ya viene en cada objeto del listado. Hay dos casos:

**Caso A — Abrir/previsualizar en una nueva pestaña del navegador** (PDF e imágenes):

```typescript
// El backend devuelve Content-Disposition: inline
// El navegador decide si muestra o descarga según el MIME type
abrirArchivo(archivo: ArchivoMetadata): void {
  const urlCompleta = `https://TU-URL-EN-RENDER.onrender.com${archivo.urlDescarga}`;
  window.open(urlCompleta, '_blank');
}
```

**Caso B — Forzar descarga con nombre de archivo correcto** (para el usuario):

```typescript
import { HttpClient } from '@angular/common/http';

descargarArchivo(archivo: ArchivoMetadata): void {
  const urlCompleta = `https://TU-URL-EN-RENDER.onrender.com${archivo.urlDescarga}`;

  this.http.get(urlCompleta, { responseType: 'blob' }).subscribe(blob => {
    const objectUrl = URL.createObjectURL(blob);
    const link      = document.createElement('a');
    link.href       = objectUrl;
    link.download   = archivo.nombreArchivo;  // nombre visible en la descarga
    link.click();
    URL.revokeObjectURL(objectUrl);           // liberar memoria
  });
}
```

**Template de ejemplo:**

```html
<!-- archivo-lista.component.html -->
<table>
  <tr *ngFor="let archivo of archivos">
    <td>{{ archivo.nombreArchivo }}</td>
    <td>{{ archivo.tipoContenido }}</td>
    <td>{{ archivo.fechaSubida | date:'dd/MM/yyyy HH:mm' }}</td>
    <td>
      <button (click)="abrirArchivo(archivo)">👁 Ver</button>
      <button (click)="descargarArchivo(archivo)">⬇ Descargar</button>
    </td>
  </tr>
</table>
```

---

### 🗂️ Filtrar locales por Mercado y Estado

**Endpoints disponibles:**

| URL | Descripción |
|---|---|
| `GET /api/locales/mercado/{idMercado}` | Todos los locales de un mercado |
| `GET /api/locales/estado/{estado}` | Todos los locales con un estado |
| `GET /api/locales/mercado/{idMercado}/estado/{estado}` | Locales de un mercado filtrados por estado |

**Valores válidos para `estado`:** `ocupado` · `disponible` · `en_mantenimiento`

**Servicio Angular:**

```typescript
// src/app/services/local.service.ts
@Injectable({ providedIn: 'root' })
export class LocalService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  listarPorMercado(idMercado: number): Observable<LocalComercial[]> {
    return this.http.get<LocalComercial[]>(
      `${this.base}/locales/mercado/${idMercado}`
    );
  }

  filtrarPorMercadoYEstado(
    idMercado: number,
    estado: 'ocupado' | 'disponible' | 'en_mantenimiento'
  ): Observable<LocalComercial[]> {
    return this.http.get<LocalComercial[]>(
      `${this.base}/locales/mercado/${idMercado}/estado/${estado}`
    );
  }
}
```

**Ejemplo de uso en componente:**

```typescript
// Al cargar el componente: todos los locales del mercado 1
this.localService.listarPorMercado(1).subscribe(locales => {
  this.locales = locales;
});

// Al cambiar el filtro del select en el template:
onEstadoChange(estado: string): void {
  this.localService
    .filtrarPorMercadoYEstado(this.idMercadoSeleccionado, estado as any)
    .subscribe(locales => this.locales = locales);
}
```

---

### 📬 Manejo de errores HTTP

El backend devuelve errores en un formato JSON consistente gracias al `GlobalExceptionHandler`. Intercepta estos errores globalmente en Angular:

```typescript
// src/app/interceptors/error.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError(err => {
      // Estructura garantizada por GlobalExceptionHandler del backend:
      // { timestamp: string, status: number, mensaje: string, errores?: object }
      const mensaje = err.error?.mensaje ?? 'Error inesperado del servidor';
      console.error(`[${err.status}] ${mensaje}`);
      // Aquí puedes mostrar un toast/snackbar con el mensaje
      return throwError(() => err);
    })
  );
};
```

---

## 🐳 Infraestructura Docker

Si necesitas correr el backend **localmente con Docker** en lugar de consumir la URL de Render, aquí está el `Dockerfile` del proyecto. Está en la raíz del repositorio (al mismo nivel que `pom.xml`).

```dockerfile
# ─── Etapa 1: Compilación ───────────────────────────────────────────
# Imagen pesada con Maven + JDK 21 — solo se usa para construir el JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar el caché de capas Docker:
# Si el código cambia pero las dependencias no, Docker no re-descarga todo Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ─── Etapa 2: Imagen de producción ──────────────────────────────────
# Imagen ligera solo con el JRE 21 (sin Maven ni JDK completo)
# Esto reduce el tamaño de la imagen final de ~700 MB a ~200 MB
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos solo el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone la app (debe coincidir con server.port o la var PORT de Render)
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Para correrlo localmente contra YugabyteDB Cloud:**

```bash
# 1. Construir la imagen
docker build -t mercaditosh-backend .

# 2. Correr pasando las variables de entorno de producción
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL="jdbc:postgresql://<host>:5433/yugabyte?sslmode=require" \
  -e DB_USER="admin" \
  -e DB_PASSWORD="tu_password" \
  -e MAIL_USERNAME="tu_correo@gmail.com" \
  -e MAIL_PASSWORD="tu_app_password" \
  mercaditosh-backend

# 3. Verificar que levantó
curl http://localhost:8080/swagger-ui/index.html
```

> ⚠️ **Importante para Render:** El `Dockerfile` debe estar en la raíz del repo. En el panel de Render deja el campo **Root Directory** vacío y **Dockerfile Path** como `Dockerfile` (sin rutas). Si lo pones en una subcarpeta, Render no puede leerlo correctamente.

---

## 🗺️ Mapa completo de endpoints

Para referencia rápida, estos son todos los módulos disponibles:

| Módulo | Base URL | Descripción |
|---|---|---|
| Mercados | `/api/mercados` | CRUD + filtro por alcaldía |
| Locales | `/api/locales` | CRUD + filtro por mercado y estado |
| Locatarios | `/api/locatarios` | CRUD + búsqueda por nombre y fecha |
| Productos | `/api/productos` | CRUD + filtro por local, mercado y categoría |
| Pagos | `/api/pagos` | CRUD + totales y filtros por rango de fecha |
| Inspecciones | `/api/inspecciones` | CRUD + alertas sanitarias y resumen por mercado |
| Archivos | `/api/archivos` | Subida/descarga + listado de metadatos |

> 📖 **Consulta Swagger UI** para ver los parámetros exactos, los body de cada endpoint y probarlos en vivo sin necesidad de Insomnia o Postman.

---

*Mercaditosh — ESCOM IPN · Grupo 4BM2*  
*Arellano Acosta Ixchel · Rivas Martinez Job Santiago*
