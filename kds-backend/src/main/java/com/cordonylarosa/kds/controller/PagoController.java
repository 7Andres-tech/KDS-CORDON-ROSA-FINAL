package com.cordonylarosa.kds.controller;

import com.cordonylarosa.kds.service.MercadoPagoService;
import com.cordonylarosa.kds.service.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PedidoService pedidoService;

    public PagoController(MercadoPagoService mercadoPagoService, PedidoService pedidoService) {
        this.mercadoPagoService = mercadoPagoService;
        this.pedidoService = pedidoService;
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearPago(@RequestBody Map<String, Object> body) {
        try {
            Long pedidoId = Long.valueOf(body.get("pedidoId").toString());
            BigDecimal total = new BigDecimal(body.get("total").toString());

            String urlPago = mercadoPagoService.crearPago(pedidoId, total);

            return ResponseEntity.ok(Map.of(
                    "url", urlPago
            ));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
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