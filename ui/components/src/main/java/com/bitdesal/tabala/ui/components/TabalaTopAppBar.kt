package com.bitdesal.tabala.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bitdesal.tabala.ui.components.R
import com.bitdesal.tabala.ui.theme.TabalaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabalaTopAppBar(
    title: String,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    @DrawableRes groupLogoResId: Int?,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = {
            if (groupLogoResId != null) {
                Image(
                    painter = painterResource(groupLogoResId),
                    contentDescription = null,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.app_bar_logo))
                        .clip(CircleShape),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Preview(name = "Top level — no group", showBackground = true)
@Composable
private fun TabalaTopAppBarTopLevelPreview() {
    TabalaTheme {
        TabalaTopAppBar(
            title = "Tabala",
            showBackButton = false,
            onBackClick = {},
            groupLogoResId = null,
        )
    }
}

@Preview(name = "Second level — with group", showBackground = true)
@Composable
private fun TabalaTopAppBarSecondLevelPreview() {
    TabalaTheme {
        TabalaTopAppBar(
            title = "Muixeranga d'Elx",
            showBackButton = true,
            onBackClick = {},
            groupLogoResId = R.drawable.logo_muixeranga_elx,
        )
    }
}
