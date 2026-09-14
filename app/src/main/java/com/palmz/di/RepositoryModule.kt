package com.palmz.di

import com.palmz.data.auth.FirebaseAuthRepository
import com.palmz.data.datastore.DataStoreProfileRepository
import com.palmz.data.datastore.DataStoreThemeRepository
import com.palmz.data.repository.FirestoreScanQuotaRepository
import com.palmz.data.repository.HoroscopeRepositoryImpl
import com.palmz.data.repository.LoveRepositoryImpl
import com.palmz.data.repository.PalmRepositoryImpl
import com.palmz.data.repository.TarotRepositoryImpl
import com.palmz.domain.repository.AuthRepository
import com.palmz.domain.repository.HoroscopeRepository
import com.palmz.domain.repository.LoveRepository
import com.palmz.domain.repository.PalmRepository
import com.palmz.domain.repository.ProfileRepository
import com.palmz.domain.repository.ScanQuotaRepository
import com.palmz.domain.repository.TarotRepository
import com.palmz.domain.repository.ThemePreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Interface → implementation aliases. `@Binds` (not `@Provides`) because every impl is
 * `@Inject constructor`-able and `@Singleton`-scoped on the class — Dagger generates no
 * factory for these. Builder-constructed things (Room, DataStore, Json, OkHttp) are
 * `@Provides` in their own `object` modules; `ContentGenerator` is picked at runtime in
 * [ContentGeneratorModule].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindProfileRepository(impl: DataStoreProfileRepository): ProfileRepository

    @Binds
    abstract fun bindPalmRepository(impl: PalmRepositoryImpl): PalmRepository

    @Binds
    abstract fun bindHoroscopeRepository(impl: HoroscopeRepositoryImpl): HoroscopeRepository

    @Binds
    abstract fun bindLoveRepository(impl: LoveRepositoryImpl): LoveRepository

    @Binds
    abstract fun bindTarotRepository(impl: TarotRepositoryImpl): TarotRepository

    @Binds
    abstract fun bindScanQuotaRepository(impl: FirestoreScanQuotaRepository): ScanQuotaRepository

    @Binds
    abstract fun bindThemePreferenceRepository(impl: DataStoreThemeRepository): ThemePreferenceRepository

    @Binds
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}
