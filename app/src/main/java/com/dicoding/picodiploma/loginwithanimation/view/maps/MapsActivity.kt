package com.dicoding.picodiploma.loginwithanimation.view.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityMapsBinding
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.welcome.WelcomeActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        mapsViewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        mapsViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                mapsViewModel.getStoriesWithLocation(user.token)
            }
        }

        mapsViewModel.storyListWithLocation.observe(this) {
            when (it) {
                is Result.Loading -> showLoading(true)
                is Result.Error -> showLoading(false)
                is Result.Success -> {
                    showLoading(false)
                    val boundsBuilder = LatLngBounds.Builder()
                    it.data.forEach { data ->
                        data.lat?.let { lat ->
                            data.lon?.let { lon ->
                                val latLng = LatLng(lat, lon)
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(data.name)
                                        .snippet(data.description)
                                )
                                boundsBuilder.include(latLng)
                            }
                        }
                    }
                    val indonesiaBounds = LatLngBounds(
                        LatLng(-11.004814, 95.251746),
                        LatLng(6.274168, 141.019454)
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(indonesiaBounds, 5))
                }
            }
        }

        if (isLocationEnabled()) {
            getMyLocation()
        } else {
            showLocationDisabledDialog()
        }

        setGtaMapStyle()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getMyLocation()
        } else {
            showLocationDisabledDialog()
        }
    }

    private fun isLocationEnabled(): Boolean = (getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

    private fun showLocationDisabledDialog() {
        AlertDialog.Builder(this)
            .setMessage("Lokasi nonaktif. Aktifkan lokasi sekarang?")
            .setPositiveButton("Ya") { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Tidak") { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) mMap.isMyLocationEnabled = true
        else requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_option, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun setGtaMapStyle() {
        try {
            if (!mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}
