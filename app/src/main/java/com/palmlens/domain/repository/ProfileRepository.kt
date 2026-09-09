package com.palmlens.domain.repository

import com.palmlens.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    /** Cold flow — collect with `stateIn`/`collectAsStateWithLifecycle`, don't block on it. */
    val profile: Flow<UserProfile>

    suspend fun update(transform: (UserProfile) -> UserProfile)

    /** Wipe the profile (Settings → "Delete my data"). */
    suspend fun clear()
}
