package com.ipn.mx.mercaditosh.features.producto.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Local;
import com.ipn.mx.mercaditosh.core.entidades.Producto;
import com.ipn.mx.mercaditosh.features.local.repository.LocalRepository;
import com.ipn.mx.mercaditosh.features.producto.repository.ProductoRepository;
import com.ipn.mx.mercaditosh.features.producto.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final LocalRepository localRepository;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerTodos() {
        return productoRepository.findAllConLocal();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Producto con id " + id + " no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorLocal(Integer idLocal) {
        if (!localRepository.existsById(idLocal)) {
            throw new NoSuchElementException(
                    "Local con id " + idLocal + " no encontrado");
        }
        return productoRepository.findByLocal_IdLocal(idLocal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorMercado(Integer idMercado) {
        return productoRepository.findByLocal_Mercado_IdMercado(idMercado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByCategoriaContainingIgnoreCase(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreProductoContainingIgnoreCase(nombre);
    }

    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Producto guardar(Producto producto, Integer idLocal) {
        // 1. Verificar que el local padre existe
        Local local = localRepository.findById(idLocal)
                .orElseThrow(() -> new NoSuchElementException(
                        "Local con id " + idLocal + " no encontrado"));

        // 2. Evitar productos duplicados en el mismo local
        if (productoRepository.existsByNombreProductoIgnoreCaseAndLocal_IdLocal(
                producto.getNombreProducto(), idLocal)) {
            throw new IllegalArgumentException(
                    "El producto '" + producto.getNombreProducto()
                            + "' ya existe en el local con id " + idLocal);
        }

        producto.setLocal(local);
        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(Integer id, Producto datosNuevos) {
        Producto existente = obtenerPorId(id);

        // Si cambia el nombre, verificar que no duplique otro producto en el mismo local
        boolean cambiaNombre = !existente.getNombreProducto()
                .equalsIgnoreCase(datosNuevos.getNombreProducto());

        if (cambiaNombre && productoRepository.existsByNombreProductoIgnoreCaseAndLocal_IdLocal(
                datosNuevos.getNombreProducto(), existente.getLocal().getIdLocal())) {
            throw new IllegalArgumentException(
                    "El producto '" + datosNuevos.getNombreProducto()
                            + "' ya existe en este local");
        }

        existente.setNombreProducto(datosNuevos.getNombreProducto());
        existente.setCategoria(datosNuevos.getCategoria());
        return productoRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!productoRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Producto con id " + id + " no encontrado");
        }
        productoRepository.deleteById(id);
    }
}
