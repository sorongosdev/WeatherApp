package com.sorongos.weatherapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices

/**위젯 클릭시 업데이트, notification channel과 notification을 생성*/
class UpdateWeatherService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**서비스에서 사용가능한 메서드*/
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // notification channel
        createChannel()
        // change to foreground service
        startForeground(1, createNotification())


        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)

        //위치 가져와서 업데이트
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //권한이 없을 때는 setting activity로
            val pendingIntent: PendingIntent = Intent(this, SettingActivity::class.java).let {
                PendingIntent.getActivity(this, 2, it, PendingIntent.FLAG_IMMUTABLE)
            }

            //remoteview에서 업데이트
            RemoteViews(packageName, R.layout.widget_weather).apply {
                setTextViewText(R.id.temperatureTextView, "권한없음")
                setTextViewText(R.id.weatherTextView,"")
                setOnClickPendingIntent(
                    R.id.temperatureTextView,
                    pendingIntent
                ) //to setting activity
            }.also { remoteViews ->
                val appWidgetName = ComponentName(this, WeatherAppWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
            }

            stopSelf()

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
                    //no permission
                    failureCallback = {
                        val pendingServiceIntent: PendingIntent =
                            Intent(this, UpdateWeatherService::class.java)
                                .let { intent ->
                                    PendingIntent.getService(
                                        this,
                                        1,
                                        intent,
                                        PendingIntent.FLAG_IMMUTABLE
                                    )
                                }

                        RemoteViews(packageName, R.layout.widget_weather).apply {
                            setTextViewText(
                                R.id.temperatureTextView,
                                "error"
                            )
                            setTextViewText(
                                R.id.weatherTextView,
                                ""
                            )
                            setOnClickPendingIntent(R.id.temperatureTextView, pendingServiceIntent)
                        }.also { remoteViews ->
                            val appWidgetName =
                                ComponentName(this, WeatherAppWidgetProvider::class.java)
                            appWidgetManager.updateAppWidget(appWidgetName, remoteViews)
                        }
                        stopSelf()
                    }
                )
            }

        return super.onStartCommand(intent, flags, startId)
    }

    /**To create notification channel*/
    private fun createChannel() {
        val channel = NotificationChannel(
            "widget_refresh_channel",
            "날씨앱",
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description = "위젯을 업데이트하는 채널"

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    /**create notification*/
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("날씨앱")
            .setContentText("날씨 업데이트")
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        //foreground service 선언시 필요
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        const val NOTIFICATION_CHANNEL = "widget_refresh_channel"
    }
}