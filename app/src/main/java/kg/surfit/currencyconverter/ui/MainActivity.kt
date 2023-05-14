package kg.surfit.currencyconverter.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kg.surfit.currencyconverter.R
import kg.surfit.currencyconverter.databinding.ActivityMainBinding
import kg.surfit.currencyconverter.utils.widgets.CurrencyTextWatcher
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: CurrencyConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CurrencyConverterViewModel::class.java]

        supportActionBar?.title = viewModel.setSupportBarTitle()

        binding.imageViewFrom.setImageResource(R.drawable.ic_flag_us)
        binding.imageViewTo.setImageResource(R.drawable.ic_flag_kg)

        binding.buttonFrom.setOnClickListener {
            viewModel.showCurrencyDialog(1, this, binding.buttonFrom)
            viewModel.clearEditTexts(binding.editTextFrom, binding.editTextTo)
        }

        binding.buttonTo.setOnClickListener {
            viewModel.showCurrencyDialog(2, this, binding.buttonTo)
            viewModel.clearEditTexts(binding.editTextFrom, binding.editTextTo)
        }

        val textWatcherFrom =
            CurrencyTextWatcher(binding.editTextFrom, binding.editTextTo, viewModel, true)
        val textWatcherTo =
            CurrencyTextWatcher(binding.editTextTo, binding.editTextFrom, viewModel, false)

        binding.editTextFrom.addTextChangedListener(textWatcherFrom)
        binding.editTextTo.addTextChangedListener(textWatcherTo)

        lifecycleScope.launchWhenStarted {
            viewModel.fromCurrency.collect { currency ->
                viewModel.setCurrencyImageResource(currency, binding.imageViewFrom)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.toCurrency.collect { currency ->
                viewModel.setCurrencyImageResource(currency, binding.imageViewTo)
            }
        }

        viewModel.mainActionFlow.onEach { action ->
            when (action) {
                is MainAction.ShowApiErrorToast -> {
                    Toast.makeText(
                        applicationContext, "Ваш api key просрочен", Toast.LENGTH_SHORT
                    ).show()
                }

                is MainAction.ShowErrorToast -> {
                    Toast.makeText(
                        applicationContext, "Что-то пошло не так)))", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.launchIn(lifecycleScope)
    }
}