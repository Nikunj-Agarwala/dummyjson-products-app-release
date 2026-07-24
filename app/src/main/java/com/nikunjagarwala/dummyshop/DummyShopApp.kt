package com.nikunjagarwala.dummyshop

import android.app.Application
import androidx.room.Room
import com.nikunjagarwala.dummyshop.data.connectivity.ConnectivityObserver
import com.nikunjagarwala.dummyshop.data.local.AppDatabase
import com.nikunjagarwala.dummyshop.data.remote.ProductApi
import com.nikunjagarwala.dummyshop.data.repository.ProductRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class DummyShopApp : Application() {

    lateinit var repository: ProductRepository
        private set

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()

        connectivityObserver = ConnectivityObserver(this)

        val database = Room.databaseBuilder(this, AppDatabase::class.java, "dummyshop.db")
            .fallbackToDestructiveMigration()
            .build()

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(ProductApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val api = retrofit.create(ProductApi::class.java)
        repository = ProductRepository(api, database)
    }
}
