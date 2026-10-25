package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.lacasajojo.app.data.entity.CategoriaProducto
import cl.lacasajojo.app.data.entity.ProductoEntity
import cl.lacasajojo.app.data.repository.AuthRepository
import cl.lacasajojo.app.data.repository.MenuRepository
import cl.lacasajojo.app.data.repository.PedidoRepository
import cl.lacasajojo.app.data.repository.PedidoResultado
import cl.lacasajojo.app.data.repository.SolicitudPedido
import cl.lacasajojo.app.util.Tiempo
import cl.lacasajojo.app.util.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID

/** Estado de la pantalla "Nuevo pedido". Las propiedades calculadas no se guardan: salen de los datos. */
data class NuevoPedidoUi(
    val cargando: Boolean = true,
    /** Menú publicado agrupado por categoría (incluye productos no disponibles, que se muestran bloqueados). */
    val menu: Map<CategoriaProducto, List<ProductoEntity>> = emptyMap(),
    /** Producto elegido por categoría (máximo uno). */
    val seleccion: Map<CategoriaProducto, Long> = emptyMap(),
    val horaRetiro: LocalTime? = null,
    val especificaciones: String = "",
    val errorHora: String? = null,
    val errorGeneral: String? = null,
    val mostrarResumen: Boolean = false,
    val enviando: Boolean = false,
    val pedidoCreadoId: String? = null
) {
    val hayMenu: Boolean
        get() = menu.isNotEmpty()

    val platoDeFondoElegido: Boolean
        get() = seleccion.containsKey(CategoriaProducto.PLATO_FONDO)

    /** Productos elegidos, en el orden de las categorías. */
    val productosSeleccionados: List<ProductoEntity>
        get() = CategoriaProducto.entries.mapNotNull { categoria ->
            val id = seleccion[categoria]
            menu[categoria].orEmpty().firstOrNull { it.id == id }
        }

    /** Total del pedido: suma automática de los precios de los productos elegidos. */
    val total: Int
        get() = productosSeleccionados.sumOf { it.precio }

    /** Lo que todavía falta para poder confirmar. */
    val pendientes: List<String>
        get() {
            val lista = mutableListOf<String>()
            if (!platoDeFondoElegido) lista.add("Elige un plato de fondo")
            if (horaRetiro == null) lista.add("Elige la hora de retiro")
            return lista
        }

    val puedeConfirmar: Boolean
        get() = pendientes.isEmpty() && !enviando
}

