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
@Table(name = "locatario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Locatario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_locatario")
    private Integer idLocatario;

    @NotBlank(message = "El nombre del locatario no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(
            regexp = "^[0-9\\-\\+\\s\\(\\)]{7,20}$",
            message = "El teléfono solo puede contener dígitos, espacios, +, -, ( y ) — entre 7 y 20 caracteres"
    )
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    // LocalDate mapea directamente al tipo DATE de YugabyteDB/PostgreSQL.
    // Se asigna automáticamente en el servicio si no viene en el request.
    @NotNull(message = "La fecha de registro no puede ser nula")
    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    // Relación ManyToOne con LocalComercial:
    // Muchos locatarios pueden ocupar un local a lo largo del tiempo,
    // pero en un momento dado cada locatario está vinculado a un local.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local", nullable = false)
    @JsonBackReference
    private Local local;
}
