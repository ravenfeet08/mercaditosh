package com.ipn.mx.mercaditosh.core.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pago")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @NotNull(message = "La fecha de pago no puede ser nula")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    // BigDecimal es el tipo correcto para dinero en Java:
    // - Evita errores de redondeo de float/double
    // - @DecimalMin: el monto debe ser mayor a cero
    // - @Digits: máximo 10 dígitos enteros y 2 decimales (igual que DECIMAL(10,2) en BD)
    @NotNull(message = "El monto no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2,
            message = "El monto debe tener como máximo 10 dígitos enteros y 2 decimales")
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    // FK a Locatario: un locatario realiza muchos pagos
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_locatario", nullable = false)
    @JsonBackReference
    private Locatario locatario;
}