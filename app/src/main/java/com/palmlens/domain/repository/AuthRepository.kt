package com.palmlens.domain.repository

import kotlinx.coroutines.flow.Flow

/** Email/password account gate — also the identity free scans are tied to (spec: anti-abuse). */
interface AuthRepository {
    /** Null if signed out. Synchronous — the backing SDK caches this from disk. */
    val currentUserId: String?

    /** Emits the signed-in user's id (or null) on every sign-in/sign-out. */
    val userIdFlow: Flow<String?>

    suspend fun signIn(email: String, password: String): Result<Unit>

    suspend fun signUp(email: String, password: String): Result<Unit>

    fun signOut()
}
