package com.palmlens.notification

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.palmlens.domain.repository.HoroscopeRepository
import com.palmlens.domain.repository.ProfileRepository
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
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val profile = profileRepository.profile.first()
        val zodiac = profile.zodiac ?: return Result.success() // not onboarded — nothing to say yet
        return try {
            val bundle = horoscopeRepository.dailyBundle(zodiac, profile.languageCode)
            notifier.notifyDailyHoroscope(zodiac, bundle)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("Palmlens", "daily horoscope worker failed (attempt $runAttemptCount)", e)
            if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.success()
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
