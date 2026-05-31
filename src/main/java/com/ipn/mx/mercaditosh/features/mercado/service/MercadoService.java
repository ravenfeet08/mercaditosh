package com.ipn.mx.mercaditosh.features.mercado.service;

import com.ipn.mx.mercaditosh.core.entidades.Mercado;

import java.util.List;

public interface MercadoService {
    List <Mercado> findAll();
    Mercado findById(Integer id);
    List<Mercado> findByAlcaldia(String alcaldia);
    Mercado save(Mercado mercado);
    Mercado update(Integer id, Mercado mercado);
    void delete(Integer id);
}
