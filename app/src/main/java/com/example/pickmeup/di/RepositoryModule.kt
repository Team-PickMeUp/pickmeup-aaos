package com.example.pickmeup.di

import com.example.pickmeup.BuildConfig
import com.example.pickmeup.data.message.FakePickupMessageRepository
import com.example.pickmeup.data.message.PickupMessageRepository
import com.example.pickmeup.data.pickup.FakePickupRepository
import com.example.pickmeup.data.pickup.PickupRepository
import com.example.pickmeup.data.pickup.RemotePickupRepository
import com.example.pickmeup.data.target.LocalPickupTargetRepository
import com.example.pickmeup.data.target.PickupTargetRepository
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

    @Binds
    abstract fun bindPickupTargetRepository(impl: LocalPickupTargetRepository): PickupTargetRepository

    /** TODO: 서버 연동 시 실시간 수신 구현으로 교체 (지금은 2초 간격 가짜 메시지) */
    @Binds
    abstract fun bindPickupMessageRepository(impl: FakePickupMessageRepository): PickupMessageRepository

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
