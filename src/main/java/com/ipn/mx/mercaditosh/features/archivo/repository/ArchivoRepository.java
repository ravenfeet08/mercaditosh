package com.ipn.mx.mercaditosh.features.archivo.repository;

import com.ipn.mx.mercaditosh.core.entidades.Archivo;
import com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivoRepository extends JpaRepository<Archivo, Integer> {

    // Archivos vinculados a un local específico
    List<Archivo> findByLocal_IdLocal(Integer idLocal);

    // Filtrar por tipo de contenido (ej: todos los PDF)
    List<Archivo> findByTipoContenido(String tipoContenido);

    // Archivos no vinculados a ningún local (archivos generales)
    List<Archivo> findByLocalIsNull();

    // Listar metadatos sin traer el campo "datos" (BYTEA pesado)
    // Usamos JPQL seleccionando solo los campos ligeros
    @Query(value = "SELECT new com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO(" +
            "a.idArchivo, a.nombreArchivo, a.tipoContenido, a.fechaSubida, " +
            "CASE WHEN a.local IS NOT NULL THEN a.local.idLocal ELSE NULL END, " +
            "CONCAT('/api/archivos/', a.idArchivo, '/descargar')) " +
            "FROM Archivo a")
    List<RespuestaDTO> findAllMetadatos();

    // Metadatos de archivos de un local sin traer los bytes
    @Query(value = "SELECT new com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO(" +
            "a.idArchivo, a.nombreArchivo, a.tipoContenido, a.fechaSubida, " +
            "a.local.idLocal, " +
            "CONCAT('/api/archivos/', a.idArchivo, '/descargar')) " +
            "FROM Archivo a WHERE a.local.idLocal = :idLocal")
    List<RespuestaDTO> findMetadatosByIdLocal(@Param("idLocal") Integer idLocal);
}
