package com.sorongos.weatherapp

import android.util.Log
import com.sorongos.weatherapp.databinding.ItemForecastBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**서비스에서 데이터를 가져오는 중간 클래스*/
object WeatherRepository {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(WeatherService::class.java)

    fun getVillageForecast(
        longitude: Double,
        latitude: Double,
        //비동기로 일어나고 있기 때문에 콜백구현
        successCallback: (List<Forecast>) -> Unit,
        failureCallback: (Throwable) -> Unit
    ) {
        val baseDateTime = BaseDateTime.getBaseDateTime()
        val converter = GeoPointConverter()
        val point = converter.convert(lon = longitude, lat = latitude)
        service.getVillageForecast(
            serviceKey = "DmBM2x0wpsJXPEO9ulpEtKw6+h8WozAD6uU7ngLMtXTiJiVIC6HxK80W4DU7/S+1mHutkaHsY1h4qjg72USKEQ==",
            baseTime = baseDateTime.baseTime,
            baseDate = baseDateTime.baseDate,
            nx = point.nx,
            ny = point.ny
        ).enqueue(
            object : Callback<WeatherEntity> {
                override fun onResponse(
                    call: Call<WeatherEntity>,
                    response: Response<WeatherEntity>
                ) {
                    //String은 날짜/시간 형태임 -> 20230323/0300
                    val forecastDateTimeMap = mutableMapOf<String, Forecast>()
                    val forecastList =
                        response.body()?.response?.body?.items?.forecastEntities.orEmpty()

                    for (forecast in forecastList) {
//                            Log.e("Forecast", forecast.toString())

                        if (forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"] == null) {
                            forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"] =
                                Forecast(
                                    forecastDate = forecast.forecastDate,
                                    forecastTime = forecast.forecastTime
                                )
                        }

                        forecastDateTimeMap["${forecast.forecastDate}/${forecast.forecastTime}"]?.apply {
                            when (forecast.category) {
                                Category.POP -> precipitation = forecast.forecastValue.toInt()
                                Category.PTY -> precipitationType = transformRainType(forecast)
                                Category.SKY -> sky = transformSky(forecast)
                                Category.TMP -> temperature = forecast.forecastValue.toDouble()
                                else -> {}
                            }
                        }
                    }

                    //정렬
                    val list = forecastDateTimeMap.values.toMutableList()
                    list.sortWith { f1, f2 ->
                        val f1DateTime = "${f1.forecastDate}${f1.forecastTime}"
                        val f2DateTime = "${f2.forecastDate}${f2.forecastTime}"

                        return@sortWith f1DateTime.compareTo(f2DateTime)
                    }

                    if(list.isEmpty()){
                      failureCallback(NullPointerException())
                    } else successCallback(list) //main 콜백에서 구현

                    // 정렬 후 첫번째 값은 현재 것임
                    val currentForecast = list.first()
                }

                override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                    failureCallback(t)
                    t.printStackTrace()
                }

            })
    }


    private fun transformRainType(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            0 -> "없음"
            1 -> "비"
            2 -> "비/눈"
            3 -> "눈"
            4 -> "소나기"
            else -> ""
        }
    }

    private fun transformSky(forecast: ForecastEntity): String {
        return when (forecast.forecastValue.toInt()) {
            1 -> "맑음"
            3 -> "구름많음"
            4 -> "흐림"
            else -> "해당없음"
        }
    }
}