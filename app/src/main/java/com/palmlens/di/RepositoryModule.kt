package com.palmlens.di

import com.palmlens.data.datastore.DataStoreProfileRepository
import com.palmlens.data.repository.HoroscopeRepositoryImpl
import com.palmlens.data.repository.LoveRepositoryImpl
import com.palmlens.data.repository.PalmRepositoryImpl
import com.palmlens.data.repository.ScanQuotaRepositoryImpl
import com.palmlens.data.repository.TarotRepositoryImpl
import com.palmlens.domain.repository.HoroscopeRepository
import com.palmlens.domain.repository.LoveRepository
import com.palmlens.domain.repository.PalmRepository
import com.palmlens.domain.repository.ProfileRepository
import com.palmlens.domain.repository.ScanQuotaRepository
import com.palmlens.domain.repository.TarotRepository
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
    abstract fun bindScanQuotaRepository(impl: ScanQuotaRepositoryImpl): ScanQuotaRepository
}
