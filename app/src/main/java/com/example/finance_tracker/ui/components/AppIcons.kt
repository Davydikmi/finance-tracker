package com.example.finance_tracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Маппер «имя иконки из БД → Material ImageVector».
 * В БД имена хранятся строками ("restaurant" и т.д.); незнакомое имя
 * заменяется на нейтральную иконку категории.
 */
object AppIcons {

    private val map: Map<String, ImageVector> = mapOf(
        "restaurant" to Icons.Outlined.Restaurant,
        "directions_bus" to Icons.Outlined.DirectionsBus,
        "home" to Icons.Outlined.Home,
        "movie" to Icons.Outlined.Movie,
        "medical_services" to Icons.Outlined.MedicalServices,
        "shopping_bag" to Icons.Outlined.ShoppingBag,
        "smartphone" to Icons.Outlined.Smartphone,
        "category" to Icons.Outlined.Category,
        "payments" to Icons.Outlined.Payments,
        "card_giftcard" to Icons.Outlined.CardGiftcard,
        "attach_money" to Icons.Outlined.AttachMoney,
        "savings" to Icons.Outlined.Savings,
        "fitness_center" to Icons.Outlined.FitnessCenter,
        "pets" to Icons.Outlined.Pets,
        "flight" to Icons.Outlined.Flight,
        "school" to Icons.Outlined.School
    )

    /** Имена, доступные для выбора в редакторе категорий. */
    val selectableNames: List<String> = map.keys.toList()

    fun vectorFor(name: String?): ImageVector =
        name?.let { map[it] } ?: Icons.Outlined.Category
}
