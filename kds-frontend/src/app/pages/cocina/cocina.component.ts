import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { PedidoService } from '../../services/pedido.service';

@Component({
  selector: 'app-cocina',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cocina.component.html',
  styleUrls: ['./cocina.component.css']
})
export class CocinaComponent implements OnInit, OnDestroy {

  pedidos: any[] = [];
  cargando = false;

  intervaloPedidos?: any;
  intervaloTiempo?: any;

  pedidosVistos = new Set<number>();

  audioActivo = false;
  audio?: HTMLAudioElement;

  constructor(private pedidoService: PedidoService) {}

  ngOnInit(): void {
    this.activarAudio();
    this.cargarPedidos();

    this.intervaloPedidos = setInterval(() => {
      this.cargarPedidos(false);
    }, 5000);

    this.intervaloTiempo = setInterval(() => {
      this.actualizarContadores();
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.intervaloPedidos) {
      clearInterval(this.intervaloPedidos);
    }

    if (this.intervaloTiempo) {
      clearInterval(this.intervaloTiempo);
    }
  }

  activarAudio(): void {
    document.addEventListener('click', () => {
      if (!this.audioActivo) {
        this.audio = new Audio('/audio/audionotificacion.mp3');
        this.audio.volume = 1;
        this.audio.preload = 'auto';

        this.audio.play()
          .then(() => {
            this.audio?.pause();

            if (this.audio) {
              this.audio.currentTime = 0;
            }

            this.audioActivo = true;
          })
          .catch(() => {});
      }
    }, { once: true });
  }

  reproducirSonido(): void {
    if (!this.audioActivo || !this.audio) return;

    try {
      this.audio.pause();
      this.audio.currentTime = 0;
      this.audio.play().catch(() => {});
    } catch (error) {
      console.log('Error audio:', error);
    }
  }

  cargarPedidos(mostrarCarga: boolean = true): void {
    if (mostrarCarga) {
      this.cargando = true;
    }

    this.pedidoService.listarPedidos().subscribe({
      next: (data) => {
        const pedidosActivos = (data || []).filter(p =>
          p.estado === 'PENDIENTE' ||
          p.estado === 'EN_PREPARACION' ||
          p.estado === 'LISTO'
        );

        pedidosActivos.forEach(p => {
          if (!this.pedidosVistos.has(Number(p.id))) {
            this.pedidosVistos.add(Number(p.id));

            if (p.estado === 'PENDIENTE') {
              this.reproducirSonido();
            }
          }
        });

        this.pedidos = pedidosActivos;
        this.actualizarContadores();

        this.cargando = false;
      },
      error: (error) => {
        console.error('Error cargando pedidos:', error);
        this.cargando = false;
      }
    });
  }

  cambiarEstado(id: number, estado: string): void {
    this.pedidoService.cambiarEstado(id, estado).subscribe({
      next: () => {
        this.cargarPedidos(false);
      },
      error: (error) => {
        console.error('Error cambiando estado:', error);
        alert('No se pudo cambiar el estado del pedido.');
      }
    });
  }

  contarPorEstado(estado: string): number {
    return this.pedidos.filter(p => p.estado === estado).length;
  }

  formatearEstado(estado: string): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'Pendiente';
      case 'EN_PREPARACION':
        return 'En preparación';
      case 'LISTO':
        return 'Listo';
      default:
        return estado;
    }
  }

  calcularTiempo(createdAt: string): string {
    if (!createdAt) {
      return 'Sin hora';
    }

    const fechaPedido = new Date(createdAt);
    const ahora = new Date();

    const diferenciaMs = ahora.getTime() - fechaPedido.getTime();
    const minutos = Math.floor(diferenciaMs / 60000);

    if (minutos <= 0) {
      return 'Hace instantes';
    }

    if (minutos === 1) {
      return 'Hace 1 min';
    }

    return `Hace ${minutos} min`;
  }

  actualizarContadores(): void {
    this.pedidos = this.pedidos.map(pedido => {
      if (pedido.estado === 'EN_PREPARACION' && pedido.inicioPreparacion) {
        const inicio = new Date(pedido.inicioPreparacion).getTime();
        const ahora = new Date().getTime();

        const segundosPreparacion = Math.max(
          0,
          Math.floor((ahora - inicio) / 1000)
        );

        const limiteSegundos = 600;
        const segundosRestantes = Math.max(0, limiteSegundos - segundosPreparacion);
        const segundosExcedidos = Math.max(0, segundosPreparacion - limiteSegundos);

        return {
          ...pedido,
          segundosPreparacion,
          segundosRestantes,
          segundosExcedidos,
          tiempoPreparacion: this.formatearTiempo(segundosPreparacion),
          tiempoRestante: this.formatearTiempo(segundosRestantes),
          tiempoExcedido: this.formatearTiempo(segundosExcedidos),
          atrasado: segundosPreparacion >= limiteSegundos
        };
      }

      return {
        ...pedido,
        segundosPreparacion: 0,
        segundosRestantes: 600,
        segundosExcedidos: 0,
        tiempoPreparacion: '00:00',
        tiempoRestante: '10:00',
        tiempoExcedido: '00:00',
        atrasado: false
      };
    });
  }

  formatearTiempo(segundos: number): string {
    const minutos = Math.floor(segundos / 60);
    const seg = segundos % 60;

    const minutosTexto = minutos < 10 ? `0${minutos}` : `${minutos}`;
    const segundosTexto = seg < 10 ? `0${seg}` : `${seg}`;

    return `${minutosTexto}:${segundosTexto}`;
  }

  estaAtrasado(pedido: any): boolean {
    return pedido.estado === 'EN_PREPARACION' && pedido.atrasado === true;
  }

  cerrarSesionCocina(): void {
    this.pedidoService.validarLogoutCocina().subscribe({
      next: (data) => {
        if (!data.puedeCerrarSesion) {
          alert('No puedes cerrar sesión: hay pedidos pendientes o en preparación.');
          return;
        }

        fetch('/logout', {
          method: 'POST'
        }).then(response => {
          if (response.ok) {
            localStorage.removeItem('usuarioKds');
            window.location.href = '/login';
          }
        });
      },
      error: (error) => {
        console.error(error);
        alert('No se pudo validar el cierre de sesión');
      }
    });
  }

  obtenerImagen(imagen: string): string {
    if (!imagen) {
      return '/img/default.png';
    }

    if (imagen.startsWith('http')) {
      return imagen;
    }

    if (imagen.startsWith('/')) {
      return imagen;
    }

    return `/${imagen}`;
  }
}