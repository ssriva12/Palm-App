package com.palmlens.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.palmlens.domain.repository.AuthRepository
import com.palmlens.domain.repository.DEFAULT_FREE_SCAN_CAP
import com.palmlens.domain.repository.ScanQuotaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Free scans tied to the signed-in account (Firestore `users/{uid}.scansUsed`), not local
 * storage — unlike the old Room-backed counter, clearing app data or reinstalling no longer
 * resets it: signing back in with the same email resolves to the same server-side document.
 */
@Singleton
class FirestoreScanQuotaRepository @Inject constructor(
    private val authRepository: AuthRepository,
) : ScanQuotaRepository {

    private val firestore = Firebase.firestore

    @OptIn(ExperimentalCoroutinesApi::class)
    override val scansUsed: Flow<Int> = authRepository.userIdFlow.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(0)
        } else {
            callbackFlow {
                val registration = firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        trySend(snapshot?.getLong("scansUsed")?.toInt() ?: 0)
                    }
                awaitClose { registration.remove() }
            }
        }
    }

    // Phase 3: swap for RemoteConfigRepository.freeScanCap.
    override val cap: Flow<Int> = flowOf(DEFAULT_FREE_SCAN_CAP)

    override val remaining: Flow<Int> =
        combine(scansUsed, cap) { used, c -> (c - used).coerceAtLeast(0) }

    override val canScan: Flow<Boolean> = remaining.map { it > 0 }

    override suspend fun recordScan() {
        val uid = authRepository.currentUserId ?: return
        // Not awaited: Firestore applies this to the local cache (and the scansUsed listener
        // above) immediately and syncs to the server in the background, so a slow/offline
        // connection can never hang the scan result behind this bookkeeping write.
        firestore.collection("users").document(uid)
            .set(mapOf("scansUsed" to FieldValue.increment(1)), SetOptions.merge())
            .addOnFailureListener { Log.w("Palmlens", "recordScan failed to sync", it) }
    }
}
