package com.bitdesal.tabala.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import com.bitdesal.tabala.R

data class TopLevelDestination(
    val navKey: NavKey,
    @StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
)

val TopLevelDestinations: List<TopLevelDestination> = listOf(
    TopLevelDestination(IniciNavKey, R.string.nav_inici, R.drawable.ic_inici),
    TopLevelDestination(MuixerangesNavKey, R.string.nav_muixeranges, R.drawable.ic_muixeranges),
    TopLevelDestination(FiguresNavKey, R.string.nav_figures, R.drawable.ic_figures),
    TopLevelDestination(PerfilNavKey, R.string.nav_perfil, R.drawable.ic_perfil),
)

fun NavKey.toTopLevelDestination(): TopLevelDestination? =
    TopLevelDestinations.find { it.navKey == this }
