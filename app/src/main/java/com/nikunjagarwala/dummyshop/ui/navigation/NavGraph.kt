package com.nikunjagarwala.dummyshop.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nikunjagarwala.dummyshop.DummyShopApp
import com.nikunjagarwala.dummyshop.ui.cart.CartScreen
import com.nikunjagarwala.dummyshop.ui.cart.CartViewModel
import com.nikunjagarwala.dummyshop.ui.common.SimpleViewModelFactory
import com.nikunjagarwala.dummyshop.ui.detail.ProductDetailScreen
import com.nikunjagarwala.dummyshop.ui.detail.ProductDetailViewModel
import com.nikunjagarwala.dummyshop.ui.list.ProductListScreen
import com.nikunjagarwala.dummyshop.ui.list.ProductListViewModel
import com.nikunjagarwala.dummyshop.ui.wishlist.WishlistScreen
import com.nikunjagarwala.dummyshop.ui.wishlist.WishlistViewModel

private data class BottomTab(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun DummyShopNavHost(app: DummyShopApp) {
    val navController = rememberNavController()
    val tabs = listOf(
        BottomTab(Routes.LIST, "Products") { Icon(Icons.Filled.Home, contentDescription = null) },
        BottomTab(Routes.WISHLIST, "Wishlist") { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
        BottomTab(Routes.CART, "Cart") { Icon(Icons.Filled.ShoppingCart, contentDescription = null) }
    )

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = tab.icon,
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LIST,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LIST) {
                val viewModel: ProductListViewModel = viewModel(
                    factory = SimpleViewModelFactory { ProductListViewModel(app.repository, app.connectivityObserver) }
                )
                ProductListScreen(viewModel = viewModel, onProductClick = { id -> navController.navigate(Routes.detail(id)) })
            }
            composable(Routes.WISHLIST) {
                val viewModel: WishlistViewModel = viewModel(
                    factory = SimpleViewModelFactory { WishlistViewModel(app.repository) }
                )
                WishlistScreen(viewModel = viewModel, onProductClick = { id -> navController.navigate(Routes.detail(id)) })
            }
            composable(Routes.CART) {
                val viewModel: CartViewModel = viewModel(
                    factory = SimpleViewModelFactory { CartViewModel(app.repository) }
                )
                CartScreen(viewModel = viewModel, onProductClick = { id -> navController.navigate(Routes.detail(id)) })
            }
            composable(
                route = Routes.DETAIL_PATTERN,
                arguments = listOf(navArgument("productId") { type = androidx.navigation.NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
                val viewModel: ProductDetailViewModel = viewModel(
                    key = "detail-$productId",
                    factory = SimpleViewModelFactory { ProductDetailViewModel(productId, app.repository) }
                )
                ProductDetailScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
            }
        }
    }
}
