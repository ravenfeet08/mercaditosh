package com.ipn.mx.mercaditosh.core.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "producto")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer idProducto;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Column(name = "nombre_producto", nullable = false, length = 100)
    private String nombreProducto;

    @NotBlank(message = "La categoría no puede estar vacía")
    @Size(max = 100, message = "La categoría no puede superar los 100 caracteres")
    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local", nullable = false)
    @JsonBackReference
    private Local local;
}
