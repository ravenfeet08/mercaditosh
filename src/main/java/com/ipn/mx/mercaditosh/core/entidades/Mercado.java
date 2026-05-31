package com.ipn.mx.mercaditosh.core.entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "mercado")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mercado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //mapea SERIAL de YugabyteDB
    @Column(name = "id_mercado")
    private Integer idMercado;

    @NotBlank(message = "El nombre del mercado no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
    @Column(name = "direccion", nullable = false, length = 150)
    private String direccion;

    @NotBlank(message = "La alcaldía no puede estar vacía")
    @Size(max = 100, message = "La alcaldía no puede superar los 100 caracteres")
    @Column(name = "alcaldia", nullable = false, length = 100)
    private String alcaldia;
}
