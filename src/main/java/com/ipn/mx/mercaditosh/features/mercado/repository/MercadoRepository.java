package com.ipn.mx.mercaditosh.features.mercado.repository;

import com.ipn.mx.mercaditosh.core.entidades.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MercadoRepository extends JpaRepository<Mercado, Integer> {
    //SELECT * FROM mercado WHERE alcaldia = ?
    List<Mercado> findByAlcaldia(String alcaldia);

    //SELECT * FROM mercado WHERE nombre LIKE '%texto%' (busqueda parcial)
    List<Mercado> findByNombreContainingIgnoreCase(String nombre);

    //Verifica si ya existe un mercado con ese nombre exacto (para validar duplicados)
    boolean existsByNombreIgnoreCase(String nombre);
}
