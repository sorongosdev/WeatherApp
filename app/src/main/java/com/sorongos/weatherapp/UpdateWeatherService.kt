package com.sorongos.weatherapp

import android.Manifest
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

class UpdateWeatherService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**서비스에서 사용가능한 메서드*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // notification channel
        // change to foreground service

        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

        //위치 가져와서 업데이트
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //todo 위젯을 권한없음 형태로 표시, 클릭했을 때 권한 팝업을 얻을 수 있도록
            return super.onStartCommand(intent, flags, startId)
        }
        LocationServices.getFusedLocationProviderClient(this).lastLocation // permission
            .addOnSuccessListener {
                WeatherRepository.getVillageForecast(
                    longitude = it.longitude,
                    latitude = it.latitude,
                    successCallback = { forecastList ->
                        val currentForecast = forecastList.first()

                        /**누르면 업데이트*/
                        val pendingServiceIntent: PendingIntent =
                            Intent(this, UpdateWeatherService::class.java)
                                .let {
                                    PendingIntent.getService(
                                        this,
                                        1,
                                        it,
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                }

                        /**위젯 업데이트, data push*/
                        RemoteViews(packageName, R.layout.widget_weather).apply {
                            setTextViewText(
                                R.id.temperatureTextView,
                                getString(R.string.temperature_text, currentForecast.temperature)
                            )
                            setTextViewText(
                                R.id.weatherTextView,
                                currentForecast.weather
                            )
                            setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                        }.also { remoteViews ->
                            val appWidgetName =
                                ComponentName(this, WeatherAppWidgetProvider::class.java)
                            appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                        }
                        //종료
                        stopSelf()
                    },
                    failureCallback = {
                        //todo 위젯을 에러 상태로 표시
                        //종료
                        stopSelf()
                    }
                )
            }

        return super.onStartCommand(intent, flags, startId)
    }
}