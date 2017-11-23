package com.ibtikar.firstkotlinapp

/**
 * Created by norhan.elsawi on 11/23/2017.
 */
data class LocationObject(var userId: String, var latitude: Double, var Longitude: Double) {
    constructor() : this("", 0.00, 0.00)
}
