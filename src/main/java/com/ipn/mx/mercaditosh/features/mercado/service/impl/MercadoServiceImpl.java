package com.ipn.mx.mercaditosh.features.mercado.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Mercado;
import com.ipn.mx.mercaditosh.features.mercado.repository.MercadoRepository;
import com.ipn.mx.mercaditosh.features.mercado.service.MercadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MercadoServiceImpl implements MercadoService {

    private final MercadoRepository mercadoRepository;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Mercado> findAll() {
        return mercadoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mercado findById(Integer id) {
        return mercadoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Mercado con id "+id+" no encontrado"));
    }

    @Override
    @Transactional (readOnly = true)
    public List<Mercado> findByAlcaldia(String alcaldia) {
        return mercadoRepository.findByAlcaldia(alcaldia);
    }


    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Mercado save(Mercado mercado) {
        //Validación de negocio: no permitir nombres duplicados
        if (mercadoRepository.existsByNombreIgnoreCase(mercado.getNombre())) {
            throw new IllegalArgumentException("Ya existe un mercado con el nombre "+mercado.getNombre());
        }
        return mercadoRepository.save(mercado);
    }

    @Override
    @Transactional
    public Mercado update(Integer id, Mercado mercado) {
        //Primero verificamos que el mercado existe (lanca 404 si no)
        Mercado existente = findById(id);

        //Actualizamos solo los campos editables
        existente.setNombre(mercado.getNombre());
        existente.setDireccion( mercado.getDireccion());
        existente.setAlcaldia(mercado.getAlcaldia());

        //save() con id existente hace UPDATE, no INSERT
        return mercadoRepository.save(existente);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!mercadoRepository.existsById(id)) {
            throw new NoSuchElementException("Mercado con id "+id+" no encontrado");
        }
        mercadoRepository.deleteById(id);
    }
}
