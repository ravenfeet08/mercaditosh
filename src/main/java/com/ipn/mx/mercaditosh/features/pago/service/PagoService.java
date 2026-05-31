package com.ipn.mx.mercaditosh.features.pago.service;

import com.ipn.mx.mercaditosh.core.entidades.Pago;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PagoService {

    List<Pago> obtenerTodos();

    Pago obtenerPorId(Integer id);

    List<Pago> obtenerPorLocatario(Integer idLocatario);

    List<Pago> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta);

    List<Pago> obtenerPorLocatarioYRangoFecha(
            Integer idLocatario, LocalDate desde, LocalDate hasta);

    List<Pago> obtenerPorMercado(Integer idMercado);

    BigDecimal obtenerTotalPorLocatario(Integer idLocatario);

    BigDecimal obtenerTotalPorRangoFecha(LocalDate desde, LocalDate hasta);

    Pago guardar(Pago pago, Integer idLocatario);

    Pago actualizar(Integer id, Pago pago);

    void eliminar(Integer id);
}