package com.dicoding.picodiploma.loginwithanimation.view.setting

import android.app.LocaleManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivitySettingBinding
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory

@Suppress("DEPRECATION")
class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    private val settingViewModel: SettingViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val languageArray = resources.getStringArray(R.array.language_array)
        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, languageArray)

        settingViewModel.getLocale().observe(this) { selectedLanguage ->
            val position = when (selectedLanguage) {
                "ja" -> 4
                "ko" -> 3
                "jv" -> 2
                "in" -> 1
                else -> 0
            }
            binding.spLanguage.setSelection(position)
        }

        binding.spLanguage.apply {
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedLocale = when (position) {
                        4 -> "ja"
                        3 -> "ko"
                        2 -> "jv"
                        1 -> "in"
                        else -> "en"
                    }
                    setLocale(selectedLocale)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            adapter = arrayAdapter
        }
    }

    private fun setLocale(localeCode: String) {
        settingViewModel.saveLocale(localeCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(localeCode)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

}