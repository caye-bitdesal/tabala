package com.bitdesal.tabala

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.bitdesal.tabala.navigation.IniciNavKey
import com.bitdesal.tabala.navigation.TopLevelDestinations
import com.bitdesal.tabala.navigation.navigateToTopLevel
import com.bitdesal.tabala.navigation.tabalaEntryProvider
import com.bitdesal.tabala.navigation.toTopLevelDestination
import com.bitdesal.tabala.ui.theme.TabalaTheme

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

@PreviewScreenSizes
@Composable
fun TabalaApp() {
    val backStack = rememberNavBackStack(IniciNavKey)
    val currentDestination = backStack.lastOrNull()?.toTopLevelDestination()
        ?: TopLevelDestinations.first()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestinations.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.iconRes),
                            contentDescription = stringResource(destination.labelRes),
                            tint = Color.Unspecified,
                        )
                    },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = destination.navKey == currentDestination.navKey,
                    onClick = { backStack.navigateToTopLevel(destination.navKey) },
                )
            }
        },
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
