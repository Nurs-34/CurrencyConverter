package kg.surfit.currencyconverter.model

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    @SerializedName("base") val result: String?,
    @SerializedName("results") val currencies: Map<String, Double>?,
    @SerializedName("updated") val updTime: String?,
    @SerializedName("ms") val ms: Short?
)
