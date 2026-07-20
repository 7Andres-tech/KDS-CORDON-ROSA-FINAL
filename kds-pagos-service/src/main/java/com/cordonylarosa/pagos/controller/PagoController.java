package com.cordonylarosa.pagos.controller;

import com.cordonylarosa.pagos.service.MercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
public class PagoController {

    private final MercadoPagoService mercadoPagoService;

    public PagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "servicio", "Microservicio Mercado Pago",
                "estado", "OK"
        ));
    }

    @PostMapping("/crear")
    public ResponseEntity<?> crearPago(@RequestBody Map<String, Object> body) {
        try {
            Long pedidoId = Long.valueOf(body.get("pedidoId").toString());
            BigDecimal total = new BigDecimal(body.get("total").toString());

            String urlPago = mercadoPagoService.crearPago(pedidoId, total);

            return ResponseEntity.ok(Map.of(
                    "url", urlPago,
                    "pedidoId", pedidoId,
                    "origen", "microservicio-pagos-railway"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}