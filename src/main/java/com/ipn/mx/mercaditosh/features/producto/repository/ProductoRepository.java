package com.ipn.mx.mercaditosh.features.producto.repository;

import com.ipn.mx.mercaditosh.core.entidades.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // Todos los productos de un local específico
    List<Producto> findByLocal_IdLocal(Integer idLocal);

    // Todos los productos de un mercado completo (navega local → mercado)
    List<Producto> findByLocal_Mercado_IdMercado(Integer idMercado);

    // Filtrar por categoría exacta
    List<Producto> findByCategoria(String categoria);

    // Búsqueda parcial por nombre de producto
    List<Producto> findByNombreProductoContainingIgnoreCase(String nombre);

    // Búsqueda parcial por categoría
    List<Producto> findByCategoriaContainingIgnoreCase(String categoria);

    // Verificar si ya existe ese producto en el mismo local (evitar duplicados)
    boolean existsByNombreProductoIgnoreCaseAndLocal_IdLocal(
            String nombreProducto, Integer idLocal);

    // Traer todos con su local y mercado cargados (evita N+1)
    @Query("SELECT p FROM Producto p JOIN FETCH p.local l JOIN FETCH l.mercado")
    List<Producto> findAllConLocal();
}
