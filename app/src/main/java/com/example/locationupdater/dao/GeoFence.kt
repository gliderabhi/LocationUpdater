package com.example.locationupdater.dao

import java.time.LocalDateTime

data class GeoFence(
    var lat : Float?= 0f,
    var long : Float? = 0f,
    var radius : Float? = 0f,
    var id : String? = LocalDateTime.now().toString()){
    companion object{

    }
}