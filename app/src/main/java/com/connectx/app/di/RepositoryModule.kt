package com.connectx.app.di

import com.connectx.app.data.repository.SupabaseAuthRepository
import com.connectx.app.data.repository.SupabaseProfileRepository
import com.connectx.app.domain.repository.AuthRepository
import com.connectx.app.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: SupabaseProfileRepository): ProfileRepository
}
