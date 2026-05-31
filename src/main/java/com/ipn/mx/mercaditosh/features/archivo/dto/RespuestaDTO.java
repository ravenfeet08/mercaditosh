package com.ipn.mx.mercaditosh.features.archivo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para operaciones sobre Archivo.
 *
 * Por qué existe este DTO:
 * Si devolviéramos la entidad Archivo directamente en el listado,
 * el campo "datos" (byte[]) se incluiría en cada elemento de la lista
 * serializado como Base64 — esto puede devolver megabytes de datos
 * innecesarios cuando el cliente solo quiere ver el nombre y la fecha.
 *
 * Este DTO excluye el campo "datos" y se usa en:
 *   - GET /api/archivos          (listar)
 *   - POST /api/archivos         (confirmar subida)
 *   - GET /api/archivos/{id}/info (metadatos)
 *
 * Para descargar el contenido binario real se usa un endpoint dedicado:
 *   GET /api/archivos/{id}/descargar  → devuelve ResponseEntity<byte[]>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDTO {

    private Integer idArchivo;
    private String nombreArchivo;
    private String tipoContenido;
    private LocalDateTime fechaSubida;
    private Integer idLocal;        // null si el archivo no está vinculado a un local
    private String urlDescarga;     // /api/archivos/{id}/descargar
}
