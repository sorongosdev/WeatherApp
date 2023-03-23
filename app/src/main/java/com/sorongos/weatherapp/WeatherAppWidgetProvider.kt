package com.sorongos.weatherapp

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

/**Widget 생성*/
class WeatherAppWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity
            // not right now, keep!
            val pendingIntent: PendingIntent = Intent(context, UpdateWeatherService::class.java)
                .let { intent ->
                    //FLAG_IMMUTABLE 안드로이드12부터 꼭 선언
                    //activity가 아닌 service 실행
                    PendingIntent.getForegroundService(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_weather
            ).apply {
                setOnClickPendingIntent(R.id.temperatureTextView, pendingIntent)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        //api 26이상일 때 foreground service로 동작, 그 미만은 일반 service
        val serviceIntent = Intent(context, UpdateWeatherService::class.java)

        //31 이상버전 -> 예외 처리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
            } catch (e: ForegroundServiceStartNotAllowedException) {
                e.printStackTrace()
            }
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}