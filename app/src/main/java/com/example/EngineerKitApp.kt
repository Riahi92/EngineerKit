package com.example

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.data.PreferencesRepository
import com.example.core.data.ToolCatalog
import com.example.core.designsystem.component.EngineerKitBottomNavigation
import com.example.core.designsystem.component.EngineerKitScaffold
import com.example.core.designsystem.component.EngineerKitTopBar
import com.example.core.designsystem.component.ToolSpecBottomSheet
import com.example.core.model.CategoryId
import com.example.core.model.EngineeringTool
import com.example.core.navigation.EngineerKitDestinations
import com.example.feature.calculator.AnalogSignalScalingScreen
import com.example.feature.calculator.AxialStressScreen
import com.example.feature.calculator.BeamScreen
import com.example.feature.calculator.BooleanLogicScreen
import com.example.feature.calculator.ConcreteVolumeScreen
import com.example.feature.calculator.CurrentLoop4To20mAScreen
import com.example.feature.calculator.DistributedLoadScreen
import com.example.feature.calculator.EnergyCo2Screen
import com.example.feature.calculator.EquivalentResistanceScreen
import com.example.feature.calculator.HeatConductionScreen
import com.example.feature.calculator.HydraulicCylinderFlowScreen
import com.example.feature.calculator.HydraulicCylinderForceScreen
import com.example.feature.calculator.MassVolumeScreen
import com.example.feature.calculator.MolarConcentrationScreen
import com.example.feature.calculator.NumberBaseConverterScreen
import com.example.feature.calculator.OeeScreen
import com.example.feature.calculator.OhmsLawScreen
import com.example.feature.calculator.ProductionCapacityScreen
import com.example.feature.calculator.QuadraticScreen
import com.example.feature.calculator.RcTimeConstantScreen
import com.example.feature.calculator.ScientificCalculatorScreen
import com.example.feature.calculator.ShaftTorsionScreen
import com.example.feature.calculator.SolutionDilutionScreen
import com.example.feature.calculator.StatisticsScreen
import com.example.feature.calculator.TaktTimeScreen
import com.example.feature.calculator.ThermalExpansionScreen
import com.example.feature.calculator.ThreePhaseScreen
import com.example.feature.calculator.UnitConverterScreen
import com.example.feature.calculator.VoltageDividerScreen
import com.example.feature.calculator.VoltageDropScreen
import com.example.feature.category.CategoryDetailScreen
import com.example.feature.favorites.FavoritesScreen
import com.example.feature.favorites.FavoritesViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.feature.profile.ProfileScreen
import com.example.feature.profile.ProfileViewModel
import com.example.feature.search.SearchScreen
import com.example.feature.search.SearchViewModel
import com.example.feature.tools.ToolsScreen
import com.example.feature.tools.ToolsViewModel

