package com.ipn.mx.mercaditosh.features.locatario.service;

import com.ipn.mx.mercaditosh.core.entidades.Locatario;

import java.time.LocalDate;
import java.util.List;

public interface LocatarioService {

    List<Locatario> obtenerTodos();

    Locatario obtenerPorId(Integer id);

    List<Locatario> obtenerPorLocal(Integer idLocal);

    List<Locatario> obtenerPorMercado(Integer idMercado);

    List<Locatario> buscarPorNombre(String nombre);

    List<Locatario> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta);

    // idLocal separado del objeto para simplificar el request del cliente
    Locatario guardar(Locatario locatario, Integer idLocal);

    Locatario actualizar(Integer id, Locatario locatario);

    void eliminar(Integer id);
}