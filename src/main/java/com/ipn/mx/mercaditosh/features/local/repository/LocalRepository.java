package com.ipn.mx.mercaditosh.features.local.repository;

import com.ipn.mx.mercaditosh.core.entidades.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalRepository extends JpaRepository<Local, Integer> {
    // Todos los locales de un mercado específico
    // Spring Data navega la relación: local.mercado.idMercado
    List<Local> findByMercado_IdMercado(Integer idMercado);

    // Filtrar por estado: "ocupado", "disponible" o "en_mantenimiento"
    List<Local> findByEstado(String estado);

    // Filtrar por tipo de comercio (frutas, carnes, abarrotes…)
    List<Local> findByTipoLocalContainingIgnoreCase(String tipoLocal);

    // Contar cuántos locales ocupados tiene un mercado (útil para reportes)
    long countByMercado_IdMercadoAndEstado(Integer idMercado, String estado);

    // Verificar si ya existe ese número de local dentro del mismo mercado
    // (para evitar duplicados como dos "Local 5" en el mismo mercado)
    boolean existsByNumeroLocalAndMercado_IdMercado(Integer numeroLocal, Integer idMercado);

    // Consulta JPQL personalizada: locales con su mercado cargado en una sola query
    // Evita el problema N+1 al listar todos los locales con información del mercado
    @Query("SELECT l FROM Local l JOIN FETCH l.mercado")
    List<Local> findAllConMercado();

    // Locales de un mercado filtrados también por estado
    @Query("SELECT l FROM Local l WHERE l.mercado.idMercado = :idMercado AND l.estado = :estado")
    List<Local> findByMercadoYEstado(
            @Param("idMercado") Integer idMercado,
            @Param("estado") String estado);
}
