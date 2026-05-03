package com.example.trazoatrazo.navigation


import HomeScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trazoatrazo.drawings.Animals.TurtleScreen
import com.example.trazoatrazo.drawings.Shapes.HeartScreen
import com.example.trazoatrazo.drawings.flowers.GirasolScreen
import com.example.trazoatrazo.drawings.flowers.ImprovedSunflowerScreen
import com.example.trazoatrazo.ui.Screen.CategoryScreen
import com.tuapp.drawbloom.drawings.flowers.FlowerScreen


@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        // ── Home ─────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.category(categoryId))
                }
            )
        }

        // ── Categoría ────────────────────────────────────────────────────────
        composable(Routes.CATEGORY) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            CategoryScreen(
                categoryId = categoryId,
                onDrawingClick = { drawingId ->
                    navController.navigate(Routes.drawing(categoryId, drawingId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Dibujos ──────────────────────────────────────────────────────────
        composable(Routes.DRAWING) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val drawingId  = backStackEntry.arguments?.getString("drawingId")  ?: ""

            when (categoryId) {
                Routes.Category.FLOWERS -> {
                    when (drawingId) {
                        Routes.Drawings.GIRASOL ->
                            GirasolScreen(onBack = { navController.popBackStack() })
                        Routes.Drawings.FLORES  ->
                            FlowerScreen(onBack  = { navController.popBackStack() })
                        Routes.Drawings.IMPROVED_SUNFLOWER ->
                            ImprovedSunflowerScreen(onBack = { navController.popBackStack() })
                    }
                }
                Routes.Category.CARTOONS -> {
                    when(drawingId) {
                        Routes.Drawings.CARTOON -> {
                            // Si tienes una pantalla para esto, agrégala aquí
                        }
                        Routes.Drawings.HEART -> {
                            HeartScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
                Routes.Category.ANIMALS -> {
                    when (drawingId) {
                        Routes.Drawings.TURTLE ->
                            TurtleScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}