@Composable
fun EngineerKitApp(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: EngineerKitDestinations.HOME_ROUTE

    val context = LocalContext.current
    val preferencesRepository = remember { PreferencesRepository.getInstance(context) }
    val favoriteIds by preferencesRepository.favoriteIds.collectAsStateWithLifecycle()

    var selectedToolForSpec by remember { mutableStateOf<EngineeringTool?>(null) }

    val isTopLevelDestination = currentRoute in listOf(
        EngineerKitDestinations.HOME_ROUTE,
        EngineerKitDestinations.TOOLS_ROUTE,
        EngineerKitDestinations.SEARCH_ROUTE,
        EngineerKitDestinations.FAVORITES_ROUTE,
        EngineerKitDestinations.PROFILE_ROUTE
    )

    val handleToolClick: (EngineeringTool) -> Unit = remember(navController, preferencesRepository) {
        { tool ->
            preferencesRepository.addRecentTool(tool.id)
            if (tool.isAvailable) {
                navController.navigate(EngineerKitDestinations.calculatorRoute(tool.id))
            } else {
                selectedToolForSpec = tool
            }
        }
    }

    EngineerKitScaffold(
        modifier = modifier,
        topBar = {
            if (isTopLevelDestination && currentRoute != EngineerKitDestinations.HOME_ROUTE) {
                val title = when (currentRoute) {
                    EngineerKitDestinations.TOOLS_ROUTE -> "Outils d'ingénierie"
                    EngineerKitDestinations.SEARCH_ROUTE -> "Recherche d'outils"
                    EngineerKitDestinations.FAVORITES_ROUTE -> "Mes favoris"
                    EngineerKitDestinations.PROFILE_ROUTE -> "Profil & Réglages"
                    else -> "EngineerKit"
                }

                EngineerKitTopBar(
                    title = title,
                    showBrandLogo = false,
                    onSettingsClick = if (currentRoute != EngineerKitDestinations.PROFILE_ROUTE) {
                        { navController.navigate(EngineerKitDestinations.PROFILE_ROUTE) }
                    } else null
                )
            }
        },
        bottomBar = {
            if (isTopLevelDestination) {
                EngineerKitBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = EngineerKitDestinations.HOME_ROUTE,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(100)) },
            exitTransition = { fadeOut(animationSpec = tween(100)) },
            popEnterTransition = { fadeIn(animationSpec = tween(100)) },
            popExitTransition = { fadeOut(animationSpec = tween(100)) }
        ) {
            composable(EngineerKitDestinations.HOME_ROUTE) {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onCategoryClick = { catId ->
                        navController.navigate(EngineerKitDestinations.categoryDetailRoute(catId.name))
                    },
                    onToolClick = handleToolClick,
                    onNavigateToSearch = {
                        navController.navigate(EngineerKitDestinations.SEARCH_ROUTE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(EngineerKitDestinations.PROFILE_ROUTE)
                    },
                    onNavigateToTools = {
                        navController.navigate(EngineerKitDestinations.TOOLS_ROUTE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(EngineerKitDestinations.TOOLS_ROUTE) {
                val toolsViewModel: ToolsViewModel = viewModel()
                ToolsScreen(
                    viewModel = toolsViewModel,
                    onCategoryClick = { catId ->
                        navController.navigate(EngineerKitDestinations.categoryDetailRoute(catId.name))
                    },
                    onToolClick = handleToolClick,
                    onNavigateToSearch = {
                        navController.navigate(EngineerKitDestinations.SEARCH_ROUTE)
                    }
                )
            }

            composable(EngineerKitDestinations.SEARCH_ROUTE) {
                val searchViewModel: SearchViewModel = viewModel()
                SearchScreen(
                    viewModel = searchViewModel,
                    onToolClick = handleToolClick
                )
            }

            composable(EngineerKitDestinations.FAVORITES_ROUTE) {
                val favoritesViewModel: FavoritesViewModel = viewModel()
                FavoritesScreen(
                    viewModel = favoritesViewModel,
                    onToolClick = handleToolClick,
                    onExploreTools = {
                        navController.navigate(EngineerKitDestinations.TOOLS_ROUTE)
                    }
                )
            }

            composable(EngineerKitDestinations.PROFILE_ROUTE) {
                val profileViewModel: ProfileViewModel = viewModel()
                ProfileScreen(
                    viewModel = profileViewModel
                )
            }

            composable(
                route = EngineerKitDestinations.CATEGORY_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryId") ?: ""
                val categoryId = try {
                    CategoryId.valueOf(categoryName)
                } catch (_: Exception) {
                    CategoryId.MECANIQUE
                }

                CategoryDetailScreen(
                    categoryId = categoryId,
                    favoriteIds = favoriteIds,
                    onFavoriteToggle = { toolId ->
                        preferencesRepository.toggleFavorite(toolId)
                    },
                    onToolClick = handleToolClick,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Route for all 30 active engineering calculators
            composable(
                route = EngineerKitDestinations.CALCULATOR_ROUTE,
                arguments = listOf(
                    navArgument("calculatorId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val calcId = backStackEntry.arguments?.getString("calculatorId") ?: ""
                val isFav = favoriteIds.contains(calcId)
                val toggleFav = { preferencesRepository.toggleFavorite(calcId) }
                val onBack: () -> Unit = { navController.popBackStack() }

                when (calcId) {
                    // Phase 2 Tools (1–10)
                    "tool_sci_calc" -> ScientificCalculatorScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_unit_converter" -> UnitConverterScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_math_quadratic" -> QuadraticScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_math_statistics" -> StatisticsScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_meca_stress" -> AxialStressScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_meca_mass_volume" -> MassVolumeScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_meca_beam" -> BeamScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_elec_ohm" -> OhmsLawScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_elec_threephase" -> ThreePhaseScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_indus_oee" -> OeeScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )

                    // Phase 3 Tools (11–30)
                    "tool_meca_torsion" -> ShaftTorsionScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_elec_cable" -> VoltageDropScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_elec_resistors_eq" -> EquivalentResistanceScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_electron_divider" -> VoltageDividerScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_elec_filter_rc" -> RcTimeConstantScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_civil_concrete" -> ConcreteVolumeScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_civil_distributed_load" -> DistributedLoadScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_hydro_cylinder_force" -> HydraulicCylinderForceScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_hydro_cylinder_flow" -> HydraulicCylinderFlowScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_therm_conduction" -> HeatConductionScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_therm_expansion" -> ThermalExpansionScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_indus_takt" -> TaktTimeScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_indus_capacity" -> ProductionCapacityScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_chem_molarity" -> MolarConcentrationScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_chem_dilution" -> SolutionDilutionScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_cs_base_converter" -> NumberBaseConverterScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_cs_boolean_logic" -> BooleanLogicScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_auto_analog_scaling" -> AnalogSignalScalingScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_auto_sensor_scale" -> CurrentLoop4To20mAScreen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )
                    "tool_env_co2" -> EnergyCo2Screen(
                        isFavorite = isFav,
                        onFavoriteToggle = toggleFav,
                        onNavigateBack = onBack
                    )

                    else -> {
                        val tool = ToolCatalog.getToolById(calcId)
                        if (tool != null) {
                            ToolSpecBottomSheet(
                                tool = tool,
                                isFavorite = isFav,
                                onFavoriteToggle = { toggleFav() },
                                onDismiss = onBack
                            )
                        }
                    }
                }
            }
        }

        // Shared Spec Sheet bottom modal for any tapped upcoming/planned tool
        selectedToolForSpec?.let { tool ->
            ToolSpecBottomSheet(
                tool = tool,
                isFavorite = favoriteIds.contains(tool.id),
                onFavoriteToggle = { toolId ->
                    preferencesRepository.toggleFavorite(toolId)
                },
                onDismiss = {
                    selectedToolForSpec = null
                }
            )
        }
    }
}
