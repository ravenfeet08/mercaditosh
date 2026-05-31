package com.ipn.mx.mercaditosh.features.inspeccion.controller;

import com.ipn.mx.mercaditosh.core.entidades.Inspeccion;
import com.ipn.mx.mercaditosh.features.inspeccion.service.InspeccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inspecciones")
@RequiredArgsConstructor
@Tag(name = "Inspecciones",
        description = "Registro y consulta de inspecciones sanitarias de los locales")
public class InspeccionController {

    private final InspeccionService inspeccionService;

    // GET /api/inspecciones
    @GetMapping
    @Operation(summary = "Listar todas las inspecciones")
    public ResponseEntity<List<Inspeccion>> listar() {
        return ResponseEntity.ok(inspeccionService.obtenerTodas());
    }

    // GET /api/inspecciones/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una inspección por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inspección encontrada"),
            @ApiResponse(responseCode = "404", description = "Inspección no encontrada")
    })
    public ResponseEntity<Inspeccion> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(inspeccionService.obtenerPorId(id));
    }

    // GET /api/inspecciones/local/{idLocal}
    @GetMapping("/local/{idLocal}")
    @Operation(summary = "Listar inspecciones de un local específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<List<Inspeccion>> porLocal(@PathVariable Integer idLocal) {
        return ResponseEntity.ok(inspeccionService.obtenerPorLocal(idLocal));
    }

    // GET /api/inspecciones/local/{idLocal}/ultima
    @GetMapping("/local/{idLocal}/ultima")
    @Operation(summary = "Obtener la inspección más reciente de un local")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inspección encontrada o null si no hay ninguna"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Inspeccion> ultimaPorLocal(@PathVariable Integer idLocal) {
        return inspeccionService.obtenerUltimaInspeccionDeLocal(idLocal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // GET /api/inspecciones/resultado/{resultado}
    // Ejemplo: /api/inspecciones/resultado/no_aprobado
    @GetMapping("/resultado/{resultado}")
    @Operation(
            summary = "Filtrar inspecciones por resultado",
            description = "Valores válidos: aprobado, condicionado, no_aprobado"
    )
    public ResponseEntity<List<Inspeccion>> porResultado(@PathVariable String resultado) {
        return ResponseEntity.ok(inspeccionService.obtenerPorResultado(resultado));
    }

    // GET /api/inspecciones/local/{idLocal}/resultado/{resultado}
    @GetMapping("/local/{idLocal}/resultado/{resultado}")
    @Operation(summary = "Inspecciones de un local filtradas por resultado")
    public ResponseEntity<List<Inspeccion>> porLocalYResultado(
            @PathVariable Integer idLocal,
            @PathVariable String resultado) {
        return ResponseEntity.ok(
                inspeccionService.obtenerPorLocalYResultado(idLocal, resultado));
    }

    // GET /api/inspecciones/rango?desde=2025-01-01&hasta=2025-06-30
    @GetMapping("/rango")
    @Operation(
            summary = "Filtrar inspecciones por rango de fecha",
            description = "Formato: YYYY-MM-DD"
    )
    public ResponseEntity<List<Inspeccion>> porRangoFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(inspeccionService.obtenerPorRangoFecha(desde, hasta));
    }

    // GET /api/inspecciones/mercado/{idMercado}
    @GetMapping("/mercado/{idMercado}")
    @Operation(summary = "Listar todas las inspecciones de un mercado")
    public ResponseEntity<List<Inspeccion>> porMercado(@PathVariable Integer idMercado) {
        return ResponseEntity.ok(inspeccionService.obtenerPorMercado(idMercado));
    }

    // GET /api/inspecciones/mercado/{idMercado}/resultado/{resultado}
    @GetMapping("/mercado/{idMercado}/resultado/{resultado}")
    @Operation(summary = "Inspecciones de un mercado filtradas por resultado")
    public ResponseEntity<List<Inspeccion>> porMercadoYResultado(
            @PathVariable Integer idMercado,
            @PathVariable String resultado) {
        return ResponseEntity.ok(
                inspeccionService.obtenerPorMercadoYResultado(idMercado, resultado));
    }

    // GET /api/inspecciones/mercado/{idMercado}/resumen
    // Devuelve: { "aprobado": 12, "condicionado": 3, "no_aprobado": 1 }
    @GetMapping("/mercado/{idMercado}/resumen")
    @Operation(
            summary = "Resumen de inspecciones de un mercado agrupadas por resultado",
            description = "Devuelve un conteo por cada tipo de resultado: aprobado, condicionado, no_aprobado"
    )
    public ResponseEntity<Map<String, Long>> resumenPorMercado(
            @PathVariable Integer idMercado) {
        return ResponseEntity.ok(inspeccionService.obtenerResumenPorMercado(idMercado));
    }

    // GET /api/inspecciones/alertas?dias=30
    @GetMapping("/alertas")
    @Operation(
            summary = "Inspecciones no aprobadas en los últimos N días",
            description = "Útil para el panel de alertas sanitarias del administrador. " +
                    "Ejemplo: /alertas?dias=30"
    )
    public ResponseEntity<List<Inspeccion>> alertasRecientes(
            @Parameter(description = "Número de días hacia atrás para buscar")
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(inspeccionService.obtenerNoAprobadasRecientes(dias));
    }

    // POST /api/inspecciones?idLocal={idLocal}
    @PostMapping
    @Operation(
            summary = "Registrar una nueva inspección",
            description = "fecha es opcional: si no se envía, se usa la fecha actual. " +
                    "No se permiten fechas futuras. " +
                    "Si resultado es 'no_aprobado', observaciones es obligatorio. " +
                    "Requiere idLocal como query param: POST /api/inspecciones?idLocal=1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inspección registrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, fecha futura " +
                    "o no_aprobado sin observaciones"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Inspeccion> crear(
            @Valid @RequestBody Inspeccion inspeccion,
            @Parameter(description = "ID del local que se inspecciona")
            @RequestParam Integer idLocal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inspeccionService.guardar(inspeccion, idLocal));
    }

    // PUT /api/inspecciones/{id}
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una inspección existente",
            description = "Se pueden modificar fecha, resultado y observaciones. " +
                    "El local inspeccionado no cambia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inspección actualizada"),
            @ApiResponse(responseCode = "404", description = "Inspección no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Inspeccion> actualizar(
            @PathVariable Integer id,
            @RequestBody Inspeccion inspeccion) {
        return ResponseEntity.ok(inspeccionService.actualizar(id, inspeccion));
    }

    // DELETE /api/inspecciones/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una inspección")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inspección eliminada"),
            @ApiResponse(responseCode = "404", description = "Inspección no encontrada")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        inspeccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}