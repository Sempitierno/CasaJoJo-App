# La Casa JoJo — Gestión digital de comandas y pedidos (Android)

Aplicación móvil Android para **La Casa JoJo SpA**, un negocio de almuerzos y menús diarios. Reemplaza la gestión de
pedidos por WhatsApp y papelitos por un sistema digital sencillo: el administrador publica el menú del día, los clientes
arman su combinación y eligen la hora de retiro, y la cocina ve las comandas estructuradas y cambia su estado.

Proyecto académico — **DSY1105 · Desarrollo de Aplicaciones Móviles** (CITT Viña del Mar). Basado en el caso
*"Gestión Digital de Comandas y Pedidos de Menú Diario"*.

**Tecnologías:** Kotlin · Jetpack Compose · Material Design 3 · MVVM · Room (SQLite) · Kotlin Coroutines y Flow ·
Navigation Compose · selector de hora nativo de Android · notificaciones locales. Sin backend, sin Internet.

---

## Estado de verificación (léase primero)

Este proyecto se generó en un entorno que **no tenía Android SDK, Gradle, compilador de Kotlin ni acceso a Internet**.
Por eso, de forma explícita:

| Aspecto | Estado |
|---|---|
| `./gradlew assembleDebug` (compilación) | **NO ejecutado.** La app **no está verificada como compilada**. |
| Pruebas unitarias (`./gradlew test`) | **NO ejecutadas.** |
| Pruebas instrumentadas (`connectedDebugAndroidTest`) | **NO ejecutadas.** |
| Ejecución de la app en emulador o teléfono | **NO realizada.** |
| Existencia de las versiones de dependencias en los repositorios Maven | **NO comprobada** (sin Internet). Se eligió una combinación estable y conocida (ver más abajo). |
| Archivo `gradle/wrapper/gradle-wrapper.jar` | **No incluido** (es un binario que no pudo generarse offline). Ver sección *Gradle Wrapper*. |

Lo que **sí** se hizo es una **revisión estática** (no equivale a una compilación):

* Análisis léxico propio de los 68 archivos Kotlin: balance de `()`, `{}`, `[]`, cadenas y plantillas; `package` coherente con
  cada carpeta; todos los `import` del proyecto resuelven a una declaración existente; ningún identificador usado sin
  importar o declarar; sin clases duplicadas.
* Coherencia entre declaraciones y usos: los argumentos con nombre de cada llamada coinciden con la firma real;
  cada `viewModel.xxx` que usa una pantalla existe en su ViewModel; cada `ui.campo` existe en su clase de estado; todas
  las rutas de navegación están registradas; los `R.*` y `@recurso` existen; los alias de `libs.versions.toml` coinciden
  con los usados en los `build.gradle.kts`; todos los íconos usados están en `material-icons-core`.
* Ejecución real (en una JVM) de la lógica que depende de APIs de Java y que usan las pruebas — expresión regular de
  correo, formato de precios en CLP, formatos de fecha y hora, cálculo de la hora de retiro y hash PBKDF2 con
  formatos inválidos — con 42 comprobaciones correctas.
* Lectura manual de todo el código buscando errores de tipos, ámbitos de Compose, `smart cast` y uso de APIs.

**Primer paso recomendado al recibir el proyecto:** abrirlo en Android Studio, sincronizar Gradle y ejecutar
`assembleDebug` y `test`. Si aparece algún error de compilación o de sincronización, es un defecto de este proyecto
(no un problema de su equipo): corríjalo o repórtelo con el mensaje exacto del error.

---

## Funcionalidades

**Registro e inicio de sesión (sin conexión)**
* Registro con nombre completo, correo, contraseña, confirmación y **tipo de usuario elegido explícitamente**
  (Administrador o Cliente; no hay valor preseleccionado).
* Validaciones: campos obligatorios, formato de correo, contraseñas iguales (mínimo 6 caracteres) y **correo único sin
  distinguir mayúsculas y minúsculas** (se valida en código y además con un índice único `NOCASE` en la base de datos).
