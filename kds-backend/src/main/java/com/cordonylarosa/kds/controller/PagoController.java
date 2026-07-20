package com.cordonylarosa.kds.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.cordonylarosa.kds.service.PedidoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    private final PedidoService pedidoService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${pagos.service.url}")
    private String pagosServiceUrl;

    public PagoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/crear")
public ResponseEntity<?> crearPago(@RequestBody Map<String, Object> body) {
    try {
        System.out.println("====================================");
        System.out.println("SOLICITANDO PAGO AL MICROSERVICIO RAILWAY");
        System.out.println("URL microservicio: " + pagosServiceUrl + "/crear");
        System.out.println("BODY: " + body);
        System.out.println("====================================");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                pagosServiceUrl + "/crear",
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());

    } catch (Exception e) {
        e.printStackTrace();

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Error llamando al microservicio de pagos: " + e.getMessage()
        ));
    }
}

    @PostMapping("/webhook")
    public ResponseEntity<?> recibirWebhook(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam Map<String, String> params
    ) {
        try {
            System.out.println("====================================");
            System.out.println("WEBHOOK MERCADO PAGO RECIBIDO");
            System.out.println("BODY: " + body);
            System.out.println("PARAMS: " + params);
            System.out.println("====================================");

            Long pedidoId = null;

            if (params.get("pedidoId") != null) {
                pedidoId = Long.valueOf(params.get("pedidoId"));
            }

            if (pedidoId != null) {
                System.out.println("Pedido detectado desde webhook: " + pedidoId);

                pedidoService.marcarPagoAprobado(
                        pedidoId,
                        "MERCADO_PAGO_WEBHOOK"
                );

                System.out.println("Pedido " + pedidoId + " marcado como PAGADO correctamente");

                return ResponseEntity.ok(Map.of(
                        "mensaje", "Pedido marcado como pagado",
                        "pedidoId", pedidoId
                ));
            }

            System.out.println("No se detectó pedidoId en el webhook");

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Webhook recibido pero sin pedidoId"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}