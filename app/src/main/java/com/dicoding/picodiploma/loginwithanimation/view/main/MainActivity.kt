package com.dicoding.picodiploma.loginwithanimation.view.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.adapter.ListAdapter
import com.dicoding.picodiploma.loginwithanimation.adapter.LoadingStateAdapter
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMainBinding
import com.dicoding.picodiploma.loginwithanimation.view.maps.MapsActivity
import com.dicoding.picodiploma.loginwithanimation.view.setting.SettingActivity
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import com.dicoding.picodiploma.loginwithanimation.view.upload.UploadActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        val message = if (isGranted) "Permission request granted" else "Permission request denied"
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        showLoading(true)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        binding.rvStoryList.layoutManager = LinearLayoutManager(this)

        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        getData()
        setOptionMenu()
        setFAB()
    }

    private fun getData() {
        val adapter = ListAdapter()
        binding.rvStoryList.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        mainViewModel.storyList.observe(this) {
            adapter.submitData(lifecycle, it)
        }
        showLoading(false)
    }

    private fun setFAB() {
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setOptionMenu() {
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_logout -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.logout_dialog_title))
                        .setMessage(getString(R.string.logout_dialog_message))
                        .setPositiveButton(getString(R.string.yes)) { _, _ -> mainViewModel.logout() }
                        .create()
                        .show()
                    true
                }
                R.id.menu_setting -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                R.id.action_maps -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvStoryList.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}