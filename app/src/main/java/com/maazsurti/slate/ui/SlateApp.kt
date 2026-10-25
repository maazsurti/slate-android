package com.maazsurti.slate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maazsurti.slate.ui.common.Hairline
import com.maazsurti.slate.ui.ledger.CustomerDetailScreen
import com.maazsurti.slate.ui.ledger.LedgerScreen
import com.maazsurti.slate.ui.reports.ReportsScreen
import com.maazsurti.slate.ui.sale.SaleScreen
import com.maazsurti.slate.ui.stock.StockScreen
import com.maazsurti.slate.ui.theme.Muted

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("sale", "Sale", Icons.Filled.PointOfSale),
    Tab("stock", "Stock", Icons.Filled.Inventory2),
    Tab("ledger", "Ledger", Icons.Filled.AccountBalanceWallet),
    Tab("reports", "Reports", Icons.Filled.QueryStats),
)

@Composable
fun SlateApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SlateBottomBar(
                currentRoute = currentRoute,
                onSelect = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "sale",
            modifier = Modifier.fillMaxSize().padding(padding),
            enterTransition = { fadeIn(tween(140)) },
            exitTransition = { fadeOut(tween(90)) },
            popEnterTransition = { fadeIn(tween(140)) },
            popExitTransition = { fadeOut(tween(90)) },
        ) {
            composable("sale") { SaleScreen() }
            composable("stock") { StockScreen() }
            composable("ledger") {
                LedgerScreen(onOpenCustomer = { id -> navController.navigate("ledger/$id") })
            }
            composable("ledger/{customerId}") { entry ->
                CustomerDetailScreen(
                    customerId = entry.arguments?.getString("customerId")?.toLongOrNull() ?: 0L,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("reports") { ReportsScreen() }
        }
    }
}

@Composable
private fun SlateBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Hairline()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    val tint = if (selected) MaterialTheme.colorScheme.onBackground else Muted
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(tab.route) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(18.dp)
                                .background(if (selected) MaterialTheme.colorScheme.onBackground else Color.Transparent),
                        )
                        Spacer(Modifier.height(8.dp))
                        Icon(tab.icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
                        Spacer(Modifier.height(5.dp))
                        Text(
                            tab.label,
                            color = tint,
                            fontSize = 10.5.sp,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}
