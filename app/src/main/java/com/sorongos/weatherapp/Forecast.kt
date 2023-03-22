package com.sorongos.weatherapp

data class Forecast(
    val forecastDate: String,
    val forecastTime: String,

    var temperature: Double = 0.0,
    var sky: String = "",
    var precipitation: Int = 0,
    var precipitationType: String = "",
) {
    /**비올때는 하늘 상태가 흐림으로 표시됨 -> 합치는 작업*/
//    0 -> "없음"
//    1 -> "비"
//    2 -> "비/눈"
//    3 -> "눈"
//    4 -> "소나기"
//    else -> ""
    val weather: String
        get() {
            return if (precipitationType == "" || precipitationType == "없음") sky else precipitationType
        }
}
