package com.palmlens.notification

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the daily horoscope worker for ~06:00 local time.
 *
 * A self-rescheduling chain of one-shot requests, not a `PeriodicWorkRequest`: a periodic
 * request's 24h interval is anchored to whenever it last actually ran, so a single delayed
 * run (Doze, no network at 6am, etc.) permanently shifts every run after it to that later
 * time. Re-deriving "next 6am local" on every completion (see [scheduleNext], called by
 * [DailyBundleWorker]) means one delayed morning never drifts the rest.
 */
@Singleton
class HoroscopeScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Call once on app start. Idempotent — leaves an already-running chain alone. */
    fun scheduleDaily() {
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, buildRequest())
    }

    /** Call from [DailyBundleWorker] once it's truly done (success or given up) to queue
     *  tomorrow's 06:00 run — never from a [androidx.work.ListenableWorker.Result.retry], so a
     *  transient failure keeps its own backoff instead of spawning a second chain link. */
    fun scheduleNext() {
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, buildRequest())
    }

    private fun buildRequest(): OneTimeWorkRequest =
        OneTimeWorkRequestBuilder<DailyBundleWorker>()
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
