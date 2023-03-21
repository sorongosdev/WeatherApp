package com.sorongos.weatherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**400개만 요청*/
interface WeatherService {
    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst?pageNo=1&numOfRows=400&dataType=JSON")
    fun getVillageForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int,
    ) : Call<WeatherEntity>
}