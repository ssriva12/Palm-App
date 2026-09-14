package com.palmz.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palmz.domain.repository.HoroscopeRepository
import com.palmz.domain.repository.ProfileRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

/**
 * Runs daily (see [HoroscopeScheduler]): fetch the day's bundle — real, stale-cached, or the
 * bundled evergreen pool, [HoroscopeRepository] decides — then post the nudge. A bundle only
 * fails to resolve if the fallback asset itself is unreadable, so that path just retries.
 */
@HiltWorker
class DailyBundleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val horoscopeRepository: HoroscopeRepository,
    private val notifier: HoroscopeNotifier,
    private val scheduler: HoroscopeScheduler,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val profile = profileRepository.profile.first()
        val zodiac = profile.zodiac
        if (zodiac == null) {
            scheduler.scheduleNext() // not onboarded — nothing to say yet, but still queue tomorrow
            return Result.success()
        }
        return try {
            val bundle = horoscopeRepository.dailyBundle(zodiac, profile.languageCode)
            notifier.notifyDailyHoroscope(zodiac, bundle)
            scheduler.scheduleNext()
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("Palmlens", "daily horoscope worker failed (attempt $runAttemptCount)", e)
            if (runAttemptCount < MAX_ATTEMPTS) {
                Result.retry() // same chain link retries with its own backoff — no new one queued
            } else {
                scheduler.scheduleNext()
                Result.success()
            }
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
