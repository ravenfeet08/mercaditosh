package com.ipn.mx.mercaditosh.features.pago.controller;

import com.ipn.mx.mercaditosh.core.entidades.Pago;
import com.ipn.mx.mercaditosh.features.pago.service.PagoService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Registro y consulta de pagos de renta de los locatarios")
public class PagoController {

    private final PagoService pagoService;

    // GET /api/pagos
    @GetMapping
    @Operation(summary = "Listar todos los pagos")
    public ResponseEntity<List<Pago>> listar() {
        return ResponseEntity.ok(pagoService.obtenerTodos());
    }

    // GET /api/pagos/{id}
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pago por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<Pago> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    // GET /api/pagos/locatario/{idLocatario}
    @GetMapping("/locatario/{idLocatario}")
    @Operation(summary = "Listar pagos de un locatario específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado")
    })
    public ResponseEntity<List<Pago>> porLocatario(@PathVariable Integer idLocatario) {
        return ResponseEntity.ok(pagoService.obtenerPorLocatario(idLocatario));
    }

    // GET /api/pagos/mercado/{idMercado}
    @GetMapping("/mercado/{idMercado}")
    @Operation(summary = "Listar todos los pagos recibidos en un mercado")
    public ResponseEntity<List<Pago>> porMercado(@PathVariable Integer idMercado) {
        return ResponseEntity.ok(pagoService.obtenerPorMercado(idMercado));
    }

    // GET /api/pagos/rango?desde=2025-01-01&hasta=2025-06-30
    @GetMapping("/rango")
    @Operation(
            summary = "Filtrar pagos por rango de fecha",
            description = "Formato: YYYY-MM-DD. Ejemplo: /rango?desde=2025-01-01&hasta=2025-06-30"
    )
    public ResponseEntity<List<Pago>> porRangoFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(pagoService.obtenerPorRangoFecha(desde, hasta));
    }

    // GET /api/pagos/locatario/{idLocatario}/rango?desde=...&hasta=...
    @GetMapping("/locatario/{idLocatario}/rango")
    @Operation(summary = "Historial de pagos de un locatario en un rango de fechas")
    public ResponseEntity<List<Pago>> porLocatarioYRangoFecha(
            @PathVariable Integer idLocatario,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(
                pagoService.obtenerPorLocatarioYRangoFecha(idLocatario, desde, hasta));
    }

    // GET /api/pagos/locatario/{idLocatario}/total
    @GetMapping("/locatario/{idLocatario}/total")
    @Operation(summary = "Suma total de pagos realizados por un locatario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado")
    })
    public ResponseEntity<BigDecimal> totalPorLocatario(@PathVariable Integer idLocatario) {
        return ResponseEntity.ok(pagoService.obtenerTotalPorLocatario(idLocatario));
    }

    // GET /api/pagos/total?desde=2025-01-01&hasta=2025-06-30
    @GetMapping("/total")
    @Operation(summary = "Suma total de ingresos en un rango de fechas")
    public ResponseEntity<BigDecimal> totalPorRangoFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(pagoService.obtenerTotalPorRangoFecha(desde, hasta));
    }

    // POST /api/pagos?idLocatario={idLocatario}
    @PostMapping
    @Operation(
            summary = "Registrar un nuevo pago",
            description = "fechaPago es opcional: si no se envía, se usa la fecha actual. " +
                    "No se permiten fechas futuras. " +
                    "Requiere idLocatario como query param: POST /api/pagos?idLocatario=1"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago registrado"),
            @ApiResponse(responseCode = "400", description = "Monto inválido o fecha futura"),
            @ApiResponse(responseCode = "404", description = "Locatario no encontrado")
    })
    public ResponseEntity<Pago> crear(
            @Valid @RequestBody Pago pago,
            @Parameter(description = "ID del locatario que realiza el pago")
            @RequestParam Integer idLocatario) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pagoService.guardar(pago, idLocatario));
    }

    // PUT /api/pagos/{id}
    @PutMapping("/{id}")
    @Operation(
            summary = "Corregir monto o fecha de un pago existente",
            description = "Solo se permite modificar monto y fechaPago. El locatario no cambia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago actualizado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o fecha futura")
    })
    public ResponseEntity<Pago> actualizar(
            @PathVariable Integer id,
            @RequestBody Pago pago) {
        return ResponseEntity.ok(pagoService.actualizar(id, pago));
    }

    // DELETE /api/pagos/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un pago")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pago eliminado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
