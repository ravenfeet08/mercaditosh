package com.ipn.mx.mercaditosh.features.producto.controller;

import com.ipn.mx.mercaditosh.core.entidades.Producto;
import com.ipn.mx.mercaditosh.features.producto.service.ProductoService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos ofertados en los locales")
public class ProductoController {

    private final ProductoService productoService;

    // GET /api/productos
    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<List<Producto>> listar() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    // GET /api/productos/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    // GET /api/productos/local/{idLocal}
    @GetMapping("/local/{idLocal}")
    @Operation(summary = "Listar productos de un local específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<List<Producto>> porLocal(@PathVariable Integer idLocal) {
        return ResponseEntity.ok(productoService.obtenerPorLocal(idLocal));
    }

    // GET /api/productos/mercado/{idMercado}
    @GetMapping("/mercado/{idMercado}")
    @Operation(summary = "Listar todos los productos de un mercado completo")
    public ResponseEntity<List<Producto>> porMercado(@PathVariable Integer idMercado) {
        return ResponseEntity.ok(productoService.obtenerPorMercado(idMercado));
    }

    // GET /api/productos/categoria?q=frutas
    @GetMapping("/categoria")
    @Operation(summary = "Filtrar productos por categoría (búsqueda parcial)")
    public ResponseEntity<List<Producto>> porCategoria(
            @Parameter(description = "Texto a buscar en la categoría, ej: frutas")
            @RequestParam String q) {
        return ResponseEntity.ok(productoService.obtenerPorCategoria(q));
    }

    // GET /api/productos/buscar?nombre=jitomate
    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos por nombre (búsqueda parcial)")
    public ResponseEntity<List<Producto>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre del producto")
            @RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    // POST /api/productos?idLocal={idLocal}
    @PostMapping
    @Operation(
            summary = "Registrar un nuevo producto",
            description = "Requiere idLocal como query param: POST /api/productos?idLocal=1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o producto duplicado en el local"),
            @ApiResponse(responseCode = "404", description = "Local no encontrado")
    })
    public ResponseEntity<Producto> crear(
            @Valid @RequestBody Producto producto,
            @Parameter(description = "ID del local donde se vende el producto")
            @RequestParam Integer idLocal) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productoService.guardar(producto, idLocal));
    }

    // PUT /api/productos/{id}
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar nombre y categoría de un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre duplicado en el local")
    })
    public ResponseEntity<Producto> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    // DELETE /api/productos/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
