package com.ipn.mx.mercaditosh.features.archivo.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Archivo;
import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.features.archivo.dto.RespuestaDTO;
import com.ipn.mx.mercaditosh.features.archivo.repository.ArchivoRepository;
import com.ipn.mx.mercaditosh.features.archivo.service.ArchivoService;
import com.ipn.mx.mercaditosh.features.local.repository.LocalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ArchivoServiceImpl implements ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final LocalRepository localRepository;

    // Tipos de archivo permitidos (whitelist de MIME types)
    private static final List<String> TIPOS_PERMITIDOS = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // Tamaño máximo permitido: 10 MB en bytes
    private static final long TAMANO_MAXIMO = 10 * 1024 * 1024;

    @Override
    @Transactional
    public RespuestaDTO subirArchivo(MultipartFile file, Integer idLocal)
            throws IOException {

        // 1. Validar que el archivo no está vacío
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // 2. Validar tamaño
        if (file.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El archivo supera el tamaño máximo permitido de 10 MB");
        }

        // 3. Validar tipo de contenido (MIME type)
        String tipoContenido = file.getContentType();
        if (tipoContenido == null || !TIPOS_PERMITIDOS.contains(tipoContenido)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido: " + tipoContenido +
                            ". Tipos aceptados: JPEG, PNG, GIF, PDF, DOC, DOCX");
        }

        // 4. Resolver el local si se proporcionó idLocal
        Local local = null;
        if (idLocal != null) {
            local = localRepository.findById(idLocal)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Local con id " + idLocal + " no encontrado"));
        }

        // 5. Construir y persistir la entidad
        Archivo archivo = Archivo.builder()
                .nombreArchivo(file.getOriginalFilename())
                .tipoContenido(tipoContenido)
                .datos(file.getBytes())      // lee el MultipartFile a byte[]
                .fechaSubida(LocalDateTime.now())
                .local(local)
                .build();

        Archivo guardado = archivoRepository.save(archivo);

        // 6. Devolver DTO (sin los bytes)
        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] descargarArchivo(Integer id) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Archivo con id " + id + " no encontrado"));
        return archivo.getDatos();
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerTipoContenido(Integer id) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Archivo con id " + id + " no encontrado"));
        return archivo.getTipoContenido();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaDTO> listarMetadatos() {
        return archivoRepository.findAllMetadatos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RespuestaDTO> listarMetadatosPorLocal(Integer idLocal) {
        if (!localRepository.existsById(idLocal)) {
            throw new NoSuchElementException(
                    "Local con id " + idLocal + " no encontrado");
        }
        return archivoRepository.findMetadatosByIdLocal(idLocal);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!archivoRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Archivo con id " + id + " no encontrado");
        }
        archivoRepository.deleteById(id);
    }

    // ---------------------------------------------------------------
    // Utilidad: entidad → DTO
    // ---------------------------------------------------------------
    private RespuestaDTO toDTO(Archivo archivo) {
        return RespuestaDTO.builder()
                .idArchivo(archivo.getIdArchivo())
                .nombreArchivo(archivo.getNombreArchivo())
                .tipoContenido(archivo.getTipoContenido())
                .fechaSubida(archivo.getFechaSubida())
                .idLocal(archivo.getLocal() != null
                        ? archivo.getLocal().getIdLocal()
                        : null)
                .urlDescarga("/api/archivos/" + archivo.getIdArchivo() + "/descargar")
                .build();
    }
}
