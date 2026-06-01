package com.bitdesal.tabala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.bitdesal.tabala.navigation.IniciNavKey
import com.bitdesal.tabala.navigation.TopLevelDestinations
import com.bitdesal.tabala.navigation.navigateToTopLevel
import com.bitdesal.tabala.navigation.tabalaEntryProvider
import com.bitdesal.tabala.navigation.toTopLevelDestination
import com.bitdesal.tabala.ui.components.TabalaTopAppBar
import com.bitdesal.tabala.ui.main.MainViewModel
import com.bitdesal.tabala.ui.theme.PrimaryMain
import com.bitdesal.tabala.ui.theme.TabalaTheme
import org.koin.androidx.compose.koinViewModel

/**
 * This is the single main activity that orchestrates all the composables in the app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TabalaTheme {
                TabalaApp()
            }
        }
    }
}

@Composable
fun TabalaApp() {
    val backStack = rememberNavBackStack(IniciNavKey)
    val currentDestination = backStack.lastOrNull()?.toTopLevelDestination()
        ?: TopLevelDestinations.first()

    val viewModel: MainViewModel = koinViewModel()
    val userGroup by viewModel.userGroup.collectAsStateWithLifecycle()

    val title = userGroup?.name ?: stringResource(R.string.app_title)
    val showBackButton = backStack.size > 1

    val navItemColors = NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryMain,
            selectedTextColor = PrimaryMain,
            indicatorColor = Color.Transparent
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = PrimaryMain,
            selectedTextColor = PrimaryMain,
            indicatorColor = Color.Transparent
        ),
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestinations.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = stringResource(destination.labelRes),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(destination.labelRes),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                    selected = destination.navKey == currentDestination.navKey,
                    onClick = { backStack.navigateToTopLevel(destination.navKey) },
                    colors = navItemColors,
                )
            }
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TabalaTopAppBar(
                    title = title,
                    showBackButton = showBackButton,
                    onBackClick = { backStack.removeLastOrNull() },
                    groupLogoResId = userGroup?.logoResId,
                )
            },
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryProvider = tabalaEntryProvider(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ),
            )
        }
    }
}
