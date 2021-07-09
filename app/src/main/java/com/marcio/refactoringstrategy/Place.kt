package com.marcio.refactoringstrategy

data class Place(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val type: String?,
    var price: Int? = null
)
