package com.ipn.mx.mercaditosh.features.local.service;

import com.ipn.mx.mercaditosh.core.entidades.Local;

import java.util.List;

public interface LocalService {
    List<Local> obtenerTodos();

    Local obtenerPorId(Integer id);

    List<Local> obtenerPorMercado(Integer idMercado);

    List<Local> obtenerPorEstado(String estado);

    List<Local> obtenerPorMercadoYEstado(Integer idMercado, String estado);

    // idMercado se pasa por separado para que el controlador no necesite
    // construir el objeto Mercado completo — solo el id viene en la URL
    Local guardar(Local local, Integer idMercado);

    Local actualizar(Integer id, Local local);

    void eliminar(Integer id);
}
