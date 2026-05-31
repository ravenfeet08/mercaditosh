package com.ipn.mx.mercaditosh.features.mercado.controller;

import com.ipn.mx.mercaditosh.core.entidades.Mercado;
import com.ipn.mx.mercaditosh.features.mercado.service.MercadoService;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mercados")
@RequiredArgsConstructor
@Tag(name = "Mercados", description= "Gestión de mercados públicos de la CDMX")
public class MercadoController {
    private final MercadoService mercadoService;

    // ---------------------------------------------------------------
    // GET /api/mercados
    // ---------------------------------------------------------------
    @GetMapping
    @Operation(summary = "Listar todos los mercados")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    public ResponseEntity<List<Mercado>> listar() {
        return ResponseEntity.ok(mercadoService.findAll());
    }

    // ---------------------------------------------------------------
    // GET /api/mercados/{id}
    // ---------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un mercado por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mercado encontrado"),
            @ApiResponse(responseCode = "404", description = "Mercado no encontrado")
    })
    public ResponseEntity<Mercado>  findById(@Parameter (description = "ID del mercado") @PathVariable Integer id) {
        return ResponseEntity.ok(mercadoService.findById(id));
    }

    // ---------------------------------------------------------------
    // GET /api/mercados/alcaldia/{alcaldia}
    // ---------------------------------------------------------------
    @GetMapping("/alcaldia/{alcaldia}")
    public ResponseEntity<List<Mercado>> byAlcaldia(
            @Parameter(description = "Nombre de la alcaldía, ej: Coyoacán")
            @PathVariable String alcaldia) {
        return ResponseEntity.ok(mercadoService.findByAlcaldia(alcaldia));
    }

    // ---------------------------------------------------------------
    // POST /api/mercados
    // ---------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Registrar un nuevo mercado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mercado creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o mercado duplicado")
    })
    public ResponseEntity<Mercado> create(
            //@Valid activa las validaciones definidas en la entidad (@NotBlank, @Size, etc.)
            @Valid @RequestBody Mercado mercado) {
        Mercado entity = mercadoService.save(mercado);
        return ResponseEntity.status(HttpStatus.CREATED).body(entity);
    }

    // ---------------------------------------------------------------
    // PUT /api/mercados/{id}
    // ---------------------------------------------------------------
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un mercado existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mercado actualizado"),
            @ApiResponse(responseCode = "404", description = "Mercado no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<Mercado> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Mercado mercado) {
        return ResponseEntity.ok(mercadoService.update(id, mercado));
    }

    // ---------------------------------------------------------------
    // DELETE /api/mercados/{id}
    // ---------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un mercado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mercado eliminado"),
            @ApiResponse(responseCode = "404", description = "Mercado no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        mercadoService.delete(id);
        // 204 No Content: operación exitosa sin cuerpo en la respuesta
        return ResponseEntity.noContent().build();
    }
}
