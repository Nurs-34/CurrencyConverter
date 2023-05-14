package kg.surfit.currencyconverter.utils.widgets

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kg.surfit.currencyconverter.ui.CurrencyConverterViewModel

class CurrencyTextWatcher(
    private val editTextFrom: EditText,
    private val editTextTo: EditText,
    private val viewModel: CurrencyConverterViewModel,
    private val from: Boolean
) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (from)
            updateEditText(editTextFrom, editTextTo, viewModel.currencyRate?.let { 1 * it })
        else updateEditText(editTextFrom, editTextTo, viewModel.currencyRate?.let { 1 / it })
    }

    override fun afterTextChanged(s: Editable) {}

    private fun updateEditText(fromEditText: EditText, toEditText: EditText, rate: Double?) {
        val inputString = fromEditText.text.toString()
        if (isValidInput(inputString) && rate != null) {
            val sum: Double = inputString.toDouble() * rate
            val total: String = String.format("%.2f", sum)
            toEditText.setText(total)
        } else if (inputString.isEmpty() && !editTextTo.text.isNullOrEmpty()) {
            toEditText.setText("")
        }
    }

    private fun isValidInput(inputString: String): Boolean {
        return inputString.isNotEmpty() && inputString.matches("^[0-9]+(\\.[0-9]{1,2})?\$".toRegex())
    }

}