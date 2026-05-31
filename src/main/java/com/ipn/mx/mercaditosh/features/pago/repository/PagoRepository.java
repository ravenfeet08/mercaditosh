package com.ipn.mx.mercaditosh.features.pago.repository;

import com.ipn.mx.mercaditosh.core.entidades.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // Todos los pagos de un locatario
    List<Pago> findByLocatario_IdLocatario(Integer idLocatario);

    // Pagos en un rango de fechas (reportes mensuales, anuales, etc.)
    List<Pago> findByFechaPagoBetween(LocalDate desde, LocalDate hasta);

    // Pagos de un locatario en un rango de fechas (historial individual)
    List<Pago> findByLocatario_IdLocatarioAndFechaPagoBetween(
            Integer idLocatario, LocalDate desde, LocalDate hasta);

    // Pagos cuyo monto supera cierto valor (detectar pagos grandes)
    List<Pago> findByMontoGreaterThanEqual(BigDecimal montoMinimo);

    // Todos los pagos de los locatarios de un mercado en particular
    // Navega: pago → locatario → local → mercado
    List<Pago> findByLocatario_Local_Mercado_IdMercado(Integer idMercado);

    // Suma total de pagos de un locatario (reporte de deuda/historial)
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
            "WHERE p.locatario.idLocatario = :idLocatario")
    BigDecimal sumMontoByLocatario(@Param("idLocatario") Integer idLocatario);

    // Suma total de pagos recibidos en un rango de fechas (reporte de ingresos)
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p " +
            "WHERE p.fechaPago BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByRangoFecha(
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    // Traer todos con locatario cargado en una sola query (evita N+1)
    @Query("SELECT p FROM Pago p JOIN FETCH p.locatario l JOIN FETCH l.local lc JOIN FETCH lc.mercado")
    List<Pago> findAllConLocatario();
}
