package com.cordonylarosa.pagos.service;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
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

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.backend-url}")
    private String backendUrl;

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

        String webhookUrl = backendUrl + "/api/pagos/webhook?pedidoId=" + pedidoId;

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
}