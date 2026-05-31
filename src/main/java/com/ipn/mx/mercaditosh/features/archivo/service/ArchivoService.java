package com.ipn.mx.mercaditosh.features.archivo.service;

import com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ArchivoService {

    // Subir un archivo y guardarlo en la BD
    // idLocal es opcional (puede ser null si el archivo no pertenece a un local)
    RespuestaDTO subirArchivo(MultipartFile file, Integer idLocal) throws IOException;

    // Obtener los bytes para descargar un archivo
    byte[] descargarArchivo(Integer id);

    // Obtener el tipo MIME de un archivo (necesario para el header Content-Type)
    String obtenerTipoContenido(Integer id);

    // Listar metadatos de todos los archivos (sin los bytes)
    List<RespuestaDTO> listarMetadatos();

    // Listar metadatos de archivos de un local específico
    List<RespuestaDTO> listarMetadatosPorLocal(Integer idLocal);

    // Eliminar un archivo
    void eliminar(Integer id);
}
