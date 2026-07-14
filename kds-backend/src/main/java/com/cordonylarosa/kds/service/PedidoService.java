package com.cordonylarosa.kds.service;

import com.cordonylarosa.kds.dto.PedidoItemRequest;
import com.cordonylarosa.kds.dto.PedidoRequest;
import com.cordonylarosa.kds.entity.EstadoPedido;
import com.cordonylarosa.kds.entity.Pedido;
import com.cordonylarosa.kds.entity.PedidoItem;
import com.cordonylarosa.kds.entity.Producto;
import com.cordonylarosa.kds.repository.PedidoRepository;
import com.cordonylarosa.kds.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequest request) {
        Pedido pedido = new Pedido();

        pedido.setEstado(EstadoPedido.PENDIENTE);

        pedido.setMetodoPago(
                request.metodoPago() != null ? request.metodoPago() : "EFECTIVO"
        );

        pedido.setEstadoPago(
                request.estadoPago() != null ? request.estadoPago() : "PAGADO"
        );

        pedido.setReferenciaPago(
                request.referenciaPago() != null ? request.referenciaPago() : ""
        );

        BigDecimal total = BigDecimal.ZERO;

        for (PedidoItemRequest itemReq : request.items()) {
            Producto producto = productoRepository.findById(itemReq.productoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemReq.productoId()));

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(itemReq.cantidad()));

            PedidoItem item = new PedidoItem();
            item.setPedido(pedido);
            item.setProducto(producto);
            item.setNombreProducto(producto.getNombre());
            item.setCantidad(itemReq.cantidad());
            item.setPrecioUnitario(producto.getPrecio());
            item.setSubtotal(subtotal);
            item.setImagen(producto.getImagen());

            pedido.getItems().add(item);
            total = total.add(subtotal);
        }

        pedido.setTotal(total);

        Pedido guardado = pedidoRepository.save(pedido);

        log.info("Pedido creado con id {} | metodoPago: {} | estadoPago: {}",
                guardado.getId(),
                guardado.getMetodoPago(),
                guardado.getEstadoPago()
        );

        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll()
                .stream()

                // No mostrar pedidos entregados
                .filter(p -> p.getEstado() != EstadoPedido.ENTREGADO)

                // No mostrar en cocina pedidos pendientes de pago
                .filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPago()))

                .sorted(Comparator
                        .comparing(this::prioridadEstado)
                        .thenComparing(p -> p.getInicioPreparacion() != null
                                ? p.getInicioPreparacion()
                                : p.getCreatedAt()
                        )
                )

                .toList();
    }

    @Transactional
    public Pedido marcarPagoAprobado(Long pedidoId, String referenciaPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedidoId));

        pedido.setEstadoPago("PAGADO");
        pedido.setMetodoPago("MERCADO_PAGO");
        pedido.setReferenciaPago(referenciaPago);

        Pedido actualizado = pedidoRepository.save(pedido);

        log.info("Pago aprobado para pedido {} | referenciaPago: {}",
                pedidoId,
                referenciaPago
        );

        return actualizado;
    }

    @Transactional
    public Pedido cambiarEstado(Long id, String estadoTexto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        if (estadoTexto == null || estadoTexto.isBlank()) {
            throw new RuntimeException("El estado no puede venir vacío");
        }

        String estadoNormalizado = estadoTexto.trim().toUpperCase(Locale.ROOT);
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(estadoNormalizado);

        pedido.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoPedido.EN_PREPARACION && pedido.getInicioPreparacion() == null) {
            pedido.setInicioPreparacion(LocalDateTime.now());
        }

        log.info("Pedido {} cambió a estado {}", id, nuevoEstado);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido marcarEntregado(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));

        pedido.setEstado(EstadoPedido.ENTREGADO);

        log.info("Pedido {} marcado como ENTREGADO", id);

        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public boolean puedeCerrarSesionCaja() {
        return pedidoRepository.findAll()
                .stream()
                .anyMatch(p ->
                        p.getEstado() == EstadoPedido.LISTO &&
                        "PAGADO".equalsIgnoreCase(p.getEstadoPago())
                ) == false;
    }

    @Transactional(readOnly = true)
    public boolean puedeCerrarSesionCocina() {
        return pedidoRepository.findAll()
                .stream()
                .noneMatch(p ->
                        "PAGADO".equalsIgnoreCase(p.getEstadoPago()) &&
                        (
                                p.getEstado() == EstadoPedido.PENDIENTE ||
                                p.getEstado() == EstadoPedido.EN_PREPARACION
                        )
                );
    }

    private int prioridadEstado(Pedido pedido) {
        return switch (pedido.getEstado()) {
            case LISTO -> 1;
            case EN_PREPARACION -> 2;
            case PENDIENTE -> 3;
            case ENTREGADO -> 4;
        };
    }
}