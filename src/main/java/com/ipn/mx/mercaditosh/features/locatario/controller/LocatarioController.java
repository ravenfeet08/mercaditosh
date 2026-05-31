package com.ipn.mx.mercaditosh.features.locatario.controller;

import com.ipn.mx.mercaditosh.core.entidades.Locatario;
import com.ipn.mx.mercaditosh.features.locatario.service.LocatarioService;
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

@RestController
@RequestMapping("/api/locatarios")
@RequiredArgsConstructor
@Tag(name = "Locatarios", description = "Gestión de comerciantes que ocupan los locales")
public class LocatarioController {

    private final LocatarioService locatarioService;

    // GET /api/locatarios
    @GetMapping
    @Operation(summary = "Listar todos los locatarios")
    public ResponseEntity<List<Locatario>> listar() {
        return ResponseEntity.ok(locatarioService.obtenerTodos());
    }

    // GET /api/locatarios/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un locatario por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locatario encontrado"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado")
    })
    public ResponseEntity<Locatario> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(locatarioService.obtenerPorId(id));
    }

    // GET /api/locatarios/local/{idLocal}
    @GetMapping("/local/{idLocal}")
    @Operation(summary = "Listar locatarios de un local específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<List<Locatario>> porLocal(@PathVariable Integer idLocal) {
        return ResponseEntity.ok(locatarioService.obtenerPorLocal(idLocal));
    }

    // GET /api/locatarios/mercado/{idMercado}
    @GetMapping("/mercado/{idMercado}")
    @Operation(summary = "Listar todos los locatarios de un mercado")
    public ResponseEntity<List<Locatario>> porMercado(@PathVariable Integer idMercado) {
        return ResponseEntity.ok(locatarioService.obtenerPorMercado(idMercado));
    }

    // GET /api/locatarios/buscar?nombre=María
    @GetMapping("/buscar")
    @Operation(summary = "Buscar locatarios por nombre (búsqueda parcial)")
    public ResponseEntity<List<Locatario>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre del locatario")
            @RequestParam String nombre) {
        return ResponseEntity.ok(locatarioService.buscarPorNombre(nombre));
    }

    // GET /api/locatarios/rango?desde=2025-01-01&hasta=2025-06-30
    @GetMapping("/rango")
    @Operation(
            summary = "Filtrar locatarios por rango de fecha de registro",
            description = "Formato de fechas: YYYY-MM-DD. Ejemplo: /rango?desde=2025-01-01&hasta=2025-06-30"
    )
    public ResponseEntity<List<Locatario>> porRangoFecha(
            @Parameter(description = "Fecha inicial (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @Parameter(description = "Fecha final (YYYY-MM-DD)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(locatarioService.obtenerPorRangoFecha(desde, hasta));
    }

    // POST /api/locatarios?idLocal={idLocal}
    @PostMapping
    @Operation(
            summary = "Registrar un nuevo locatario",
            description = "El campo fechaRegistro es opcional: si no se envía, se asigna la fecha actual. " +
                    "Requiere idLocal como query param: POST /api/locatarios?idLocal=1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Locatario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos, teléfono duplicado o local en mantenimiento"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Locatario> crear(
            @Valid @RequestBody Locatario locatario,
            @Parameter(description = "ID del local que ocupará el locatario")
            @RequestParam Integer idLocal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locatarioService.guardar(locatario, idLocal));
    }

    // PUT /api/locatarios/{id}
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar datos de un locatario",
            description = "Solo actualiza nombre y teléfono. La fecha de registro y el local asignado no se modifican."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locatario actualizado"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o teléfono duplicado")
    })
    public ResponseEntity<Locatario> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Locatario locatario) {
        return ResponseEntity.ok(locatarioService.actualizar(id, locatario));
    }

    // DELETE /api/locatarios/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un locatario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Locatario eliminado"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        locatarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}