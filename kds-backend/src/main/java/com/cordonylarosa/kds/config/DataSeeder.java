package com.cordonylarosa.kds.config;

import com.cordonylarosa.kds.entity.Producto;
import com.cordonylarosa.kds.entity.Usuario;
import com.cordonylarosa.kds.repository.ProductoRepository;
import com.cordonylarosa.kds.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner cargarDatosIniciales(
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setRol("ROLE_ADMIN");
                admin.setActivo(true);
                usuarioRepository.save(admin);

                Usuario cajero = new Usuario();
                cajero.setUsername("cajero1");
                cajero.setPassword(passwordEncoder.encode("123456"));
                cajero.setRol("ROLE_CAJERO");
                cajero.setActivo(true);
                usuarioRepository.save(cajero);

                Usuario cocinero = new Usuario();
                cocinero.setUsername("cocinero1");
                cocinero.setPassword(passwordEncoder.encode("123456"));
                cocinero.setRol("ROLE_COCINERO");
                cocinero.setActivo(true);
                usuarioRepository.save(cocinero);
            }

            if (productoRepository.count() == 0) {
                crearProducto(productoRepository, "Arroz Chaufa", "Criollo", "18.00", "img/arroz-chaufa.png");
                crearProducto(productoRepository, "Arroz con Mariscos", "Marino", "28.00", "img/arroz-mariscos.png");
                crearProducto(productoRepository, "Arroz", "Guarnición", "5.00", "img/arroz.png");
                crearProducto(productoRepository, "Bistec con Arroz", "Criollo", "22.00", "img/bistek-arroz.png");
                crearProducto(productoRepository, "Bistec a lo Pobre", "Criollo", "25.00", "img/bistek-pobre.png");

                crearProducto(productoRepository, "Causa de Cangrejo", "Entrada", "18.00", "img/causa-cangrejo.png");
                crearProducto(productoRepository, "Causa de Langostino", "Entrada", "20.00", "img/causa-langostino.png");
                crearProducto(productoRepository, "Causa de Pollo", "Entrada", "12.00", "img/causa-pollo.png");

                crearProducto(productoRepository, "Ceviche Mixto Marino", "Marino", "30.00", "img/cebiche-mixto-marino.png.png");
                crearProducto(productoRepository, "Ceviche Mixto", "Marino", "25.00", "img/cebiche-mixto.png");
                crearProducto(productoRepository, "Chaufa de Mariscos", "Marino", "28.00", "img/chaufa-mariscos.png");
                crearProducto(productoRepository, "Chicharrón de Pescado", "Marino", "24.00", "img/chicharron-pescado.png");
                crearProducto(productoRepository, "Chicharrón de Pollo", "Criollo", "20.00", "img/chicharron-pollo.png");
                crearProducto(productoRepository, "Choritos a la Chalaca", "Entrada", "16.00", "img/choritos-chalaca.png");

                crearProducto(productoRepository, "Ensalada Mixta", "Ensaladas", "10.00", "img/ensalada-mixta.png");
                crearProducto(productoRepository, "Fettuccini con Langostinos", "Pastas", "30.00", "img/fettuccini-langostinos.png");
                crearProducto(productoRepository, "Fettuccini Alfredo con Lomo", "Pastas", "28.00", "img/fetuccini-alfredo-lomo.png");
                crearProducto(productoRepository, "Fettuccini a la Huancaína con Lomo", "Pastas", "28.00", "img/fetuccini-huancaina-lomo.png");

                crearProducto(productoRepository, "Frejoles con Asado", "Criollo", "22.00", "img/frejoles-asado.png");
                crearProducto(productoRepository, "Leche de Tigre", "Marino", "14.00", "img/leche-tigre.png");
                crearProducto(productoRepository, "Lomo a la Pimienta", "Criollo", "28.00", "img/lomo-pimienta.png");
                crearProducto(productoRepository, "Lomo Saltado", "Criollo", "25.00", "img/lomo-saltado.png");

                crearProducto(productoRepository, "Milanesa a lo Pobre", "Criollo", "24.00", "img/milanesa-pobre.png");
                crearProducto(productoRepository, "Milanesa de Pollo", "Criollo", "20.00", "img/milanesa-pollo.png");

                crearProducto(productoRepository, "Pallares con Seco", "Criollo", "22.00", "img/pallares-seco.png");
                crearProducto(productoRepository, "Pallares Verdes", "Criollo", "18.00", "img/pallares-verdes.png");
                crearProducto(productoRepository, "Papa a la Huancaína", "Entrada", "10.00", "img/papa-huancaina.png");
                crearProducto(productoRepository, "Papas Fritas", "Guarnición", "8.00", "img/papas-fritas.png");

                crearProducto(productoRepository, "Parihuela", "Marino", "32.00", "img/parihuela.png");
                crearProducto(productoRepository, "Pescado Especial", "Marino", "28.00", "img/pescado-especial.png");
                crearProducto(productoRepository, "Pescado a la Plancha", "Marino", "24.00", "img/pescado-plancha.png");
                crearProducto(productoRepository, "Pesto con Bistec", "Pastas", "22.00", "img/pesto-bistek.png");
                crearProducto(productoRepository, "Pollo a la Plancha", "Criollo", "18.00", "img/pollo-plancha.png");

                crearProducto(productoRepository, "Pulpo al Olivo", "Entrada", "25.00", "img/pulpo-olivo.png");
                crearProducto(productoRepository, "Puré con Asado", "Criollo", "22.00", "img/pure-asado.png");
                crearProducto(productoRepository, "Sopa de Pollo", "Sopas", "12.00", "img/sopa-pollo.png");
                crearProducto(productoRepository, "Spaghetti Alfredo", "Pastas", "20.00", "img/spaguetti-alfredo.png");
                crearProducto(productoRepository, "Sudado de Pescado", "Marino", "26.00", "img/sudado-pescado.png");

                crearProducto(productoRepository, "Tacu Tacu a lo Pobre", "Criollo", "25.00", "img/tacu-tacu-pobre.png");
                crearProducto(productoRepository, "Tacu Tacu con Seco", "Criollo", "24.00", "img/tacu-tacu-seco.png");
                crearProducto(productoRepository, "Tallarín Saltado", "Criollo", "22.00", "img/tallarin-saltado.png");
                crearProducto(productoRepository, "Yuca Frita", "Guarnición", "8.00", "img/yuca-frita.png");
            }
        };
    }

    private void crearProducto(
            ProductoRepository productoRepository,
            String nombre,
            String categoria,
            String precio,
            String imagen
    ) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal(precio));
        producto.setImagen(imagen);
        producto.setActivo(true);

        productoRepository.save(producto);
    }
}