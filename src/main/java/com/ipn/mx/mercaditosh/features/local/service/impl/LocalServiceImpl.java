package com.ipn.mx.mercaditosh.features.local.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.core.entidades.Mercado;
import com.ipn.mx.mercaditosh.features.local.repository.LocalRepository;
import com.ipn.mx.mercaditosh.features.local.service.LocalService;
import com.ipn.mx.mercaditosh.features.mercado.repository.MercadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LocalServiceImpl implements LocalService {

    private final LocalRepository localRepository;

    // Necesitamos el repositorio de Mercado para verificar que el mercado
    // padre existe antes de guardar o actualizar un local.
    private final MercadoRepository mercadoRepository;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Local> obtenerTodos() {
        return localRepository.findAllConMercado();
    }

    @Override
    @Transactional(readOnly = true)
    public Local obtenerPorId(Integer id) {
        return localRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Local con id "+id+" no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Local> obtenerPorMercado(Integer idMercado) {
        // Primero verificamos que el mercado existe
        if (!mercadoRepository.existsById(idMercado)) {
            throw new NoSuchElementException("Mercado con id " + idMercado + " no encontrado");
        }
        return localRepository.findByMercado_IdMercado(idMercado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Local> obtenerPorEstado(String estado) {
        return localRepository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Local> obtenerPorMercadoYEstado(Integer idMercado, String estado) {
        return localRepository.findByMercadoYEstado(idMercado, estado);
    }

    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Local guardar(Local local, Integer idMercado) {
        // 1. Verificar que el mercado padre existe
        Mercado mercado = mercadoRepository.findById(idMercado)
                .orElseThrow(() -> new NoSuchElementException(
                        "Mercado con id " + idMercado + " no encontrado"));

        // 2. Validar que no exista ya ese número de local en el mismo mercado
        if (localRepository.existsByNumeroLocalAndMercado_IdMercado(
                local.getNumeroLocal(), idMercado)) {
            throw new IllegalArgumentException(
                    "Ya existe el local número " + local.getNumeroLocal()
                            + " en el mercado con id " + idMercado);
        }

        // 3. Asignar la relación y persistir
        local.setMercado(mercado);
        return localRepository.save(local);
    }

    @Override
    @Transactional
    public Local actualizar(Integer id, Local local) {
        Local existente = obtenerPorId(id);

        // Actualizamos solo los campos editables
        // La FK al mercado NO se cambia en un PUT normal;
        // si se necesitara, sería un endpoint dedicado.
        existente.setNumeroLocal(local.getNumeroLocal());
        existente.setTipoLocal(local.getTipoLocal());
        existente.setEstado(local.getEstado());

        return localRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!localRepository.existsById(id)) {
            throw new NoSuchElementException("Local con id " + id + " no encontrado");
        }
        localRepository.deleteById(id);
    }
}
