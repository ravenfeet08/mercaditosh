package com.ipn.mx.mercaditosh.features.mail.service.impl;

import com.ipn.mx.mercaditosh.features.mail.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j  // Lombok genera un logger: log.info(), log.error(), etc.
public class EmailServiceImpl implements EmailService {

    // JavaMailSender es el bean que Spring Boot autoconfigura con las
    // propiedades spring.mail.* que definiste en application.properties
    private final JavaMailSender mailSender;

    // Lee el remitente desde application.properties para no hardcodearlo
    @Value("${spring.mail.username}")
    private String remitente;

    // ---------------------------------------------------------------
    // Correo simple (texto plano)
    // ---------------------------------------------------------------
    @Override
    public void enviarCorreoSimple(String destinatario, String asunto, String cuerpo) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);

            mailSender.send(mensaje);
            log.info("Correo simple enviado a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar correo simple a {}: {}", destinatario, e.getMessage());
            // No relanzamos la excepción para que el proceso principal no falle
            // si el correo no pudo enviarse (el pago/inspección ya se guardó en BD)
        }
    }

    // ---------------------------------------------------------------
    // Correo HTML (MimeMessage permite HTML y caracteres especiales)
    // ---------------------------------------------------------------
    @Override
    public void enviarCorreoHtml(String destinatario, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();

            // MimeMessageHelper con true = modo multipart (necesario para HTML)
            // "UTF-8" garantiza que los acentos del español se vean bien
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true); // true = el texto es HTML

            mailSender.send(mensaje);
            log.info("Correo HTML enviado a: {}", destinatario);

        } catch (MessagingException e) {
            log.error("Error al enviar correo HTML a {}: {}", destinatario, e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Confirmación de pago recibido
    // Se llama desde PagoServiceImpl.guardar() después de persistir el pago
    // ---------------------------------------------------------------
    @Override
    public void enviarConfirmacionPago(String destinatario, String nombreLocatario,
                                       String monto, String fecha) {
        try {
            String asunto = "Mercaditosh — Confirmación de pago recibido";

            String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: auto;">
                  <div style="background: #2E7D32; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">🏪 Mercaditosh</h1>
                    <p style="color: #C8E6C9; margin: 4px 0 0 0;">
                        Sistema de Gestión de Mercados Públicos CDMX
                    </p>
                  </div>
                  <div style="background: #F9F9F9; padding: 24px; border-radius: 0 0 8px 8px;
                              border: 1px solid #E0E0E0;">
                    <h2 style="color: #2E7D32;">✅ Pago registrado correctamente</h2>
                    <p>Estimado/a <strong>%s</strong>,</p>
                    <p>Se ha registrado el siguiente pago en el sistema:</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                      <tr style="background: #E8F5E9;">
                        <td style="padding: 10px; border: 1px solid #C8E6C9;
                                   font-weight: bold;">Monto</td>
                        <td style="padding: 10px; border: 1px solid #C8E6C9;">
                            $%s MXN
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 10px; border: 1px solid #C8E6C9;
                                   font-weight: bold;">Fecha</td>
                        <td style="padding: 10px; border: 1px solid #C8E6C9;">%s</td>
                      </tr>
                    </table>
                    <p style="color: #757575; font-size: 12px;">
                        Este es un mensaje automático del sistema Mercaditosh — ESCOM IPN.
                        Por favor no responda a este correo.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(nombreLocatario, monto, fecha);
            enviarCorreoHtml(destinatario, asunto, html);
        } catch (Exception e) {
            // El correo falló pero NO rompemos el flujo principal
            log.warn("No se pudo enviar correo de confirmación a {}: {}",
                    destinatario, e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Alerta de inspección sanitaria
    // Se llama desde InspeccionServiceImpl.guardar() cuando el resultado
    // es "no_aprobado" o "condicionado"
    // ---------------------------------------------------------------
    @Override
    public void enviarAlertaInspeccion(String destinatario, String nombreLocal,
                                       String resultado, String observaciones) {

        // Color del encabezado según el resultado
        String colorHeader = switch (resultado) {
            case "aprobado"     -> "#2E7D32";   // verde
            case "condicionado" -> "#E65100";   // naranja
            default             -> "#B71C1C";   // rojo (no_aprobado)
        };

        String emoji = switch (resultado) {
            case "aprobado"     -> "✅";
            case "condicionado" -> "⚠️";
            default             -> "🚨";
        };

        String asunto = "Mercaditosh — Resultado de inspección: "
                + resultado.toUpperCase() + " — " + nombreLocal;

        String obsHtml = (observaciones != null && !observaciones.isBlank())
                ? "<p><strong>Observaciones:</strong> " + observaciones + "</p>"
                : "";

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;
                             max-width: 600px; margin: auto;">
                  <div style="background: %s; padding: 20px; border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">🏪 Mercaditosh</h1>
                    <p style="color: rgba(255,255,255,0.85); margin: 4px 0 0 0;">
                        Resultado de inspección sanitaria
                    </p>
                  </div>
                  <div style="background: #F9F9F9; padding: 24px; border-radius: 0 0 8px 8px;
                              border: 1px solid #E0E0E0;">
                    <h2>%s Inspección: <span style="color: %s;">%s</span></h2>
                    <p><strong>Local inspeccionado:</strong> %s</p>
                    %s
                    <p style="color: #757575; font-size: 12px; margin-top: 24px;">
                        Este es un mensaje automático del sistema Mercaditosh — ESCOM IPN.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(
                colorHeader,
                emoji, colorHeader, resultado.toUpperCase(),
                nombreLocal,
                obsHtml);

        enviarCorreoHtml(destinatario, asunto, html);
    }
}