* La contraseña **nunca se guarda en texto plano**: PBKDF2-HMAC-SHA256, 100.000 iteraciones y sal aleatoria por usuario.
* La sesión **se mantiene al cerrar y abrir la app** hasta que el usuario cierre sesión.

**Administrador**
* Panel con resumen real (productos, publicados y comandas por estado).
* **Menú del día:** crear, editar y eliminar productos (nombre, categoría, descripción opcional, precio en CLP,
  disponibilidad y publicación). Lista ordenada por categoría; interruptores rápidos *Publicado/Oculto* y
  *Disponible/Agotado*.
* **Comandas:** todos los pedidos con identificador, cliente, correo, fecha de creación, **hora de retiro**, los cinco
  productos (plato de fondo, acompañamiento, ensalada, postre, bebida), especificaciones, total y estado. Orden de cocina:
  primero lo pendiente por hora de retiro (el más próximo arriba) y al final lo ya listo. **Filtro por estado** con
  contadores.
* **Cambio de estado** con un botón: Recibido → En preparación → Listo para retiro (y posibilidad de volver atrás si
  hubo un error). Se guarda en Room al instante.

**Cliente**
* **Menú publicado**, agrupado por categoría, con descripción, precio y disponibilidad. Los productos no disponibles
  se ven atenuados y **no se pueden seleccionar**. Si no hay menú publicado se muestra un mensaje amigable.
* **Nuevo pedido:** un producto por categoría, campo de especificaciones o restricciones alimentarias, **selector de hora
  nativo de Android** (`TimePickerDialog`), resumen con **total calculado automáticamente** y diálogo de confirmación final.
* El pedido guarda el **nombre y precio de cada producto en el momento de pedir**: cambiar o borrar productos del menú
  después no altera los pedidos anteriores.
* **Historial** de pedidos propios y **detalle** de cada pedido con una línea de tiempo del estado.
* Un cliente **solo puede ver sus propios pedidos** (la consulta filtra por su id).
* **Notificaciones locales** cuando cambia el estado de un pedido (ver sección *Notificaciones*).

Interfaz en español, precios en pesos chilenos (`$3.500`), estados de carga, mensajes de validación, estados vacíos,
confirmaciones en acciones importantes (eliminar producto, cerrar sesión, confirmar pedido) y navegación correcta con el
botón Atrás de Android. Tema claro y oscuro.

---

## Requisitos previos

* **Android Studio** Ladybug (2024.2.1) o más reciente.
* **JDK 17 o superior** (el JDK incluido en Android Studio sirve).
* **Android SDK Platform 35** (se instala desde *Tools > SDK Manager*; Android Studio ofrece instalarlo al sincronizar).
* Conexión a Internet **la primera vez** (Gradle descarga la distribución 8.9 y las dependencias).
* Un emulador o teléfono con **Android 8.0 (API 26) o superior**.

Versiones usadas (combinación estable y compatible, definida en `gradle/libs.versions.toml`):

| Componente | Versión |
|---|---|
| Gradle | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 (plugin de Compose incluido en Kotlin) |
| KSP (procesador de Room) | 2.0.21-1.0.25 |
| Compose BOM | 2024.12.01 (Compose 1.7.6, Material 3 1.3.1) |
| Navigation Compose / Lifecycle / Activity | 2.8.5 / 2.8.7 / 1.9.3 |
| Room | 2.6.1 |
| Coroutines | 1.9.0 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |

Si Gradle no pudiera resolver alguna versión, ajuste el número en `gradle/libs.versions.toml` (es el único lugar).

---

## Cómo abrir y ejecutar la aplicación

1. Descomprima `CasaJoJo_AndroidStudio.zip`. Quedará la carpeta `CasaJoJo`.
2. En Android Studio: **File > Open** y seleccione la carpeta **`CasaJoJo`** (la que contiene `settings.gradle.kts`).
3. Espere la sincronización de Gradle. Si pide instalar *Android SDK Platform 35*, acepte. Si propone actualizar el
   Android Gradle Plugin, puede rechazarlo (el proyecto está configurado para la versión indicada).
