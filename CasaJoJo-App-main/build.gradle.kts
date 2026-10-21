// Archivo de construcción del proyecto raíz.
// Los plugins se declaran aquí (apply false) y se aplican en :app.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
