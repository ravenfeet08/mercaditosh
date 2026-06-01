package com.ipn.mx.mercaditosh.features.pago.service.impl;

import com.ipn.mx.mercaditosh.core.entidades.Locatario;
import com.ipn.mx.mercaditosh.core.entidades.Pago;
import com.ipn.mx.mercaditosh.features.locatario.repository.LocatarioRepository;
import com.ipn.mx.mercaditosh.features.mail.service.EmailService;
import com.ipn.mx.mercaditosh.features.pago.repository.PagoRepository;
import com.ipn.mx.mercaditosh.features.pago.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final LocatarioRepository locatarioRepository;
    private final EmailService emailService;

    // ---------------------------------------------------------------
    // LECTURA
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<Pago> obtenerTodos() {
        return pagoRepository.findAllConLocatario();
    }

    @Override
    @Transactional(readOnly = true)
    public Pago obtenerPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pago con id " + id + " no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> obtenerPorLocatario(Integer idLocatario) {
        if (!locatarioRepository.existsById(idLocatario)) {
            throw new NoSuchElementException(
                    "Locatario con id " + idLocatario + " no encontrado");
        }
        return pagoRepository.findByLocatario_IdLocatario(idLocatario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> obtenerPorRangoFecha(LocalDate desde, LocalDate hasta) {
        validarRangoFecha(desde, hasta);
        return pagoRepository.findByFechaPagoBetween(desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> obtenerPorLocatarioYRangoFecha(
            Integer idLocatario, LocalDate desde, LocalDate hasta) {
        validarRangoFecha(desde, hasta);
        if (!locatarioRepository.existsById(idLocatario)) {
            throw new NoSuchElementException(
                    "Locatario con id " + idLocatario + " no encontrado");
        }
        return pagoRepository.findByLocatario_IdLocatarioAndFechaPagoBetween(
                idLocatario, desde, hasta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> obtenerPorMercado(Integer idMercado) {
        return pagoRepository.findByLocatario_Local_Mercado_IdMercado(idMercado);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPorLocatario(Integer idLocatario) {
        if (!locatarioRepository.existsById(idLocatario)) {
            throw new NoSuchElementException(
                    "Locatario con id " + idLocatario + " no encontrado");
        }
        return pagoRepository.sumMontoByLocatario(idLocatario);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalPorRangoFecha(LocalDate desde, LocalDate hasta) {
        validarRangoFecha(desde, hasta);
        return pagoRepository.sumMontoByRangoFecha(desde, hasta);
    }

    // ---------------------------------------------------------------
    // ESCRITURA
    // ---------------------------------------------------------------

    @Override
    @Transactional
    public Pago guardar(Pago pago, Integer idLocatario) {
        // 1. Verificar que el locatario existe
        Locatario locatario = locatarioRepository.findById(idLocatario)
                .orElseThrow(() -> new NoSuchElementException(
                        "Locatario con id " + idLocatario + " no encontrado"));

        // 2. La fecha de pago se asigna automáticamente si no viene en el request
        if (pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDate.now());
        }

        // 3. No permitir fechas de pago futuras
        if (pago.getFechaPago().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de pago no puede ser una fecha futura");
        }

        pago.setLocatario(locatario);

        // Enviar correo de confirmación después de persistir
        // En un caso real el locatario tendría un campo "email" —
        // por ahora puedes hardcodear tu correo para la prueba
        try {
            emailService.enviarConfirmacionPago(
                    "tu_correo@gmail.com",
                    locatario.getNombre(),
                    pago.getMonto().toString(),
                    pago.getFechaPago().toString()
            );
        } catch (Exception e) {
            log.warn("Correo no enviado, pero el pago se registró correctamente: {}",
                    e.getMessage());
        }

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    public Pago actualizar(Integer id, Pago datosNuevos) {
        Pago existente = obtenerPorId(id);

        // En un pago solo se permite corregir monto y fecha
        // (el locatario no cambia — sería otro pago distinto)
        if (datosNuevos.getFechaPago() != null) {
            if (datosNuevos.getFechaPago().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La fecha de pago no puede ser una fecha futura");
            }
            existente.setFechaPago(datosNuevos.getFechaPago());
        }

        if (datosNuevos.getMonto() != null) {
            existente.setMonto(datosNuevos.getMonto());
        }

        return pagoRepository.save(existente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (!pagoRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Pago con id " + id + " no encontrado");
        }
        pagoRepository.deleteById(id);
    }

    // ---------------------------------------------------------------
    // Utilidad interna
    // ---------------------------------------------------------------
    private void validarRangoFecha(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha 'desde' no puede ser posterior a la fecha 'hasta'");
        }
    }
}
