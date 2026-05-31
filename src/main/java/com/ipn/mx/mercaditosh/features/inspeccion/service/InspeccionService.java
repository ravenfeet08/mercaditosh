package com.ipn.mx.mercaditosh.features.inspeccion.service;

import com.ipn.mx.mercaditosh.core.entidades.Inspeccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InspeccionService {

    List<Inspeccion> obtenerTodas();

    Inspeccion obtenerPorId(Integer id);

    List<Inspeccion> obtenerPorLocal(Integer idLocal);

    List<Inspeccion> obtenerPorResultado(String resultado);

    List<Inspeccion> obtenerPorLocalYResultado(Integer idLocal, String resultado);

    List<Inspeccion> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta);

    List<Inspeccion> obtenerPorMercado(Integer idMercado);

    List<Inspeccion> obtenerPorMercadoYResultado(Integer idMercado, String resultado);

    Optional<Inspeccion> obtenerUltimaInspeccionDeLocal(Integer idLocal);

    List<Inspeccion> obtenerNoAprobadasRecientes(int dias);

    // Devuelve un mapa resultado → cantidad para un mercado
    // Ejemplo: { "aprobado": 12, "condicionado": 3, "no_aprobado": 1 }
    Map<String, Long> obtenerResumenPorMercado(Integer idMercado);

    Inspeccion guardar(Inspeccion inspeccion, Integer idLocal);

    Inspeccion actualizar(Integer id, Inspeccion inspeccion);

    void eliminar(Integer id);
}
