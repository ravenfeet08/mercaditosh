package com.ipn.mx.mercaditosh.features.locatario.repository;

import com.ipn.mx.mercaditosh.core.entidades.Locatario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LocatarioRepository extends JpaRepository<Locatario, Integer> {

    // Todos los locatarios de un local específico
    List<Locatario> findByLocal_IdLocal(Integer idLocal);

    // Búsqueda por nombre parcial (útil para buscadores en el frontend)
    List<Locatario> findByNombreContainingIgnoreCase(String nombre);

    // Locatarios registrados en un rango de fechas (para reportes)
    List<Locatario> findByFechaRegistroBetween(LocalDate desde, LocalDate hasta);

    // Locatarios de todos los locales de un mercado en particular
    // Navega: locatario → local → mercado
    List<Locatario> findByLocal_Mercado_IdMercado(Integer idMercado);

    // Verifica si ya existe un locatario con ese teléfono
    boolean existsByTelefono(String telefono);

    // Trae todos los locatarios con su local cargado en una sola query (evita N+1)
    @Query("SELECT l FROM Locatario l JOIN FETCH l.local lc JOIN FETCH lc.mercado")
    List<Locatario> findAllConLocal();

    // Locatarios registrados después de una fecha (útil para filtros recientes)
    @Query("SELECT l FROM Locatario l WHERE l.fechaRegistro >= :fecha")
    List<Locatario> findRegistradosDesde(@Param("fecha") LocalDate fecha);
}
