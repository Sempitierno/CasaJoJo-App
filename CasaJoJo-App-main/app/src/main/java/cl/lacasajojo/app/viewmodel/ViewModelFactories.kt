package cl.lacasajojo.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Crea una fábrica de ViewModel a partir de una función. Permite pasar repositorios y argumentos
 * de navegación al constructor sin librerías de inyección de dependencias.
 *
 * Uso:  val vm: MiViewModel = viewModel(factory = factoryOf { MiViewModel(repositorio) })
 */
fun <VM : ViewModel> factoryOf(crear: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = crear() as T
    }
