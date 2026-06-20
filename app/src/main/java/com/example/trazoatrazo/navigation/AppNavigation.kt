package com.example.trazoatrazo.navigation

import com.example.trazoatrazo.drawings.spring.ButterflyScreen
import com.example.trazoatrazo.drawings.spring.CherryTreeScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trazoatrazo.drawings.animals.CatBlackScreen
import com.example.trazoatrazo.drawings.animals.TurtleScreen
import com.example.trazoatrazo.drawings.flowers.FlowerScreen
import com.example.trazoatrazo.drawings.flowers.GirasolScreen
import com.example.trazoatrazo.drawings.flowers.ImprovedSunflowerScreen
import com.example.trazoatrazo.drawings.cartoons.BatmanScreen
import com.example.trazoatrazo.drawings.cartoons.HarleyScreen
import com.example.trazoatrazo.drawings.cartoons.TrofeoScreen
import com.example.trazoatrazo.drawings.shapes.HeartScreen
import com.example.trazoatrazo.drawings.special.EnvelopeScreen
import com.example.trazoatrazo.drawings.special.LetterContentScreen
import com.example.trazoatrazo.drawings.winter.ChristmasTreeScreen
import com.example.trazoatrazo.drawings.winter.SnowflakeScreen
import com.example.trazoatrazo.drawings.winter.SnowmanScreen
import com.example.trazoatrazo.presentation.home.HomeScreen
import com.example.trazoatrazo.presentation.settings.SettingsScreen
import com.example.trazoatrazo.presentation.settings.SettingsViewModel
import com.example.trazoatrazo.ui.theme.AppColors
import com.example.trazoatrazo.ui.theme.LocalAppColors
import com.example.trazoatrazo.ui.background.LocalBackgroundConfig
import com.example.trazoatrazo.ui.theme.LocalAppFont
import com.example.trazoatrazo.ui.theme.themeColorSchemeFor
import com.example.trazoatrazo.ui.theme.typographyFor

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val selectedTheme by settingsViewModel.selectedTheme.collectAsStateWithLifecycle()
    val themeReady    by settingsViewModel.themeReady.collectAsStateWithLifecycle()
    val backgroundConfig by settingsViewModel.backgroundConfig.collectAsStateWithLifecycle()
    val selectedFont     by settingsViewModel.selectedFont.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalAppColors provides themeColorSchemeFor(selectedTheme),
        LocalBackgroundConfig provides backgroundConfig,
        LocalAppFont          provides selectedFont
    ) {
        MaterialTheme(
            typography = typographyFor(selectedFont)
        ) {
        if (!themeReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Vacio)
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onDrawingClick = { categoryId, drawingId ->
                            navController.navigate(Routes.drawing(categoryId, drawingId))
                        },
                        onLetterClick = { navController.navigate(Routes.LETTER) },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                    )
                }

                composable(Routes.DRAWING) { backStackEntry: NavBackStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                    val drawingId = backStackEntry.arguments?.getString("drawingId") ?: ""
                    when (categoryId) {
                        Routes.Category.FLOWERS -> when (drawingId) {
                            Routes.Drawings.GIRASOL -> GirasolScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.FLORES -> FlowerScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.IMPROVED_SUNFLOWER -> ImprovedSunflowerScreen(onBack = { navController.popBackStack() })
                        }

                        Routes.Category.CARTOONS -> when (drawingId) {
                            Routes.Drawings.HEART -> HeartScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.BATMAN -> BatmanScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.TROFEO -> TrofeoScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.HARLEY -> HarleyScreen(onBack = { navController.popBackStack() })
                        }

                        Routes.Category.ANIMALS -> when (drawingId) {
                            Routes.Drawings.TURTLE -> TurtleScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.CAT_BLACK -> CatBlackScreen(onBack = { navController.popBackStack() })
                        }

                        Routes.Category.SPECIAL -> when (drawingId) {
                            Routes.Drawings.carta -> EnvelopeScreen(
                                onBack = { navController.popBackStack() },
                                onReadLetter = { navController.navigate(Routes.LETTER_CONTENT) }
                            )
                        }

                        Routes.Category.SPRING -> when (drawingId) {
                            Routes.Drawings.CHERRY_TREE -> CherryTreeScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.BUTTERFLY -> ButterflyScreen(onBack = { navController.popBackStack() })
                        }

                        Routes.Category.WINTER -> when (drawingId) {
                            Routes.Drawings.SNOWMAN -> SnowmanScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.CHRISTMAS_TREE -> ChristmasTreeScreen(onBack = { navController.popBackStack() })
                            Routes.Drawings.COPONIEVE -> SnowflakeScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }

                composable(Routes.LETTER) { LetterContentScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.LETTER_CONTENT) { LetterContentScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
        }
    }
}
