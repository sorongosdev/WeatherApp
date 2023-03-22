package com.sorongos.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.sorongos.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Objects.isNull

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationCallback: LocationCallback

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                updateLocation()
            }
            else -> {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))


    }

    private fun updateLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                Log.e("lastLocation", location.toString())
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://apis.data.go.kr/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(WeatherService::class.java)

                val baseDateTime = BaseDateTime.getBaseDateTime()
                val converter = GeoPointConverter()
                val point = converter.convert(lon = location.longitude, lat = location.latitude)
                service.getVillageForecast(
                    serviceKey = "DmBM2x0wpsJXPEO9ulpEtKw6+h8WozAD6uU7ngLMtXTiJiVIC6HxK80W4DU7/S+1mHutkaHsY1h4qjg72USKEQ==",
                    baseTime = baseDateTime.baseTime,
                    baseDate = baseDateTime.baseDate,
                    nx = point.nx,
                    ny = point.ny
                ).enqueue(object : Callback<WeatherEntity> {
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

                        // 정렬 후 첫번째 값은 현재 것임
                        val currentForecast = list.first()
                        binding.temperatureTextView.text = getString(R.string.temperature_text,currentForecast.temperature)
                        binding.skyTextView.text = currentForecast.weather
                        binding.precipitationTextView.text = getString(R.string.precipitation_text, currentForecast.precipitation)

                        Log.e("Forecast", forecastDateTimeMap.toString())
                    }

                    override fun onFailure(call: Call<WeatherEntity>, t: Throwable) {
                        t.printStackTrace()
                    }

                })
            }
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