4. Elija un dispositivo (ver más abajo) y pulse **Run ▶**.

### Elegir un emulador o conectar un teléfono

* **Emulador:** *Tools > Device Manager > Create Device*, elija por ejemplo *Pixel 6* y una imagen del sistema de
  **API 34 o 35**; luego selecciónelo en la lista de dispositivos de la barra superior y pulse *Run*.
  (Para ver el permiso de notificaciones use una imagen de API 33 o superior.)
* **Teléfono:** en el teléfono active *Opciones de desarrollador* y *Depuración USB*, conéctelo por USB, acepte el
  cuadro de confirmación en el teléfono y selecciónelo en Android Studio. También sirve la *Depuración inalámbrica*.

### Gradle Wrapper

El ZIP incluye `gradlew`, `gradlew.bat` y `gradle/wrapper/gradle-wrapper.properties` (Gradle 8.9), pero **no** el binario
`gradle/wrapper/gradle-wrapper.jar`. Android Studio normalmente sincroniza el proyecto sin ese archivo (usa la
`distributionUrl` del `.properties`). Para usar `./gradlew` desde la terminal, restáurelo con cualquiera de estas opciones:

* Con Gradle instalado: ejecutar en la carpeta del proyecto `gradle wrapper --gradle-version 8.9`.
* Tras abrir el proyecto una vez en Android Studio, Gradle 8.9 queda descargado en
  `~/.gradle/wrapper/dists/gradle-8.9-bin/<código>/gradle-8.9/bin/gradle`; ejecute ese programa con el argumento `wrapper`.
* Copiar `gradle-wrapper.jar` desde cualquier otro proyecto de Android Studio a `gradle/wrapper/`.

---

## Guía de uso (flujo completo en un mismo dispositivo)

Como la app es 100 % local, el administrador y el cliente usan **el mismo teléfono o emulador**, iniciando sesión con
una cuenta y luego con la otra. Use solo datos ficticios.

1. **Registrar un administrador:** pantalla de inicio de sesión > *¿No tienes cuenta? Regístrate* > complete los datos
   (por ejemplo `Admin Demo`, `admin.demo@ejemplo.cl`, clave `demo1234`) > tipo **Administrador** > *Crear cuenta*.
2. **Registrar un cliente:** repita con otro correo (por ejemplo `cliente.demo@ejemplo.cl`) y tipo **Cliente**.
3. **Publicar el menú** (como administrador): *Menú del día*. Puede crear productos con *Nuevo producto*, o pulsar
   *Cargar menú de ejemplo* (12 productos ficticios cuyo nombre termina en "(demo)"; uno viene agotado para probar el
   bloqueo). Compruebe que los productos estén *Publicado*. Cierre sesión con el ícono de salida de la barra superior.
4. **Crear un pedido** (como cliente): *Hacer un pedido* > elija el plato de fondo (obligatorio) y lo que desee > *Elegir
   hora de retiro* (debe ser igual o posterior a la hora actual) > escriba especificaciones si corresponde > *Revisar y
   confirmar pedido* > revise el resumen > *Confirmar pedido*. Verá la pantalla de confirmación. Cierre sesión.
5. **Cambiar el estado** (como administrador): *Comandas* > en la tarjeta del pedido pulse *Iniciar preparación* y luego
   *Marcar listo para retiro*. Use los filtros de arriba para ver solo un estado. Cierre sesión.
6. **Ver el resultado** (como cliente): al entrar llegará la notificación del cambio de estado y en *Mis pedidos* verá el
   estado actualizado; al tocar el pedido se abre su detalle con la línea de tiempo.

### Notificaciones locales

No hay servidor que envíe avisos: la detección ocurre en el propio dispositivo. Cada pedido guarda su estado actual y el
**último estado del que se avisó**. Cuando el cliente tiene la sesión iniciada, la app compara ambos y, si difieren,
muestra la notificación y registra el aviso. En la práctica con un único dispositivo: el administrador cambia el estado,
cierra sesión, y **el aviso aparece cuando el cliente inicia sesión**. En Android 13+ la app pide el permiso `POST_NOTIFICATIONS` al abrir el panel del cliente; si se
rechaza, la app funciona igual pero no muestra avisos. Con un backend (versión futura) esta misma lógica se podría
reemplazar por notificaciones push.

