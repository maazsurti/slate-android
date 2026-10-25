package com.maazsurti.slate.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.EmojiFoodBeverage
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.OilBarrel
import androidx.compose.material.icons.filled.Soap
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maazsurti.slate.core.domain.model.Category

/** Each shelf gets a wash of colour and a mark, so stock is recognisable at a glance. */
data class CategoryLook(val wash: Color, val ink: Color, val icon: ImageVector)

fun Category.look(): CategoryLook = when (this) {
    Category.Grains -> CategoryLook(Color(0xFFF2ECDF), Color(0xFF7A5B2E), Icons.Filled.Grain)
    Category.Dairy -> CategoryLook(Color(0xFFE8F0F7), Color(0xFF2F5C8A), Icons.Filled.LocalDrink)
    Category.Oil -> CategoryLook(Color(0xFFFBF1D6), Color(0xFF8A6400), Icons.Filled.OilBarrel)
    Category.Tea -> CategoryLook(Color(0xFFF3E9E1), Color(0xFF7A4A2B), Icons.Filled.EmojiFoodBeverage)
    Category.Snacks -> CategoryLook(Color(0xFFFBE9E7), Color(0xFF9F2F2D), Icons.Filled.Cookie)
    Category.Cleaning -> CategoryLook(Color(0xFFE7F1EA), Color(0xFF2F5D45), Icons.Filled.CleaningServices)
    Category.Care -> CategoryLook(Color(0xFFEDEAF7), Color(0xFF4B3F72), Icons.Filled.Soap)
    Category.Spices -> CategoryLook(Color(0xFFFBEDE0), Color(0xFF9A4A1F), Icons.Filled.Spa)
    Category.Other -> CategoryLook(Color(0xFFEFEDE7), Color(0xFF79736A), Icons.Filled.Inventory2)
}

@Composable
fun CategoryBadge(category: Category, size: Dp = 34.dp, corner: Dp = 9.dp) {
    val look = category.look()
    Box(
        modifier = Modifier
            .size(size)
            .background(look.wash, RoundedCornerShape(corner)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(look.icon, contentDescription = category.label, tint = look.ink, modifier = Modifier.size(size * 0.5f))
    }
}

/** Pastel circles for customer initials, picked from the account id. */
private val AccountWashes = listOf(
    Color(0xFFE7F1EA) to Color(0xFF2F5D45),
    Color(0xFFFBE9E7) to Color(0xFF9F2F2D),
    Color(0xFFE8F0F7) to Color(0xFF2F5C8A),
    Color(0xFFEDEAF7) to Color(0xFF4B3F72),
    Color(0xFFFBF1D6) to Color(0xFF8A6400),
)

fun accountWash(id: Long): Pair<Color, Color> = AccountWashes[(id.mod(AccountWashes.size.toLong())).toInt()]
