package com.cordonylarosa.kds.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access.token}")
    private String accessToken;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.public-url}")
    private String publicUrl;

    private final PedidoService pedidoService;

    public MercadoPagoService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public String crearPago(Long pedidoId, BigDecimal total) throws Exception {

        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Pedido KDS #" + pedidoId + " - El Cordón y la Rosa")
                .quantity(1)
                .currencyId("PEN")
                .unitPrice(total)
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/pago-exitoso")
                .failure(frontendUrl + "/pago-error")
                .pending(frontendUrl + "/pago-pendiente")
                .build();

        String webhookUrl = publicUrl + "/api/pagos/webhook?pedidoId=" + pedidoId;

        System.out.println("====================================");
        System.out.println("CREANDO PAGO MERCADO PAGO");
        System.out.println("Pedido ID: " + pedidoId);
        System.out.println("Total: " + total);
        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("====================================");

        PreferenceRequest request = PreferenceRequest.builder()
                .items(List.of(item))
                .backUrls(backUrls)
                .notificationUrl(webhookUrl)
                .externalReference(String.valueOf(pedidoId))
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(request);

        return preference.getInitPoint();
    }

    public void procesarWebhookPago(String paymentId, Long pedidoIdDesdeUrl) throws Exception {
        PaymentClient paymentClient = new PaymentClient();
        Payment payment = paymentClient.get(Long.valueOf(paymentId));

        System.out.println("====================================");
        System.out.println("PAGO CONSULTADO EN MERCADO PAGO");
        System.out.println("Payment ID: " + payment.getId());
        System.out.println("Estado MP: " + payment.getStatus());
        System.out.println("External Reference: " + payment.getExternalReference());
        System.out.println("Pedido ID desde URL: " + pedidoIdDesdeUrl);
        System.out.println("====================================");

        if (!"approved".equalsIgnoreCase(payment.getStatus())) {
            System.out.println("El pago todavía no está aprobado. Estado: " + payment.getStatus());
            return;
        }

        Long pedidoId = pedidoIdDesdeUrl;

        if (pedidoId == null && payment.getExternalReference() != null) {
            pedidoId = Long.valueOf(payment.getExternalReference());
        }

        if (pedidoId == null) {
            throw new RuntimeException("No se pudo identificar el pedido del pago.");
        }

        pedidoService.marcarPagoAprobado(
                pedidoId,
                String.valueOf(payment.getId())
        );
    }
}