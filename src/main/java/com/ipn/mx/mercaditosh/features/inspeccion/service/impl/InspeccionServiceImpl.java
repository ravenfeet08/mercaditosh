package com.ipn.mx.mercaditosh.features.inspeccion.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Inspeccion;
import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.features.inspeccion.repository.InspeccionRepository;
import com.ipn.mx.mercaditosh.features.inspeccion.service.InspeccionService;
import com.ipn.mx.mercaditosh.features.local.repository.LocalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InspeccionServiceImpl implements InspeccionService {

    private final InspeccionRepository inspeccionRepository;
    private final LocalRepository localRepository;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerTodas() {
        return inspeccionRepository.findAllConLocal();
    }

    @Override
    @Transactional(readOnly = true)
    public Inspeccion obtenerPorId(Integer id) {
        return inspeccionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Inspección con id " + id + " no encontrada"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorLocal(Integer idLocal) {
        if (!localRepository.existsById(idLocal)) {
            throw new NoSuchElementException(
                    "Local con id " + idLocal + " no encontrado");
        }
        return inspeccionRepository.findByLocal_IdLocal(idLocal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorResultado(String resultado) {
        return inspeccionRepository.findByResultado(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorLocalYResultado(Integer idLocal, String resultado) {
        return inspeccionRepository.findByLocal_IdLocalAndResultado(idLocal, resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return inspeccionRepository.findByFechaBetween(desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorMercado(Integer idMercado) {
        return inspeccionRepository.findByLocal_Mercado_IdMercado(idMercado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerPorMercadoYResultado(Integer idMercado, String resultado) {
        return inspeccionRepository.findByLocal_Mercado_IdMercadoAndResultado(
                idMercado, resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inspeccion> obtenerUltimaInspeccionDeLocal(Integer idLocal) {
        if (!localRepository.existsById(idLocal)) {
            throw new NoSuchElementException(
                    "Local con id " + idLocal + " no encontrado");
        }
        return inspeccionRepository.findTopByLocal_IdLocalOrderByFechaDesc(idLocal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Inspeccion> obtenerNoAprobadasRecientes(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException(
                    "El número de días debe ser mayor a cero");
        }
        LocalDate desde = LocalDate.now().minusDays(dias);
        return inspeccionRepository.findNoAprobadasDesde(desde);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerResumenPorMercado(Integer idMercado) {
        List<Object[]> filas = inspeccionRepository
                .countByResultadoParaMercado(idMercado);

        // Convertir el resultado de la query de agregación a un Map legible
        // Cada Object[] tiene [resultado (String), cantidad (Long)]
        Map<String, Long> resumen = new HashMap<>();
        resumen.put("aprobado",    0L);
        resumen.put("condicionado", 0L);
        resumen.put("no_aprobado", 0L);

        for (Object[] fila : filas) {
            String resultado = (String) fila[0];
            Long cantidad    = (Long)   fila[1];
            resumen.put(resultado, cantidad);
        }
        return resumen;
    }

    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Inspeccion guardar(Inspeccion inspeccion, Integer idLocal) {
        // 1. Verificar que el local existe
        Local local = localRepository.findById(idLocal)
                .orElseThrow(() -> new NoSuchElementException(
                        "Local con id " + idLocal + " no encontrado"));

        // 2. Asignar fecha actual si no viene en el request
        if (inspeccion.getFecha() == null) {
            inspeccion.setFecha(LocalDate.now());
        }

        // 3. No permitir inspecciones con fecha futura
        if (inspeccion.getFecha().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de inspección no puede ser una fecha futura");
        }

        // 4. Limpiar observaciones vacías para que queden como null en BD
        //    (consistente con el modelo que define observaciones como nullable)
        if (inspeccion.getObservaciones() != null
                && inspeccion.getObservaciones().isBlank()) {
            inspeccion.setObservaciones(null);
        }

        // 5. Si el resultado es "no_aprobado", las observaciones son obligatorias
        if ("no_aprobado".equals(inspeccion.getResultado())
                && inspeccion.getObservaciones() == null) {
            throw new IllegalArgumentException(
                    "Las observaciones son obligatorias cuando el resultado es 'no_aprobado'");
        }

        inspeccion.setLocal(local);
        return inspeccionRepository.save(inspeccion);
    }

    @Override
    @Transactional
    public Inspeccion actualizar(Integer id, Inspeccion datosNuevos) {
        Inspeccion existente = obtenerPorId(id);

        if (datosNuevos.getFecha() != null) {
            if (datosNuevos.getFecha().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La fecha de inspección no puede ser una fecha futura");
            }
            existente.setFecha(datosNuevos.getFecha());
        }

        if (datosNuevos.getResultado() != null) {
            existente.setResultado(datosNuevos.getResultado());
        }

        // Limpiar observaciones vacías
        if (datosNuevos.getObservaciones() != null) {
            existente.setObservaciones(
                    datosNuevos.getObservaciones().isBlank()
                            ? null
                            : datosNuevos.getObservaciones()
            );
        }

        // Re-validar la regla no_aprobado → observaciones obligatorias
        if ("no_aprobado".equals(existente.getResultado())
                && existente.getObservaciones() == null) {
            throw new IllegalArgumentException(
                    "Las observaciones son obligatorias cuando el resultado es 'no_aprobado'");
        }

        return inspeccionRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!inspeccionRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Inspección con id " + id + " no encontrada");
        }
        inspeccionRepository.deleteById(id);
    }
}