package com.ipn.mx.mercaditosh.features.archivo.controller;

import com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO;
import com.ipn.mx.mercaditosh.features.archivo.service.ArchivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Tag(name = "Archivos", description = "Subida, descarga y gestión de archivos adjuntos")
public class ArchivoController {

    private final ArchivoService archivoService;

    // ---------------------------------------------------------------
    // GET /api/archivos
    // Lista metadatos de todos los archivos (sin los bytes)
    // ---------------------------------------------------------------
    @GetMapping
    @Operation(summary = "Listar metadatos de todos los archivos")
    public ResponseEntity<List<RespuestaDTO>> listar() {
        return ResponseEntity.ok(archivoService.listarMetadatos());
    }

    // ---------------------------------------------------------------
    // GET /api/archivos/local/{idLocal}
    // ---------------------------------------------------------------
    @GetMapping("/local/{idLocal}")
    @Operation(summary = "Listar archivos de un local específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<List<RespuestaDTO>> porLocal(
            @PathVariable Integer idLocal) {
        return ResponseEntity.ok(archivoService.listarMetadatosPorLocal(idLocal));
    }

    // ---------------------------------------------------------------
    // GET /api/archivos/{id}/descargar
    // Devuelve el archivo binario con el Content-Type correcto
    // y el header Content-Disposition para que el navegador lo descargue
    // ---------------------------------------------------------------
    @GetMapping("/{id}/descargar")
    @Operation(summary = "Descargar el contenido binario de un archivo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Archivo descargado"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    })
    public ResponseEntity<byte[]> descargar(@PathVariable Integer id) {
        byte[] datos          = archivoService.descargarArchivo(id);
        String tipoContenido  = archivoService.obtenerTipoContenido(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(tipoContenido));

        // Content-Disposition: attachment → fuerza descarga en el navegador
        // Content-Disposition: inline    → intenta mostrar en el navegador (PDF/imágenes)
        // Usamos inline para que los PDF e imágenes se puedan previsualizar en Angular
        headers.setContentDisposition(
                ContentDisposition.inline().build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(datos);
    }

    // ---------------------------------------------------------------
    // POST /api/archivos
    // Recibe un MultipartFile (formulario multipart/form-data)
    // idLocal es un query param opcional
    // ---------------------------------------------------------------
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Subir un archivo",
            description = "Acepta: JPEG, PNG, GIF, PDF, DOC, DOCX. " +
                    "Tamaño máximo: 10 MB. " +
                    "idLocal es opcional: si se pasa, vincula el archivo a ese local. " +
                    "En Insomnia: Body → Form → selecciona 'Multipart Form', " +
                    "agrega un campo tipo 'File' con el nombre 'archivo'."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Archivo subido correctamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío, tipo no permitido o supera 10 MB"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<RespuestaDTO> subir(
            // @RequestParam con MultipartFile captura el campo del form con nombre "archivo"
            @Parameter(description = "Archivo a subir (campo multipart con nombre 'archivo')")
            @RequestParam("archivo") MultipartFile archivo,
            @Parameter(description = "ID del local al que pertenece el archivo (opcional)")
            @RequestParam(value = "idLocal", required = false) Integer idLocal)
            throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(archivoService.subirArchivo(archivo, idLocal));
    }

    // ---------------------------------------------------------------
    // DELETE /api/archivos/{id}
    // ---------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un archivo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Archivo eliminado"),
            @ApiResponse(responseCode = "404", description = "Archivo no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        archivoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}