package com.maazsurti.slate.core.domain.model

/** How the shop groups its stock. Colour and icon live in the UI layer. */
enum class Category(val label: String) {
    Grains("Grains"),
    Dairy("Dairy"),
    Oil("Oil"),
    Tea("Tea & coffee"),
    Snacks("Snacks"),
    Cleaning("Cleaning"),
    Care("Personal care"),
    Spices("Spices"),
    Other("Other");

    companion object {
        fun fromName(name: String?): Category = entries.firstOrNull { it.name == name } ?: Other
    }
}
