package kg.surfit.currencyconverter.repository

import kg.surfit.currencyconverter.utils.network.RestApiInterface
import kg.surfit.currencyconverter.model.CurrencyResponse
import retrofit2.Response

class CurrencyRepository(private val apiService: RestApiInterface) {

    suspend fun fetchExchangeRates(from: String, to: String): Response<CurrencyResponse> {
        return apiService.fetchExchangeRates(from, to)
    }
}