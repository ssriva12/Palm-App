package com.palmz.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.palmz.MainActivity
import com.palmz.R
import com.palmz.domain.model.DailyBundle
import com.palmz.domain.model.Zodiac
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** The one daily-horoscope nudge (spec §3.9): its own channel, one post per day, teasing copy. */
@Singleton
class HoroscopeNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** No-op when the user has notifications switched off — the worker still counts as done. */
    fun notifyDailyHoroscope(zodiac: Zodiac, bundle: DailyBundle) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        ensureChannel()

        val today = LocalDate.now().dayOfYear
        val tapIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(TITLES[today % TITLES.size])
            .setContentText("${zodiac.displayName}: ${bundle.highlights.mood.lowercase()}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bundle.highlights.oneLineAdvice))
            .setAutoCancel(true)
            .setContentIntent(tapIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Daily horoscope",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = "One gentle nudge each morning with the day's reading." }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "horoscope"
        const val NOTIFICATION_ID = 1001

        val TITLES = listOf(
            "Your stars have news",
            "Today's reading is ready",
            "The day's energy just landed",
            "A fresh horoscope is waiting",
            "The cards are turned. Curious?",
        )
    }
}
