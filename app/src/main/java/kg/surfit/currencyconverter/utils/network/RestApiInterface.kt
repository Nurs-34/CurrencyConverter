package kg.surfit.currencyconverter.utils.network

import kg.surfit.currencyconverter.model.CurrencyResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface RestApiInterface {

    @GET("/fetch-multi")
    suspend fun fetchExchangeRates(
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<CurrencyResponse>

    companion object {
        private const val BASE_URL = "https://api.fastforex.io"
        private const val API_KEY = "a8951066d9-e1ac898b75-rtlqix"

        private val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val original = chain.request()
                val originalHttpUrl = original.url

                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("api_key", API_KEY)
                    .build()

                val requestBuilder = original.newBuilder()
                    .url(url)

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()

        operator fun invoke(): RestApiInterface {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RestApiInterface::class.java)
        }
    }
}