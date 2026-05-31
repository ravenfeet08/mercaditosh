package com.ipn.mx.mercaditosh.core.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "inspeccion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inspeccion")
    private Integer idInspeccion;

    @NotNull(message = "La fecha de inspección no puede ser nula")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    // Solo acepta los tres valores definidos en el modelo original
    @NotBlank(message = "El resultado no puede estar vacío")
    @Pattern(
            regexp = "aprobado|condicionado|no_aprobado",
            message = "El resultado debe ser: aprobado, condicionado o no_aprobado"
    )
    @Column(name = "resultado", nullable = false, length = 50)
    private String resultado;

    // Nullable según el diccionario de datos original del proyecto:
    // las observaciones son opcionales, no siempre hay comentarios
    @Size(max = 200, message = "Las observaciones no pueden superar los 200 caracteres")
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    // FK a LocalComercial: un local puede tener muchas inspecciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local", nullable = false)
    @JsonBackReference
    private Local local;
}
