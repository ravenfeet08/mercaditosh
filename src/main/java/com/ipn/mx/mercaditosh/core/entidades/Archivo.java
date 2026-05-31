package com.ipn.mx.mercaditosh.core.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "archivo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo")
    private Integer idArchivo;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    // MIME type: "application/pdf", "image/jpeg", "image/png", etc.
    @Column(name = "tipo_contenido", nullable = false, length = 100)
    private String tipoContenido;

    // En YugabyteDB/PostgreSQL mapea al tipo BYTEA.
    @Column(name = "datos", nullable = false, columnDefinition = "bytea")
    private byte[] datos;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    // Relación opcional con LocalComercial
    // (un archivo puede pertenecer a un local o ser genérico)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_local")
    private Local local;
}
