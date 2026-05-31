package com.ipn.mx.mercaditosh.features.inspeccion.repository;

import com.ipn.mx.mercaditosh.core.entidades.Inspeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InspeccionRepository extends JpaRepository<Inspeccion, Integer> {

    // Todas las inspecciones de un local específico
    List<Inspeccion> findByLocal_IdLocal(Integer idLocal);

    // Filtrar por resultado: "aprobado", "condicionado" o "no_aprobado"
    List<Inspeccion> findByResultado(String resultado);

    // Inspecciones de un local filtradas por resultado
    List<Inspeccion> findByLocal_IdLocalAndResultado(Integer idLocal, String resultado);

    // Inspecciones en un rango de fechas
    List<Inspeccion> findByFechaBetween(LocalDate desde, LocalDate hasta);

    // Inspecciones de todos los locales de un mercado
    // Navega: inspeccion → local → mercado
    List<Inspeccion> findByLocal_Mercado_IdMercado(Integer idMercado);

    // Inspecciones de un mercado filtradas por resultado
    List<Inspeccion> findByLocal_Mercado_IdMercadoAndResultado(
            Integer idMercado, String resultado);

    // La inspección más reciente de un local (útil para mostrar estado actual)
    Optional<Inspeccion> findTopByLocal_IdLocalOrderByFechaDesc(Integer idLocal);

    // Inspecciones con observaciones (las que tuvieron comentarios)
    List<Inspeccion> findByObservacionesIsNotNull();

    // Traer todas con el local y mercado cargados en una sola query (evita N+1)
    @Query("SELECT i FROM Inspeccion i JOIN FETCH i.local l JOIN FETCH l.mercado")
    List<Inspeccion> findAllConLocal();

    // Inspecciones no aprobadas de los últimos N días (alerta sanitaria)
    @Query("SELECT i FROM Inspeccion i JOIN FETCH i.local l JOIN FETCH l.mercado " +
            "WHERE i.resultado = 'no_aprobado' AND i.fecha >= :desde")
    List<Inspeccion> findNoAprobadasDesde(@Param("desde") LocalDate desde);

    // Conteo de inspecciones por resultado para un mercado (resumen ejecutivo)
    @Query("SELECT i.resultado, COUNT(i) FROM Inspeccion i " +
            "WHERE i.local.mercado.idMercado = :idMercado " +
            "GROUP BY i.resultado")
    List<Object[]> countByResultadoParaMercado(@Param("idMercado") Integer idMercado);
}