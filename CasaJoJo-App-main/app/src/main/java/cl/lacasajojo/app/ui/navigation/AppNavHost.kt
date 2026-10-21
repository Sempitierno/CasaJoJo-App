package cl.lacasajojo.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.lacasajojo.app.data.AppContainer
import cl.lacasajojo.app.ui.screens.admin.AdminHomeScreen
import cl.lacasajojo.app.ui.screens.admin.AdminMenuScreen
import cl.lacasajojo.app.ui.screens.admin.AdminPedidosScreen
import cl.lacasajojo.app.ui.screens.admin.ProductoFormScreen
import cl.lacasajojo.app.ui.screens.auth.InicioScreen
import cl.lacasajojo.app.ui.screens.auth.LoginScreen
import cl.lacasajojo.app.ui.screens.auth.RegistroScreen
import cl.lacasajojo.app.ui.screens.cliente.ClienteHomeScreen
import cl.lacasajojo.app.ui.screens.cliente.ClienteMenuScreen
import cl.lacasajojo.app.ui.screens.cliente.DetallePedidoScreen
import cl.lacasajojo.app.ui.screens.cliente.HistorialScreen
import cl.lacasajojo.app.ui.screens.cliente.NuevoPedidoScreen
import cl.lacasajojo.app.ui.screens.cliente.PedidoConfirmadoScreen
import cl.lacasajojo.app.viewmodel.AdminHomeViewModel
import cl.lacasajojo.app.viewmodel.AdminMenuViewModel
import cl.lacasajojo.app.viewmodel.AdminPedidosViewModel
import cl.lacasajojo.app.viewmodel.ClienteHomeViewModel
import cl.lacasajojo.app.viewmodel.ClienteMenuViewModel
import cl.lacasajojo.app.viewmodel.DetallePedidoViewModel
import cl.lacasajojo.app.viewmodel.EstadoSesion
import cl.lacasajojo.app.viewmodel.HistorialViewModel
import cl.lacasajojo.app.viewmodel.LoginViewModel
import cl.lacasajojo.app.viewmodel.NuevoPedidoViewModel
import cl.lacasajojo.app.viewmodel.ProductoFormViewModel
import cl.lacasajojo.app.viewmodel.RegistroViewModel
import cl.lacasajojo.app.viewmodel.SesionViewModel
import cl.lacasajojo.app.viewmodel.factoryOf

/**
 * Grafo de navegación de toda la app.
 *
 * Cada pantalla tiene su propio ViewModel, ligado a su entrada en la pila de navegación: al salir de la
 * pantalla (botón Atrás) se descarta. Al cerrar sesión se vacía la pila, por lo que los datos de un usuario
 * nunca quedan en memoria para el siguiente.
 */
