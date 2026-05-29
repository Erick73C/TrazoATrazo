package com.example.trazoatrazo.navigation

import HomeScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trazoatrazo.drawings.Animals.CatBlackScreen
import com.example.trazoatrazo.drawings.Animals.TurtleScreen
import com.example.trazoatrazo.drawings.Shapes.HeartScreen
import com.example.trazoatrazo.drawings.flowers.FlowerScreen
import com.example.trazoatrazo.drawings.flowers.GirasolScreen
import com.example.trazoatrazo.drawings.flowers.ImprovedSunflowerScreen
import com.example.trazoatrazo.drawings.special.EnvelopeScreen
import com.example.trazoatrazo.drawings.special.LetterContentScreen
import com.example.trazoatrazo.presentation.category.CategoryScreen
import com.example.trazoatrazo.presentation.settings.SettingsScreen
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.LocalAppColors
import com.example.trazoatrazo.ui.theme.themeColorSchemeFor


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    // 1. Crear SettingsViewModel aquí → init() corre al abrir la app
    //    viewModel() devuelve la misma instancia si SettingsScreen lo llama también
    val settingsViewModel: SettingsViewModel = viewModel()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsStateWithLifecycle()

    // 2. Proveer el esquema actual a todo el árbol de composición
    CompositionLocalProvider(
        LocalAppColors provides themeColorSchemeFor(selectedTheme)
    ) {
        NavHost(
            navController    = navController,
            startDestination = Routes.HOME
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onDrawingClick  = { categoryId, drawingId ->
                        navController.navigate(Routes.drawing(categoryId, drawingId))
                    },
                    onLetterClick   = { navController.navigate(Routes.LETTER) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.DRAWING) { backStackEntry ->
                // Sin cambios — igual que antes
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val drawingId  = backStackEntry.arguments?.getString("drawingId")  ?: ""
                when (categoryId) {
                    Routes.Category.FLOWERS -> when (drawingId) {
                        Routes.Drawings.GIRASOL           -> GirasolScreen(onBack = { navController.popBackStack() })
                        Routes.Drawings.FLORES            -> FlowerScreen(onBack  = { navController.popBackStack() })
                        Routes.Drawings.IMPROVED_SUNFLOWER -> ImprovedSunflowerScreen(onBack = { navController.popBackStack() })
                    }
                    Routes.Category.CARTOONS -> when (drawingId) {
                        Routes.Drawings.HEART -> HeartScreen(onBack = { navController.popBackStack() })
                    }
                    Routes.Category.ANIMALS -> when (drawingId) {
                        Routes.Drawings.TURTLE    -> TurtleScreen(onBack = { navController.popBackStack() })
                        Routes.Drawings.CAT_BLACK -> CatBlackScreen(onBack = { navController.popBackStack() })
                    }
                    Routes.Category.SPECIAL -> when (drawingId) {
                        Routes.Drawings.carta -> EnvelopeScreen(
                            onBack       = { navController.popBackStack() },
                            onReadLetter = { navController.navigate(Routes.LETTER_CONTENT) }
                        )
                    }
                }
            }

            composable(Routes.LETTER)         { LetterContentScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.LETTER_CONTENT)  { LetterContentScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.SETTINGS)        { SettingsScreen(onBack = { navController.popBackStack() }) }
        }
    }
}