/** Lógica de la creación de un pedido: selección, hora de retiro, resumen y confirmación. */
class NuevoPedidoViewModel(
    private val auth: AuthRepository,
    menu: MenuRepository,
    private val pedidos: PedidoRepository,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    /**
     * Identificador único del pedido, generado una sola vez al abrir la pantalla. Si el botón
     * "Confirmar" se pulsa varias veces, siempre se intenta guardar el mismo id y no se duplica.
     */
    private val idPedido: String = UUID.randomUUID().toString()

    /** Lo que el usuario ha ido eligiendo (el menú llega por separado desde Room). */
    private data class Entrada(
        val seleccion: Map<CategoriaProducto, Long> = emptyMap(),
        val horaRetiro: LocalTime? = null,
        val especificaciones: String = "",
        val errorHora: String? = null,
        val errorGeneral: String? = null,
        val mostrarResumen: Boolean = false,
        val enviando: Boolean = false,
        val pedidoCreadoId: String? = null
    )

    private val entrada = MutableStateFlow(Entrada())

    val ui: StateFlow<NuevoPedidoUi> = combine(menu.observarPublicados(), entrada) { productos, e ->
        val porCategoria = productos.groupBy { it.categoria }
        // Si un producto elegido deja de estar disponible, se quita de la selección.
        val seleccionValida = e.seleccion.filter { (categoria, id) ->
            porCategoria[categoria].orEmpty().any { it.id == id && it.disponible }
        }
        NuevoPedidoUi(
            cargando = false,
            menu = porCategoria,
            seleccion = seleccionValida,
            horaRetiro = e.horaRetiro,
            especificaciones = e.especificaciones,
            errorHora = e.errorHora,
            errorGeneral = e.errorGeneral,
            mostrarResumen = e.mostrarResumen,
            enviando = e.enviando,
            pedidoCreadoId = e.pedidoCreadoId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NuevoPedidoUi())

    /** Elige un producto de la categoría; con productoId = null quita la selección (categorías opcionales). */
    fun seleccionar(categoria: CategoriaProducto, productoId: Long?) {
        entrada.update { actual ->
            val nueva = if (productoId == null) {
                actual.seleccion - categoria
            } else {
                actual.seleccion + (categoria to productoId)
            }
            actual.copy(seleccion = nueva, errorGeneral = null)
        }
    }

    fun cambiarHora(hora: LocalTime) {
        entrada.update { it.copy(horaRetiro = hora, errorHora = null, errorGeneral = null) }
    }

    fun cambiarEspecificaciones(texto: String) {
        entrada.update {
            it.copy(especificaciones = texto.take(Validaciones.MAX_ESPECIFICACIONES), errorGeneral = null)
        }
    }

    /** Valida el pedido y, si es correcto, muestra el resumen para confirmarlo. */
    fun solicitarConfirmacion() {
        val actual = ui.value
        if (actual.enviando || actual.pedidoCreadoId != null) return

        val ahora = reloj()
        val hora = actual.horaRetiro
        val errores = Validaciones.validarPedido(
            hayPlatoDeFondo = actual.platoDeFondoElegido,
            horaRetiroMillis = if (hora == null) null else Tiempo.horaDeHoyAMillis(hora, ahora),
            ahoraMillis = ahora,
            especificaciones = actual.especificaciones
        )
        if (errores.hayErrores) {
            entrada.update {
                it.copy(
                    errorHora = errores.horaRetiro,
                    errorGeneral = errores.platoDeFondo ?: errores.especificaciones
                )
            }
            return
        }
        entrada.update { it.copy(mostrarResumen = true, errorHora = null, errorGeneral = null) }
    }

    fun cancelarConfirmacion() {
        if (entrada.value.enviando) return
        entrada.update { it.copy(mostrarResumen = false, errorGeneral = null) }
    }

    /** Guarda el pedido en Room. Ignora pulsaciones repetidas mientras se está enviando. */
    fun confirmar() {
        val estadoEntrada = entrada.value
        if (estadoEntrada.enviando || estadoEntrada.pedidoCreadoId != null) return

        val actual = ui.value
        val hora = actual.horaRetiro ?: return

        // Se marca "enviando" de inmediato (en el hilo principal) para bloquear pulsaciones repetidas.
        entrada.update { it.copy(enviando = true, errorGeneral = null) }

        viewModelScope.launch {
            // Pausa de 3 segundos para que se luzca la animación al confirmar
            kotlinx.coroutines.delay(3000)

            val cliente = auth.usuarioActual.first()
            if (cliente == null) {
                entrada.update {
                    it.copy(
                        enviando = false,
                        mostrarResumen = false,
                        errorGeneral = "Tu sesión ya no está activa. Inicia sesión nuevamente."
                    )
                }
                return@launch
            }

            val solicitud = SolicitudPedido(
                idPedido = idPedido,
                cliente = cliente,
                seleccion = actual.seleccion,
                especificaciones = actual.especificaciones,
                horaRetiroMillis = Tiempo.horaDeHoyAMillis(hora, reloj())
            )

            when (val resultado = pedidos.crearPedido(solicitud)) {
                is PedidoResultado.Exito ->
                    // Se deja "enviando" en true hasta salir de la pantalla: no se puede confirmar de nuevo.
                    entrada.update {
                        it.copy(mostrarResumen = false, pedidoCreadoId = resultado.pedidoId)
                    }
                is PedidoResultado.Invalido ->
                    entrada.update {
                        it.copy(
                            enviando = false,
                            mostrarResumen = false,
                            errorHora = resultado.errores.horaRetiro,
                            errorGeneral = resultado.errores.platoDeFondo ?: resultado.errores.especificaciones
                        )
                    }
                is PedidoResultado.Rechazado ->
                    entrada.update {
                        it.copy(
                            enviando = false,
                            mostrarResumen = false,
                            errorGeneral = resultado.mensaje
                        )
                    }
            }
        }
    }
}
