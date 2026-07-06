# ✏️ Trazo a Trazo

App de Android hecha con **Kotlin** y **Jetpack Compose** que muestra dibujos animados construidos 100% con `Canvas` — sin imágenes externas ni librerías de gráficos. Cada dibujo se traza punto a punto frente al usuario, como si una mano invisible lo estuviera dibujando en tiempo real.

Es un proyecto personal / de regalo, organizado en categorías temáticas (Flores, Cartoons, Animales, Especial, Primavera, Invierno), con una galería de recuerdos, un editor de pixel art propio y un sistema de temas completamente dinámico.

> Repo: [`Erick73C/TrazoATrazo`](https://github.com/Erick73C/TrazoATrazo) · rama `master`

---

## 📦 Contenido

- [¿Qué es Trazo a Trazo?](#-qué-es-trazo-a-trazo)
- [Categorías disponibles](#-categorías-disponibles)
- [Stack y tecnologías](#-stack-y-tecnologías)
- [Arquitectura](#-arquitectura)
- [Estructura de carpetas](#-estructura-de-carpetas)
- [Sistema de temas](#-sistema-de-temas)
- [Editor de Pixel Art](#-editor-de-pixel-art)
- [Puesta en marcha](#-puesta-en-marcha)
- [Cómo agregar un nuevo dibujo](#-cómo-agregar-un-nuevo-dibujo)
- [Patrón estándar de animación](#-patrón-estándar-de-animación)
- [Aprendizajes clave](#-aprendizajes-clave)
- [Roadmap](#-roadmap)

---

## 🎨 ¿Qué es Trazo a Trazo?

La idea central: cada dibujo se construye **trazo a trazo** frente al usuario, con su propia secuencia, velocidad y estilo de animación, usando únicamente `Canvas` de Compose (paths, curvas bézier, líneas, `Animatable`).

Además de los dibujos animados, la app incluye:

- 🖼️ **Galería de Recuerdos** — fotos organizadas por categoría en formato polaroid.
- 🎨 **Editor de Pixel Art** — crea tus propios dibujos pixel por pixel y revive el trazo como animación.
- ⚙️ **Ajustes** — temas, tipografía, partículas de fondo y modo inmersivo.

## 🗂️ Categorías disponibles

| Emoji | Categoría | Contenido |
|---|---|---|
| 🌸 | **Flores** | Girasol, Ramo de flores, Girasol mejorado (con tallo y hojas animados) |
| 🎭 | **Cartoons** | Corazón, Batman, Trofeo Inovatec, Harley Quinn |
| 🐾 | **Animales** | Tortuga, Gatito negro |
| 🌱 | **Primavera** | Cerezo en flor, Mariposa monarca |
| ❄️ | **Invierno** | Muñeco de nieve, Árbol de Navidad, Copo de nieve |
| ⭐ | **Especial** | Carta animada (sobre → carta → recuerdos), Editor de Pixel Art |
| 📷 | **Galería** | Recuerdos organizados por categoría (Personal, Inovatec, Programas, Especiales) |

## 🧱 Stack y tecnologías

| Tecnología | Uso en el proyecto |
|---|---|
| **Kotlin** | Lenguaje principal |
| **Jetpack Compose** | UI declarativa — toda la interfaz y los `Canvas` |
| **Canvas API** | Paths, curvas bézier, líneas, gradientes |
| **Animatable / AnimatedVisibility** | Animaciones con control preciso (0f → 1f por elemento) |
| **Navigation Compose** | `NavHost` único (`AppNavigation.kt`) + objeto `Routes` |
| **DataStore Preferences** | Persistencia de tema, tipografía, fondo dinámico y ajustes de sistema |
| **Room** | Persistencia del editor de Pixel Art (dibujos + historial de trazos) |
| **ViewModel (MVVM manual, sin Hilt)** | `HomeViewModel`, `SettingsViewModel`, `PixelArtViewModel` |
| **StateFlow / Flow** | Estado reactivo entre ViewModel y composables |
| **mutableStateOf reactivo** | `AppColors` — el tema cambia toda la app sin refactor |
| **Coil** | Carga de imágenes |
| **KSP** | Anotaciones de Room |

### Versiones clave

- Kotlin `2.2.10` · Compose BOM `2026.02.01`
- Room `2.8.4` · KSP `2.0.21-1.0.28`

## 🏗️ Arquitectura

MVVM con **DI manual** (sin Hilt), organizado en tres capas:

| Capa | Paquete | Responsabilidad |
|---|---|---|
| **Presentation** | `presentation/` | ViewModels + UiState. Sin lógica de Android salvo `AndroidViewModel` |
| **Data** | `data/` | DataStore, catálogos estáticos, entidades de Room, repositorios |
| **UI** | `ui/` + `drawings/` | Composables y pantallas. Solo leen estado, sin lógica de negocio |

Puntos importantes del diseño:

- **Navegación**: un único `AppNavigation.kt` con `NavHost` + objeto `Routes`, sin fragmentación en múltiples grafos.
- **Tema reactivo**: `AppColors` usa `mutableStateOf` internamente — cualquier composable que lea `AppColors.X` se recompone automáticamente cuando cambia el tema, sin tocar el resto del código.
- **CompositionLocals**: `LocalAppColors`, `LocalBackgroundConfig` y `LocalAppFont` se proveen una sola vez en `AppNavigation`, evitando duplicados que rompan la reactividad.
- **`MaterialTheme` anidado**: se coloca dentro de `AppNavigation` (después del `CompositionLocalProvider`) para que la tipografía esté disponible antes de aplicar el tema — soluciona el orden de composición de Compose.
- **`themeReady`**: flag de splash que evita el "flash" de tema por defecto al iniciar la app mientras se lee DataStore.

## 📁 Estructura de carpetas

```
com.example.trazoatrazo/
├── data/
│   ├── DrawingsData.kt              ← Catálogo centralizado de dibujos
│   ├── MemoriesData.kt              ← Recuerdos de la galería
│   ├── ThemePreferences.kt          ← DataStore: tema
│   ├── FontPreferences.kt           ← DataStore: tipografía
│   ├── BackgroundPreferences.kt     ← DataStore: partículas / fondo dinámico
│   ├── SystemPreferences.kt         ← DataStore: modo inmersivo
│   └── local/                       ← Room (pixel art): entities, dao, repository
├── domain/model/
│   └── Memory.kt                    ← Modelo + enum MemoryCategory
├── navigation/
│   ├── AppNavigation.kt             ← NavHost principal (única fuente de verdad)
│   └── Routes.kt                    ← Constantes de rutas e IDs
├── presentation/
│   ├── home/                        ← HomeScreen + HomeViewModel
│   ├── settings/                    ← SettingsScreen + SettingsViewModel
│   ├── gallery/                     ← GalleryScreen (polaroid + overlay)
│   └── pixeleditor/                 ← PixelArtViewModel, MyCreationsScreen, Replay
├── ui/
│   ├── theme/                       ← AppColors, AppTheme, ThemeColorScheme, AppFonts, Type
│   ├── background/                  ← DynamicBackground, BackgroundConfig, partículas, grain, glow
│   └── components/                  ← DrawingButtons, DrawingCard, BackMenuButton
├── drawings/
│   ├── flowers/    GirasolScreen, FlowerScreen, ImprovedSunflowerScreen
│   ├── shapes/     HeartScreen, BatmanScreen, TrofeoScreen, HarleyScreen
│   ├── animals/    TurtleScreen, CatBlackScreen
│   ├── spring/     CherryTreeScreen, ButterflyScreen
│   ├── winter/     SnowmanScreen, ChristmasTreeScreen, SnowflakeScreen (SnowCop)
│   └── special/    EnvelopeScreen, LetterContentScreen, PixelEditorScreen
└── utils/                           ← ColorUtils, categoryLabelFor
```

## 🎨 Sistema de temas

La app soporta **16 temas visuales** (inspirados en Jujutsu Kaisen y variaciones estacionales/festivas), persistidos con DataStore y restaurados automáticamente al abrir la app.

### Cómo funciona la reactividad

```kotlin
// ui/theme/AppColors.kt
object AppColors {
    private var _scheme: ThemeColorScheme by mutableStateOf(jjkDarkScheme)
    fun applyTheme(theme: AppTheme) { _scheme = themeColorSchemeFor(theme) }
    val Vacio: Color get() = _scheme.vacio   // ← lectura reactiva
    // ...
}
```

Al leer `AppColors.Vacio` dentro de un Composable, Compose registra esa lectura como dependencia. Cuando `applyTheme()` cambia `_scheme`, **todos** los composables que leen cualquier propiedad de `AppColors` se recomponen automáticamente — sin tocar el resto de archivos.

Cada tema define, además de su paleta base (fondos, primarios, secundarios, texto), acentos propios por categoría (`flowersAccent`, `cartoonsAccent`, `animalsAccent`, `springAccent`, `winterAccent`) y una configuración de **fondo dinámico** (`BackgroundConfig`) con partículas, estrellas, pétalos, grain y glow — cada uno con su propio `SpecialParticleType` (12 tipos: copo de nieve, destello, hoja, corazón, pixel, etc.).

### Personalización adicional

- **6 tipografías** (`AppFont`): Quicksand, Caveat, Nunito, Pacifico, Playfair, Bitcount.
- **Partículas configurables** desde Ajustes: activar/desactivar, intensidad y velocidad global, con reset al valor por defecto del tema.
- **Modo inmersivo**: oculta barras del sistema.

## 🖌️ Editor de Pixel Art

Editor completo persistido en Room:

- 8 herramientas: Lápiz, Borrador, Relleno (BFS / flood fill), Gotero, Línea (Bresenham), Selección/mover, zoom 1x–4x, pan con dos dedos.
- Undo/redo, selector de color HSV personalizado.
- **Reproducción del trazo**: cada pincelada se guarda como `StrokeStep` en Room con timestamp, permitiendo reconstruir la animación exacta de cómo se dibujó (`stepDelay = (2800L / totalSteps).coerceIn(6L, 60L)`).
- Galería de creaciones (`MyCreationsScreen`) con miniaturas, ordenamiento y borrado.

## 🚀 Puesta en marcha

```bash
git clone https://github.com/Erick73C/TrazoATrazo.git
cd TrazoATrazo
```

1. Abre el proyecto en **Android Studio** (versión reciente, con soporte para Kotlin 2.2.x).
2. Deja que Gradle sincronice (usa el wrapper incluido, Gradle 9.3.1 + JDK 21 vía toolchain).
3. Ejecuta sobre un emulador o dispositivo con **minSdk 24** / **targetSdk 35**.

No requiere claves de API ni configuración adicional — toda la persistencia es local (DataStore + Room).

## ➕ Cómo agregar un nuevo dibujo

| # | Paso | Qué hacer |
|---|---|---|
| 1 | **Crear el archivo** | `XxxScreen.kt` en el subpaquete de `drawings/` que corresponda |
| 2 | **Firma obligatoria** | `@Composable fun XxxScreen(onBack: () -> Unit)` |
| 3 | **Estado** | `var repetir by remember { mutableIntStateOf(0) }` + `Animatable`s necesarios |
| 4 | **`LaunchedEffect(repetir)`** | Toda la secuencia de animación va aquí — **nunca** usar `Unit` |
| 5 | **`DrawingButtons`** | Al final del `Box`: `DrawingButtons(visible=..., onRepeat={ repetir++ }, onBack=onBack)` |
| 6 | **`BackMenuButton`** | En `Alignment.TopStart` |
| 7 | **`Routes.kt`** | Agregar el `const val` en `object Drawings` |
| 8 | **`DrawingsData.kt`** | Agregar el `DrawingItem` en la categoría correcta del `drawingCatalog` |
| 9 | **`AppNavigation.kt`** | Agregar el `when(drawingId)` dentro de la categoría correspondiente |
| 10 | **Probar** | Compilar, navegar, verificar animación y botón "Repetir" |

## 🔁 Patrón estándar de animación

Todos los dibujos siguen el mismo patrón por etapas, sin excepciones:

```kotlin
var etapa   by remember { mutableIntStateOf(0) }
var repetir by remember { mutableIntStateOf(0) }
val partAnim = remember { Animatable(0f) }

LaunchedEffect(repetir) {   // ← SIEMPRE repetir, NUNCA Unit
    etapa = 0
    partAnim.snapTo(0f)                              // reset
    delay(300L)
    partAnim.animateTo(1f, tween(600, easing = EaseOutBack))
    etapa = 1
    // ... más partes ...
    etapa = N   // activa DrawingButtons
}
```

**Regla clave**: `LaunchedEffect(repetir)` — al incrementar `repetir++` en el botón "Repetir", la animación se reinicia por completo.

## 💡 Aprendizajes clave

- Un `LocalBackgroundConfig` duplicado entre paquetes causa un desajuste silencioso de DI — mantener una única fuente de verdad para cada `CompositionLocal`.
- `SettingsScreen` debe recibir el ViewModel compartido desde `AppNavigation`, no instanciar el suyo propio con `viewModel()`.
- Los `collect` anidados dentro de `init` de un ViewModel bloquean las actualizaciones reactivas — usar `flatMapLatest`.
- Restricción de orden de composición: `MaterialTheme` para tipografía debe ir dentro de `AppNavigation` (después del `CompositionLocalProvider`), no en `MainActivity`.
- `AwaitPointerEventScope` (con `@RestrictsSuspension`) requiere `rememberCoroutineScope().launch { scrollState.scrollBy(...) }` para hacer scroll dentro de un loop de gestos.
- Los nombres de recursos de Android no admiten caracteres especiales (ej. `ñ`).
- `drawWithCache` es preferible a `drawWithContent` para evitar recalcular geometría en cada frame.

## 🛣️ Roadmap

- 🔊 Sonidos cortos (200–400ms, MP3 128kbps) para las animaciones e interacciones.
- Más ajustes de personalización (notificaciones, tamaño de texto).
- Nuevos dibujos en Animales y Flores.

---

*Trazo a Trazo — proyecto personal hecho con cariño, trazo a trazo.* ✏️
