package com.bitdesal.tabala.domain.model

import androidx.annotation.DrawableRes

data class UserGroup(
    val id: String,
    val name: String,
    val logoResId: Int? = null,
)
