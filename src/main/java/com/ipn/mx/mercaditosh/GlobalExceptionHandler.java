package com.ipn.mx.mercaditosh;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones.
 *
 * Va en el paquete raíz (mismo nivel que MercaditoshApplication y CorsConfig).
 *
 * Sin esta clase, Spring Boot devuelve errores con formato inconsistente.
 * Con ella, TODOS los endpoints devuelven errores en el mismo formato JSON:
 *
 * {
 *   "timestamp": "2025-05-30T12:00:00",
 *   "status": 404,
 *   "mensaje": "Mercado con id 99 no encontrado"
 * }
 *
 * Esto es crítico para que tu frontend Angular pueda interpretar los errores
 * de forma predecible.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------
    // 404 — Recurso no encontrado
    // Lanzada por: mercadoService.obtenerPorId() cuando el id no existe
    // ---------------------------------------------------------------
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ---------------------------------------------------------------
    // 400 — Regla de negocio violada
    // Lanzada por: mercadoService.guardar() cuando el nombre está duplicado
    // ---------------------------------------------------------------
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ---------------------------------------------------------------
    // 400 — Validación de campos fallida (@Valid en el controlador)
    // Lanzada automáticamente por Spring cuando @NotBlank, @Size, etc. fallan
    // ---------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        // Recopila todos los mensajes de error por campo
        Map<String, String> erroresPorCampo = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            erroresPorCampo.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", "Error de validación en los datos enviados");
        body.put("errores", erroresPorCampo);   // ej: { "nombre": "no puede estar vacío" }

        return ResponseEntity.badRequest().body(body);
    }

    // ---------------------------------------------------------------
    // 500 — Cualquier otra excepción no manejada
    // ---------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ---------------------------------------------------------------
    // Utilidad para construir la respuesta de error
    // ---------------------------------------------------------------
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}