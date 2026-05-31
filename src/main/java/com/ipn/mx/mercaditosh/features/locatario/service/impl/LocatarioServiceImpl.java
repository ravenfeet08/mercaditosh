package com.ipn.mx.mercaditosh.features.locatario.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.core.entidades.Locatario;
import com.ipn.mx.mercaditosh.features.local.repository.LocalRepository;
import com.ipn.mx.mercaditosh.features.locatario.repository.LocatarioRepository;
import com.ipn.mx.mercaditosh.features.locatario.service.LocatarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LocatarioServiceImpl implements LocatarioService {

    private final LocatarioRepository locatarioRepository;
    private final LocalRepository localRepository;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Locatario> obtenerTodos() {
        return locatarioRepository.findAllConLocal();
    }

    @Override
    @Transactional(readOnly = true)
    public Locatario obtenerPorId(Integer id) {
        return locatarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Locatario con id " + id + " no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Locatario> obtenerPorLocal(Integer idLocal) {
        if (!localRepository.existsById(idLocal)) {
            throw new NoSuchElementException(
                    "Local con id " + idLocal + " no encontrado");
        }
        return locatarioRepository.findByLocal_IdLocal(idLocal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Locatario> obtenerPorMercado(Integer idMercado) {
        return locatarioRepository.findByLocal_Mercado_IdMercado(idMercado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Locatario> buscarPorNombre(String nombre) {
        return locatarioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Locatario> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
        return locatarioRepository.findByFechaRegistroBetween(desde, hasta);
    }

    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Locatario guardar(Locatario locatario, Integer idLocal) {
        // 1. Verificar que el local padre existe
        Local local = localRepository.findById(idLocal)
                .orElseThrow(() -> new NoSuchElementException(
                        "Local con id " + idLocal + " no encontrado"));

        // 2. Validar que el local esté ocupado o disponible (no en mantenimiento)
        if ("en_mantenimiento".equals(local.getEstado())) {
            throw new IllegalArgumentException(
                    "No se puede registrar un locatario en un local en mantenimiento");
        }

        // 3. Validar teléfono único
        if (locatarioRepository.existsByTelefono(locatario.getTelefono())) {
            throw new IllegalArgumentException(
                    "Ya existe un locatario con el teléfono: " + locatario.getTelefono());
        }

        // 4. Asignar fecha de registro si no viene en el request
        if (locatario.getFechaRegistro() == null) {
            locatario.setFechaRegistro(LocalDate.now());
        }

        // 5. Asignar relación y persistir
        locatario.setLocal(local);
        return locatarioRepository.save(locatario);
    }

    @Override
    @Transactional
    public Locatario actualizar(Integer id, Locatario datosNuevos) {
        Locatario existente = obtenerPorId(id);

        // Validar que el nuevo teléfono no esté usado por OTRO locatario
        if (!existente.getTelefono().equals(datosNuevos.getTelefono())
                && locatarioRepository.existsByTelefono(datosNuevos.getTelefono())) {
            throw new IllegalArgumentException(
                    "El teléfono " + datosNuevos.getTelefono()
                            + " ya está registrado en otro locatario");
        }

        existente.setNombre(datosNuevos.getNombre());
        existente.setTelefono(datosNuevos.getTelefono());
        // La fecha de registro no se modifica en un PUT
        // El local tampoco: reasignar un locatario a otro local
        // sería una operación de negocio distinta (PATCH dedicado)

        return locatarioRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!locatarioRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Locatario con id " + id + " no encontrado");
        }
        locatarioRepository.deleteById(id);
    }
}
