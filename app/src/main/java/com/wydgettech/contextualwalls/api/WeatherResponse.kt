package com.wydgettech.contextualwalls.api

data class WeatherResponse (
    var coord: Coord,
    var weather: List<Weather>,
    var main: Main,
    var wind: Wind,
    var rain: Rain,
    var clouds: Clouds,
    var dt: Float,
    var sys: Sys,
    var id: Int,
    var name: String,
    var cod: Float

)

data class Weather (
    var id: Int,
    var main: String,
    var description: String,
    var icon: String
)

data class Clouds (
    var all: Float
)

data class Rain (
    var h3: Float
)

data class Wind (
    var speed: Float,
    var deg: Float
)

data class Main (
    var temp: Float,
    var humidity: Float,
    var pressure: Float,
    var temp_min: Float,
    var temp_max: Float
)

data class Sys (
    var country: String,
    var sunrise: Long,
    var sunset: Long
)

data class Coord (
    var lon: Float,
    var lat: Float
)