package com.sorongos.weatherapp

import com.google.gson.annotations.SerializedName

data class WeatherEntity(
    @SerializedName("response")
    val response: WeatherResponse
)

data class WeatherResponse(
    @SerializedName("header")
    val header: WeatherHeader,
    @SerializedName("body")
    val body: WeatherBody,
)

data class WeatherHeader(
    @SerializedName("resultCode")
    val resultCode: String,
    @SerializedName("resultMsg")
    val resultMessage: String
)

data class WeatherBody(
    @SerializedName("items")
    val items: ForecastEntityList
)

data class ForecastEntityList(
    @SerializedName("item")
    val forecastEntities: List<ForecastEntity>
)
data class ForecastEntity(
    val baseData: String,
    val baseTime: String,
    val category: String,
    @SerializedName("fcstDate")
    val forecastDate: String,
    @SerializedName("fcstTime")
    val forecastTime: String,
    @SerializedName("fcstValue")
    val forecastValue: String,
    val nx: Int,
    val ny: Int,
)