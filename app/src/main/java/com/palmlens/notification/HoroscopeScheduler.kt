package com.palmlens.notification

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules the daily horoscope worker for ~06:00 local. Idempotent — safe to call every launch. */
@Singleton
class HoroscopeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun scheduleDaily() {
        val request = PeriodicWorkRequestBuilder<DailyBundleWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(millisUntilNext(NOTIFY_AT, ZonedDateTime.now()), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .build()

        // KEEP: an already-scheduled chain keeps its next-run time; we don't reset it on launch.
        // ponytail: a fixed-period request drifts later over weeks and doesn't track timezone
        //   hops — swap for a self-rescheduling OneTimeWorkRequest if the 06:00 anchor matters.
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    private companion object {
        const val WORK_NAME = "daily-horoscope"
        val NOTIFY_AT: LocalTime = LocalTime.of(6, 0)
    }
}

/** Milliseconds from [now] to the next occurrence of [target] local time (a full day if equal). */
internal fun millisUntilNext(target: LocalTime, now: ZonedDateTime): Long {
    var next = now.toLocalDate().atTime(target).atZone(now.zone)
    if (!next.isAfter(now)) next = next.plusDays(1)
    return Duration.between(now, next).toMillis()
}
