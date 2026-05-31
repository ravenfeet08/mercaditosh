package com.ipn.mx.mercaditosh.core.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "local_comercial")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_local")
    private Integer idLocal;

    @NotBlank(message = "El numero del local no puede estar vacío")
    @Column(name = "numero_local", nullable = false)
    private Integer numeroLocal;

    @NotBlank(message = "El tipo del local no puede estar vacío")
    @Size(max = 50, message = "El tipo del local no puede superar los 50 caracteres")
    @Column(name = "tipo_local",  nullable = false, length = 50)
    private String tipoLocal;

    @NotBlank(message = "El estado del local no puede estar vacío")
    @Size(max = 50, message = "El estado no puede superar los 50 caracteres")
    @Column(name = "estado",  nullable = false, length = 50)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mercado", nullable = false)
    @JsonBackReference
    private Integer idMercado;
}
