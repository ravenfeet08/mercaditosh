package com.ipn.mx.mercaditosh.features.producto.service;

import com.ipn.mx.mercaditosh.core.entidades.Producto;

import java.util.List;

public interface ProductoService {

    List<Producto> obtenerTodos();

    Producto obtenerPorId(Integer id);

    List<Producto> obtenerPorLocal(Integer idLocal);

    List<Producto> obtenerPorMercado(Integer idMercado);

    List<Producto> obtenerPorCategoria(String categoria);

    List<Producto> buscarPorNombre(String nombre);

    Producto guardar(Producto producto, Integer idLocal);

    Producto actualizar(Integer id, Producto producto);

    void eliminar(Integer id);
}