---

## Dónde se almacenan los datos

* **Base de datos Room/SQLite** `casajojo.db`, en el almacenamiento privado de la app
  (`/data/data/cl.lacasajojo.app/databases/`). Tablas: `usuarios`, `productos`, `pedidos`, `detalles_pedido`.
  Para inspeccionarla use **View > Tool Windows > App Inspection > Database Inspector** con la app en ejecución.
* **SharedPreferences** `sesion_casajojo`: guarda únicamente el *id* del usuario con la sesión iniciada (ninguna
  credencial ni dato personal).
* Todo es local: no se envía nada a Internet y `allowBackup` está desactivado (la base de datos tampoco va a las
  copias de seguridad en la nube).
* **Borrar todos los datos:** *Ajustes del teléfono > Aplicaciones > La Casa JoJo > Almacenamiento > Borrar datos*, o
  `adb shell pm clear cl.lacasajojo.app`.

---

## Limitaciones de seguridad del prototipo

* **La selección libre de rol (Administrador/Cliente) existe solo para pruebas académicas y NO constituye un sistema
  seguro de autorización.** No hay validación remota ni aprobación de cuentas: cualquiera puede registrarse como
  administrador. En producción los roles deben asignarse y verificarse en un servidor.
* Las credenciales se protegen con PBKDF2 con sal, pero viven en el mismo dispositivo. La base de datos no está cifrada
  y la sesión es un identificador en preferencias privadas: un dispositivo con *root* podría alterarlos.
* Use **solo datos ficticios o anonimizados** (el caso exige cumplir las leyes N.º 21.719 y N.º 19.628). La app no incluye
  usuarios, contraseñas ni datos personales reales; el único dato de ejemplo es el menú "(demo)", que se carga solo si el
  administrador lo solicita.

---

## Arquitectura (MVVM)

```
UI (Compose)  ──►  ViewModel  ──►  Repository  ──►  DAO (Room)  ──►  SQLite
 pantallas         estado (StateFlow)   reglas de negocio      consultas reactivas (Flow)
```

* Los DAO devuelven `Flow`: cuando el administrador cambia el estado de un pedido, la pantalla del cliente se actualiza
  sola al consultarla, sin cerrar la app.
* Operaciones de base de datos siempre fuera del hilo principal (funciones `suspend` y `Flow` de Room; el hash de
  contraseñas en `Dispatchers.Default`).
* Inyección de dependencias manual (`AppContainer`), sin librerías adicionales, para que el código sea fácil de seguir.
* Cada pantalla tiene su ViewModel ligado a su entrada de navegación; al cerrar sesión se vacía la pila.

```
CasaJoJo/
├── settings.gradle.kts · build.gradle.kts · gradle.properties · gradlew · gradlew.bat
├── gradle/ (libs.versions.toml, wrapper/gradle-wrapper.properties)
└── app/
    ├── build.gradle.kts · proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/ (tema, íconos, reglas de respaldo)
        │   └── java/cl/lacasajojo/app/
        │       ├── CasaJoJoApplication.kt · MainActivity.kt
        │       ├── data/
        │       │   ├── entity/      Usuario, Producto, Pedido (+ DetallePedido), enums
        │       │   ├── dao/         UsuarioDao, ProductoDao, PedidoDao
        │       │   ├── database/    AppDatabase, Converters
        │       │   ├── repository/  Auth, Menu, Pedido, sesión, resultados
        │       │   ├── AppContainer.kt · DatosDemostracion.kt
        │       ├── viewmodel/       un ViewModel por pantalla + fábrica
        │       ├── ui/
        │       │   ├── theme/       colores, tipografía, tema Material 3
        │       │   ├── components/  componentes reutilizables
        │       │   ├── navigation/  Rutas, AppNavHost
        │       │   └── screens/     auth/ · admin/ · cliente/
        │       └── util/            validaciones, formato, hash, notificaciones, orden de comandas
        ├── test/                    pruebas unitarias (JVM)
        └── androidTest/             pruebas instrumentadas (Room real)
```

