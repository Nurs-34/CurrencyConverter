package kg.surfit.currencyconverter.utils.network

import android.util.Log
import kg.surfit.currencyconverter.ui.CurrencyConverterViewModel
import kg.surfit.currencyconverter.ui.MainAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class CurrencyFetcher(private val viewModel: CurrencyConverterViewModel) {

    suspend fun fetchCurrencies(fromCurrency: String, toCurrency: String) {
        try {
            var rate: Double? = null

            rate = when {
                fromCurrency == "kgs" && (toCurrency == "usd" || toCurrency == "eur") -> fetchRateFromNbkr(
                    toCurrency
                )

                fromCurrency == "eur" && toCurrency == "kgs" -> fetchRateFromYahoo(
                    fromCurrency,
                    toCurrency
                )

                fromCurrency == "kzt" && (toCurrency == "usd" || toCurrency == "kgs") -> fetchRateFromNationalBank(
                    toCurrency
                )

                fromCurrency == "rub" && (toCurrency == "usd" || toCurrency == "eur") -> fetchRateFromCbr(
                    toCurrency
                )

                fromCurrency != toCurrency -> fetchRateFromBankiros(fromCurrency, toCurrency)
                fromCurrency.equals(toCurrency) -> 1.00
                else -> null
            }

            if (rate != null) {
                viewModel.currencyRate = rate
            } else {
                viewModel.mainActionFlow.emit(MainAction.ShowScrapErrorToast)
            }
        } catch (e: Exception) {
            viewModel.mainActionFlow.emit(MainAction.ShowErrorToast)
            Log.e("CurrencyConverterVM", "Error scraping exchange rates", e)
        }
    }

    private suspend fun fetchRateFromNbkr(toCurrency: String): Double? {
        val url = "https://www.nbkr.kg/XML/daily.xml"
        val doc = getDocument(url)

        val node = doc.select("Currency[ISOCode=${toCurrency.uppercase()}]")
        val valueElement = node.select("Value").text()
        return getRateFromValueElement(valueElement)
    }

    private suspend fun fetchRateFromYahoo(fromCurrency: String, toCurrency: String): Double? {
        val url = "https://finance.yahoo.com/quote/$fromCurrency$toCurrency=X/"
        val doc = getDocument(url)

        val rateElement =
            doc.selectFirst("fin-streamer[data-field='regularMarketPrice'][data-test='qsp-price']")
        val rateString = rateElement?.attr("value")
        return rateString?.toDoubleOrNull()
    }

    private suspend fun fetchRateFromNationalBank(toCurrency: String): Double? {
        val url = "https://nationalbank.kz/rss/rates_all.xml"
        val doc = getDocument(url)

        val node = doc.select("item title:contains(${toCurrency.uppercase()})")
        val valueElement = node.parents().select("description").text()
        return getRateFromValueElement(valueElement.substringBefore(" "))
    }

    private suspend fun fetchRateFromCbr(toCurrency: String): Double? {
        val url = "https://cbr.ru/scripts/xml_daily.asp"
        val doc = getDocument(url)

        val node = doc.select("Valute")
        val valueElements = node.firstOrNull {
            it.selectFirst("CharCode")?.text() == toCurrency.uppercase()
        }
        val valueElement = valueElements?.selectFirst("Value")?.text()
        return getRateFromValueElement(valueElement!!.replace(",", "."))
    }

    // The website fetches exchange rates from the Central Bank of the Russian Federation (CBR), which provides almost all the rates. However, some rates are either empty or not filled, and some rates round floating-point numbers too much.
    // For example, the exchange rate from Russian Ruble to US Dollar is 0.01239, but they round it to 0.01.
    // That's why I obtained some rates from central or national banks of the Kyrgyz Republic, Russian Federation, and Republic of Kazakhstan.
    private suspend fun fetchRateFromBankiros(fromCurrency: String, toCurrency: String): Double? {
        val url = "https://bankiros.ru/convert/$fromCurrency-$toCurrency"
        val doc = getDocument(url)
        val rateString = doc.selectFirst("meta[name=description]")?.attr("content")
        val rateRegex = """\b(\d+(.\d+)?)\b""".toRegex()
        val rateMatch = rateRegex.find(rateString ?: "")
        return rateMatch?.value?.toDoubleOrNull()
    }

    private suspend fun getDocument(url: String) =
        withContext(Dispatchers.IO) { Jsoup.connect(url).get() }

    private fun getRateFromValueElement(valueElement: String): Double? {
        val rateString = valueElement.replace(",", ".").toDoubleOrNull()
        return 1 / rateString!!
    }
}
