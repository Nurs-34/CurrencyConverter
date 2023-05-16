package kg.surfit.currencyconverter.ui

import android.app.Activity
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kg.surfit.currencyconverter.R
import kg.surfit.currencyconverter.repository.CurrencyRepository
import kg.surfit.currencyconverter.utils.network.CurrencyFetcher
import kg.surfit.currencyconverter.utils.network.RestApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class CurrencyConverterViewModel : ViewModel() {
    private val repository: CurrencyRepository = CurrencyRepository(RestApiInterface.invoke())
    val mainActionFlow: MutableSharedFlow<MainAction> = MutableSharedFlow()

    var currencyRate: Double? = null
    var defaultFromCurrency: String = "usd"
    var defaultToCurrency: String = "kgs"

    val fromCurrency = MutableStateFlow(defaultFromCurrency)
    val toCurrency = MutableStateFlow(defaultToCurrency)

    private val currencyFetcher = CurrencyFetcher(this)

    init {
        fetchCurrencies(defaultFromCurrency, defaultToCurrency)
    }


    fun fetchCurrencies(fromCurrency: String, toCurrency: String) {
        viewModelScope.launch {
            currencyFetcher.fetchCurrencies(fromCurrency, toCurrency)
        }
    }

    // here used trial api from ApiForex
    fun fetchCurrenciesApiForex(fromCurrency: String, toCurrency: String) {
        viewModelScope.launch {
            try {
                val response = repository.fetchExchangeRates(fromCurrency, toCurrency)
                if (response.isSuccessful) {
                    currencyRate =
                        response
                            .body()?.currencies?.get(
                                toCurrency
                            )
                } else {
                    mainActionFlow.emit(MainAction.ShowApiErrorToast)
                }
            } catch (e: Exception) {
                mainActionFlow.emit(MainAction.ShowErrorToast)
                Log.e("CurrencyConverterVM", "Error fetching exchange rates", e)
            }
        }
    }

    fun showCurrencyDialog(currency: Int, activity: Activity, button: Button) {
        val currencies = arrayOf("USD", "EUR", "RUB", "KGS", "KZT")

        AlertDialog.Builder(activity).apply {
            setTitle("Выберите валюту")
            setItems(currencies) { _, which ->
                val selectedCurrency = currencies[which]
                when (currency) {
                    1 -> {
                        fromCurrency.value = selectedCurrency.lowercase()
                        button.text = fromCurrency.value
                    }

                    2 -> {
                        toCurrency.value = selectedCurrency.lowercase()
                        button.text = toCurrency.value
                    }
                }
                fetchCurrencies(fromCurrency.value, toCurrency.value)
            }
            show()
        }
    }

    fun setCurrencyImageResource(currency: String, imageView: ImageView) {
        val imageResource = when (currency) {
            "usd" -> R.drawable.ic_flag_us
            "eur" -> R.drawable.ic_flag_eu
            "rub" -> R.drawable.ic_flag_ru
            "kgs" -> R.drawable.ic_flag_kg
            "kzt" -> R.drawable.ic_flag_kz
            else -> null
        }
        imageResource?.let { imageView.setImageResource(it) }
    }

    fun clearEditTexts(fromEditText: EditText, toEditText: EditText) {
        fromEditText.setText("")
        toEditText.setText("")
//        fromEditText.clearFocus()
//        toEditText.clearFocus()
    }

    fun setSupportBarTitle(): SpannableStringBuilder {
        val title = "Конвертер валют"
        val spannableText = SpannableStringBuilder(title)
        spannableText.setSpan(
            StyleSpan(Typeface.BOLD), 0, spannableText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableText
    }
}

sealed class MainAction {
    object ShowErrorToast : MainAction()
    object ShowScrapErrorToast : MainAction()
    object ShowApiErrorToast : MainAction()
}