### Modelo de datos

| Tabla | Contenido y claves |
|---|---|
| `usuarios` | `id` (PK autoincremental), nombre, `correo` (índice único, collation NOCASE), `rol`, `claveHash`, fecha. |
| `productos` | `id` (PK), nombre, `categoria`, descripción, `precio` (Int, CLP), `disponible`, `publicado`. |
| `pedidos` | `id` (PK, **UUID** único), `clienteId` (FK a `usuarios`), copia del nombre y correo del cliente, `creadoEn`, `horaRetiro`, especificaciones, `total`, `estado`, `estadoNotificado`, `actualizadoEn`. |
| `detalles_pedido` | `id` (PK), `pedidoId` (FK a `pedidos`, borrado en cascada), categoría, `productoId` (referencia informativa, sin FK), **nombre y precio del producto al pedir**. |

Borrar o editar un producto **no** toca los pedidos (no hay clave foránea hacia `productos`). La base se crea en la
versión 1; el esquema se exporta a `app/schemas/` para escribir migraciones en el futuro, y se evita
`fallbackToDestructiveMigration` para no borrar pedidos de forma silenciosa.

### Reglas de negocio y decisiones de diseño

* **Plato de fondo obligatorio; el resto opcional** (las categorías opcionales ofrecen "Sin ensalada", etc.). Si una
  categoría no tiene productos, se indica claramente y no bloquea el pedido. Si no hay ningún plato de fondo disponible,
  no se puede pedir.
* **Hora de retiro:** debe elegirse y no puede ser anterior a la hora actual (a nivel de minuto); el retiro es el mismo día.
* **Doble pulsación:** el botón se bloquea al instante y, además, el pedido usa un UUID generado al abrir la pantalla; si
  el mismo id llegara dos veces, la base de datos lo ignora (no se duplica).
* **Revalidación al confirmar:** el repositorio vuelve a leer los productos desde Room; si alguno dejó de estar publicado
  o disponible, rechaza el pedido con un mensaje claro y usa los precios vigentes.
* **Identificador visible:** `#` + los primeros 8 caracteres del UUID (por ejemplo `#A1B2C3D4`); el detalle muestra
  también el id completo.
* El perfil "Operador de Caja" del caso corresponde al rol **Administrador** pedido en el enunciado del proyecto.

---

## Compilar y ejecutar las pruebas

Desde la terminal (en Windows use `gradlew.bat`; requiere el `gradle-wrapper.jar`, ver sección *Gradle Wrapper*), o desde la
ventana *Gradle* de Android Studio:

```
./gradlew assembleDebug                 # compila e instala el APK en app/build/outputs/apk/debug/
./gradlew test                          # pruebas unitarias (JVM, no requieren emulador)
./gradlew connectedDebugAndroidTest     # pruebas instrumentadas (requieren emulador o teléfono conectado)
```

Informes de resultados: `app/build/reports/tests/testDebugUnitTest/index.html` y
`app/build/reports/androidTests/connected/`.

### Qué cubren las pruebas

**Unitarias (50, en JVM):** formato de precios en CLP y de fechas; validaciones de registro, inicio de sesión, productos y
pedidos (correo, contraseñas, rol obligatorio, precios, hora de retiro); hash de contraseñas (no se guarda en texto plano,
verificación correcta e incorrecta, formatos inválidos, sal distinta); orden de las comandas para cocina; transiciones de
estado; total del pedido y reglas para poder confirmar.

**Instrumentadas (30, con Room real):**

