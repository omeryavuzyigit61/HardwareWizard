package com.panda.myapplication.data.di

import com.panda.myapplication.data.local.HardwareDatabase
import com.panda.myapplication.data.local.HardwareLocalDataSource
import com.panda.myapplication.data.remote.HardwareRemoteDataSource
import com.panda.myapplication.data.repository.HardwareRepositoryImpl
import com.panda.myapplication.data.repository.ActiveBuildRepositoryImpl
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.usecase.CalculatePsuRequirementUseCase
import com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase
import com.panda.myapplication.domain.usecase.GetCompatibleHardwareForSlotUseCase
import com.panda.myapplication.domain.usecase.GetHardwareDetailUseCase
import com.panda.myapplication.domain.usecase.GetHardwareListUseCase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val dataModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }
    }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                level = LogLevel.NONE
            }
        }
    }

    single<CoroutineDispatcher> { Dispatchers.IO }

    single { get<HardwareDatabase>().hardwareDao() }

    single { HardwareLocalDataSource(hardwareDao = get(), ioDispatcher = get(), json = get()) }
    single { HardwareRemoteDataSource(httpClient = get(), ioDispatcher = get(), json = get()) }

    single<HardwareRepository> {
        HardwareRepositoryImpl(
            hardwareDao = get(),
            localDataSource = get(),
            remoteDataSource = get(),
            ioDispatcher = get(),
        )
    }

    single<ActiveBuildRepository> { ActiveBuildRepositoryImpl() }
    single<com.panda.myapplication.domain.repository.VsArenaRepository> {
        com.panda.myapplication.data.repository.VsArenaRepositoryImpl()
    }
    single<com.panda.myapplication.domain.repository.CommunityPriceRepository> {
        com.panda.myapplication.data.repository.CommunityPriceRepositoryImpl(
            httpClient = get(),
            json = get(),
            ioDispatcher = get(),
        )
    }

    factory { GetHardwareListUseCase(repository = get()) }
    factory { GetHardwareDetailUseCase(repository = get()) }
    factory { CalculatePsuRequirementUseCase() }
    factory { CheckBuildCompatibilityUseCase(calculatePsuRequirementUseCase = get()) }
    factory { GetCompatibleHardwareForSlotUseCase() }
}