@Composable
fun AppNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val sesionVm: SesionViewModel = viewModel(
        factory = factoryOf { SesionViewModel(container.authRepository) }
    )
    val estadoSesion by sesionVm.estado.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Rutas.INICIO) {

        // ---------- Arranque y autenticación ----------
        composable(Rutas.INICIO) {
            InicioScreen(
                estado = estadoSesion,
                onSinSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                },
                onSesionActiva = { rol ->
                    navController.navigate(Rutas.inicioDe(rol)) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.LOGIN) {
            val vm: LoginViewModel = viewModel(
                factory = factoryOf { LoginViewModel(container.authRepository) }
            )
            LoginScreen(
                viewModel = vm,
                onLoginExitoso = { rol ->
                    navController.navigate(Rutas.inicioDe(rol)) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
            )
        }

        composable(Rutas.REGISTRO) {
            val vm: RegistroViewModel = viewModel(
                factory = factoryOf { RegistroViewModel(container.authRepository) }
            )
            RegistroScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onRegistroExitoso = { navController.popBackStack() }
            )
        }

        // ---------- Administrador ----------
        composable(Rutas.ADMIN_HOME) {
            val vm: AdminHomeViewModel = viewModel(
                factory = factoryOf {
                    AdminHomeViewModel(container.menuRepository, container.pedidoRepository)
                }
            )
            AdminHomeScreen(
                nombre = (estadoSesion as? EstadoSesion.Activa)?.usuario?.nombreCompleto.orEmpty(),
                viewModel = vm,
                onMenu = { navController.navigate(Rutas.ADMIN_MENU) },
                onPedidos = { navController.navigate(Rutas.ADMIN_PEDIDOS) },
                onCerrarSesion = {
                    sesionVm.cerrarSesion()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.ADMIN_MENU) {
            val vm: AdminMenuViewModel = viewModel(
                factory = factoryOf { AdminMenuViewModel(container.menuRepository) }
            )
            AdminMenuScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onNuevoProducto = { navController.navigate(Rutas.adminProducto(0L)) },
                onEditarProducto = { id -> navController.navigate(Rutas.adminProducto(id)) }
            )
        }

        composable(
            route = Rutas.ADMIN_PRODUCTO_RUTA,
            arguments = listOf(navArgument(Rutas.ARG_PRODUCTO_ID) { type = NavType.LongType })
        ) { entrada ->
            val productoId = entrada.arguments?.getLong(Rutas.ARG_PRODUCTO_ID) ?: 0L
            val vm: ProductoFormViewModel = viewModel(
                factory = factoryOf { ProductoFormViewModel(container.menuRepository, productoId) }
            )
            ProductoFormScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.ADMIN_PEDIDOS) {
            val vm: AdminPedidosViewModel = viewModel(
                factory = factoryOf { AdminPedidosViewModel(container.pedidoRepository) }
            )
            AdminPedidosScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }

        // ---------- Cliente ----------
        composable(Rutas.CLIENTE_HOME) {
            val vm: ClienteHomeViewModel = viewModel(
                factory = factoryOf {
                    ClienteHomeViewModel(
                        container.authRepository,
                        container.pedidoRepository,
                        container.notificador
                    )
                }
            )
            ClienteHomeScreen(
                nombre = (estadoSesion as? EstadoSesion.Activa)?.usuario?.nombreCompleto.orEmpty(),
                viewModel = vm,
                onMenu = { navController.navigate(Rutas.CLIENTE_MENU) },
                onNuevoPedido = { navController.navigate(Rutas.CLIENTE_NUEVO_PEDIDO) },
                onHistorial = { navController.navigate(Rutas.CLIENTE_HISTORIAL) },
                onCerrarSesion = {
                    sesionVm.cerrarSesion()
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.CLIENTE_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.CLIENTE_MENU) {
            val vm: ClienteMenuViewModel = viewModel(
                factory = factoryOf { ClienteMenuViewModel(container.menuRepository) }
            )
            ClienteMenuScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onHacerPedido = { navController.navigate(Rutas.CLIENTE_NUEVO_PEDIDO) }
            )
        }

        composable(Rutas.CLIENTE_NUEVO_PEDIDO) {
            val vm: NuevoPedidoViewModel = viewModel(
                factory = factoryOf {
                    NuevoPedidoViewModel(
                        container.authRepository,
                        container.menuRepository,
                        container.pedidoRepository
                    )
                }
            )
            NuevoPedidoScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onPedidoCreado = { pedidoId ->
                    // Se reemplaza el formulario por la confirmación: "Atrás" vuelve al panel, no al formulario.
                    navController.navigate(Rutas.clienteConfirmado(pedidoId)) {
                        popUpTo(Rutas.CLIENTE_NUEVO_PEDIDO) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Rutas.CLIENTE_CONFIRMADO_RUTA,
            arguments = listOf(navArgument(Rutas.ARG_PEDIDO_ID) { type = NavType.StringType })
        ) { entrada ->
            val pedidoId = entrada.arguments?.getString(Rutas.ARG_PEDIDO_ID).orEmpty()
            val vm: DetallePedidoViewModel = viewModel(
                factory = factoryOf {
                    DetallePedidoViewModel(container.authRepository, container.pedidoRepository, pedidoId)
                }
            )
            PedidoConfirmadoScreen(
                viewModel = vm,
                onVerDetalle = {
                    navController.navigate(Rutas.clienteDetalle(pedidoId)) {
                        popUpTo(Rutas.CLIENTE_CONFIRMADO_RUTA) { inclusive = true }
                    }
                },
                onVolverInicio = {
                    navController.popBackStack(Rutas.CLIENTE_HOME, inclusive = false)
                }
            )
        }

        composable(Rutas.CLIENTE_HISTORIAL) {
            val vm: HistorialViewModel = viewModel(
                factory = factoryOf {
                    HistorialViewModel(container.authRepository, container.pedidoRepository)
                }
            )
            HistorialScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() },
                onVerPedido = { pedidoId -> navController.navigate(Rutas.clienteDetalle(pedidoId)) },
                onNuevoPedido = { navController.navigate(Rutas.CLIENTE_NUEVO_PEDIDO) }
            )
        }

        composable(
            route = Rutas.CLIENTE_DETALLE_RUTA,
            arguments = listOf(navArgument(Rutas.ARG_PEDIDO_ID) { type = NavType.StringType })
        ) { entrada ->
            val pedidoId = entrada.arguments?.getString(Rutas.ARG_PEDIDO_ID).orEmpty()
            val vm: DetallePedidoViewModel = viewModel(
                factory = factoryOf {
                    DetallePedidoViewModel(container.authRepository, container.pedidoRepository, pedidoId)
                }
            )
            DetallePedidoScreen(
                viewModel = vm,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
