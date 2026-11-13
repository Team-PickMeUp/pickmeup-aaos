package com.example.pickmeup.di

import com.example.pickmeup.BuildConfig
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.pickup.RemotePickupRepository
import com.example.pickmeup.data.terms.LocalTermsConsentRepository
import com.example.pickmeup.data.terms.TermsConsentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTermsConsentRepository(impl: LocalTermsConsentRepository): TermsConsentRepository

    companion object {
        /** 서버 준비 전에는 가짜 저장소 사용 (app/build.gradle.kts 의 USE_FAKE_API 참고) */
        @Provides
        @Singleton
        fun providePickupRepository(
            fake: Provider<FakePickupRepository>,
            remote: Provider<RemotePickupRepository>,
        ): PickupRepository = if (BuildConfig.USE_FAKE_API) fake.get() else remote.get()
    }
}