| Requisito del enunciado | Prueba |
|---|---|
| Registro de cliente / de administrador | `registra_un_cliente`, `registra_un_administrador` |
| Rechazo de correos duplicados | `rechaza_correos_duplicados_sin_distinguir_mayusculas`, `el_indice_unico_...` |
| Inicio de sesión correcto e incorrecto | `inicia_sesion_con_credenciales_correctas`, `no_inicia_sesion_con_contrasena_incorrecta`, `..._correo_no_registrado` |
| Persistencia de la sesión | `la_sesion_se_mantiene_al_reabrir_la_aplicacion` |
| Creación y edición de productos | `el_administrador_crea_y_edita_un_producto` |
| Consulta del menú desde el cliente | `el_cliente_solo_ve_los_productos_publicados`, `un_producto_no_disponible_...` |
| Creación de un pedido y cálculo del total | `crea_un_pedido_con_total_calculado_y_estado_recibido` |
| Pedidos anteriores no cambian al editar el menú | `el_pedido_conserva_nombres_y_precios_aunque_el_menu_cambie_despues` |
| Persistencia del pedido tras reiniciar | `usuarios_menu_y_pedidos_persisten_al_reabrir_la_base_de_datos` (cierra y reabre el archivo) |
| Cambio de estado y que lo vea el cliente | `el_administrador_cambia_el_estado_y_el_cliente_lo_ve` |
| Separación de pedidos por usuario | `cada_cliente_solo_ve_sus_propios_pedidos` |
| Pedido duplicado por pulsación repetida | `una_pulsacion_repetida_no_duplica_el_pedido` |

Las pantallas (Compose) no tienen pruebas automáticas de interfaz; para ellas use esta **lista de verificación manual**:
registro con datos inválidos (cada mensaje de error) · correo duplicado con otras mayúsculas · inicio de sesión
incorrecto · cerrar la app desde "recientes", reabrirla y comprobar que la sesión sigue · crear, editar y eliminar un
producto · producto *Agotado* visible pero no seleccionable en "Nuevo pedido" · menú vacío (mensaje amigable) · pedido sin
plato de fondo (botón bloqueado) · hora de retiro anterior (mensaje) · pedido completo y confirmación · cambio de estado y
filtros en *Comandas* · notificación y estado actualizado como cliente · botón Atrás en cada pantalla · rotación de la
pantalla durante el formulario de pedido.

---

## Alcance respecto del caso

Esta primera versión implementa lo pedido para el prototipo local. **Quedan fuera, a propósito:** backend de administración
(Spring Boot) e integración con Retrofit/API REST, Firebase o nube, APK firmado en modo *release*, repositorio Git y tablero
Trello. La arquitectura facilita agregarlos después: todo el acceso a datos pasa por los repositorios (se podría añadir
una fuente remota sin tocar las pantallas) y los pedidos ya usan identificadores UUID generados en el cliente.

## Limitaciones conocidas

* No verificado en compilación ni en ejecución (ver *Estado de verificación*).
* Un solo dispositivo: administrador y cliente no se comunican entre teléfonos distintos (no hay backend).
* Las notificaciones aparecen cuando la sesión del cliente está activa en el dispositivo (ver *Notificaciones locales*).
* La pantalla de comandas no tiene búsqueda ni filtro por fecha; los pedidos antiguos permanecen en la lista.
* Selección libre de rol (ver *Limitaciones de seguridad*).

## Solución de problemas

* **"Failed to find target with hash string 'android-35'"** → instale *Android SDK Platform 35* en el SDK Manager.
* **"Unsupported class file major version" / error de JDK** → en *Settings > Build, Execution, Deployment > Build Tools >
  Gradle* elija como *Gradle JDK* el JDK incluido en Android Studio (17 o superior).
* **"Plugin ... was not found" / no resuelve dependencias** → compruebe la conexión a Internet y los repositorios
  `google()` y `mavenCentral()` en `settings.gradle.kts`; si una versión no existe, cámbiela en `gradle/libs.versions.toml`.
* **`./gradlew` dice que falta `gradle-wrapper.jar`** → ver la sección *Gradle Wrapper*.
* **No aparecen notificaciones** → en Android 13+ acepte el permiso; revise que las notificaciones de la app estén
  activadas en los ajustes del sistema.
