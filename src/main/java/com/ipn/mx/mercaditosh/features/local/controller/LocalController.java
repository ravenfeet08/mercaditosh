package com.ipn.mx.mercaditosh.features.local.controller;

import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.features.local.service.LocalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locales")
@RequiredArgsConstructor
@Tag(name = "Locales", description = "Gestión de locales comerciales dentro de los mercados")
public class LocalController {
    private final LocalService localService;

    // GET /api/locales
    @GetMapping
    @Operation(summary = "Listar todos los locales")
    public ResponseEntity<List<Local>> listar() {
        return ResponseEntity.ok(localService.obtenerTodos());
    }

    // GET /api/locales/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un local por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Local encontrado"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Local> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(localService.obtenerPorId(id));
    }

    // GET /api/locales/mercado/{idMercado}
    // Endpoint anidado: "dame todos los locales de este mercado"
    @GetMapping("/mercado/{idMercado}")
    @Operation(summary = "Listar locales de un mercado específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Mercado no encontrado")
    })
    public ResponseEntity<List<Local>> porMercado(
            @Parameter(description = "ID del mercado padre")
            @PathVariable Integer idMercado) {
        return ResponseEntity.ok(localService.obtenerPorMercado(idMercado));
    }

    // GET /api/locales/estado/{estado}
    // Ejemplo: /api/locales/estado/disponible
    @GetMapping("/estado/{estado}")
    @Operation(summary = "Filtrar locales por estado",
            description = "Valores válidos: ocupado, disponible, en_mantenimiento")
    public ResponseEntity<List<Local>> porEstado(@PathVariable String estado) {
        return ResponseEntity.ok(localService.obtenerPorEstado(estado));
    }

    // GET /api/locales/mercado/{idMercado}/estado/{estado}
    @GetMapping("/mercado/{idMercado}/estado/{estado}")
    @Operation(summary = "Filtrar locales de un mercado por estado")
    public ResponseEntity<List<Local>> porMercadoYEstado(
            @PathVariable Integer idMercado,
            @PathVariable String estado) {
        return ResponseEntity.ok(localService.obtenerPorMercadoYEstado(idMercado, estado));
    }

    // POST /api/locales?idMercado={idMercado}
    // El idMercado viene como query param para no anidarlo en el body
    // (el body solo trae los datos del local, no el objeto Mercado completo)
    @PostMapping
    @Operation(summary = "Registrar un nuevo local en un mercado",
            description = "Requiere el parámetro idMercado en la URL: POST /api/locales?idMercado=1")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Local creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o local duplicado"),
            @ApiResponse(responseCode = "404", description = "Mercado no encontrado")
    })
    public ResponseEntity<Local> crear(
            @Valid @RequestBody Local local,
            @Parameter(description = "ID del mercado al que pertenece este local")
            @RequestParam Integer idMercado) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(localService.guardar(local, idMercado));
    }

    // PUT /api/locales/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar los datos de un local")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Local actualizado"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Local> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Local local) {
        return ResponseEntity.ok(localService.actualizar(id, local));
    }

    // DELETE /api/locales/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un local")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Local eliminado"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        localService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
