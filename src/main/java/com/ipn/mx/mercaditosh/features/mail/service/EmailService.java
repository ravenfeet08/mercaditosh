package com.ipn.mx.mercaditosh.features.mail.service;

public interface EmailService {

    // Correo simple de texto plano
    void enviarCorreoSimple(String destinatario, String asunto, String cuerpo);

    // Correo con formato HTML (para notificaciones bonitas)
    void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml);

    // Correo de confirmación de pago — se llama desde PagoServiceImpl
    void enviarConfirmacionPago(String destinatario, String nombreLocatario,
                                String monto, String fecha);

    // Correo de alerta sanitaria — se llama desde InspeccionServiceImpl
    void enviarAlertaInspeccion(String destinatario, String nombreLocal,
                                String resultado, String observaciones);
}
