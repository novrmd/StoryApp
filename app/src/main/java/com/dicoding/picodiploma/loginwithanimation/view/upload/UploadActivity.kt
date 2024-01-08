package com.dicoding.picodiploma.loginwithanimation.view.upload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityUploadBinding
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import com.dicoding.picodiploma.loginwithanimation.view.utils.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.utils.getImageUri
import com.dicoding.picodiploma.loginwithanimation.view.utils.reduceFileImage
import com.dicoding.picodiploma.loginwithanimation.view.utils.uriToFile
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding
    private var currentImageUri: Uri? = null
    private lateinit var uploadStoryViewModel: UploadViewModel
    private var currentLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                getMyLastLocation()
            } else if (permissions[Manifest.permission.CAMERA] != true) {
                binding.switchLocation.isChecked = false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.upload_page_title)
            setDisplayHomeAsUpEnabled(true)
        }

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        uploadStoryViewModel = ViewModelProvider(this, factory)[UploadViewModel::class.java]

        uploadStoryViewModel.uploadStoryResponse.observe(this) {
            when (it) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> showSuccessDialog()
                is Result.Error -> showErrorDialog()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }
        binding.tvLat.text = getString(R.string.latitude, null)
        binding.tvLong.text = getString(R.string.longtitute, null)

        binding.switchLocation.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            if (isChecked) {
                handleLocationSwitchOn()
            } else {
                handleLocationSwitchOff()
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.yeay))
            setMessage(getString(R.string.upload_dialog_message))
            setCancelable(false)
            setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            create()
            show()
        }
    }

    private fun showErrorDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.error_title))
            setMessage(getString(R.string.error_message))
            create()
            show()
        }
    }

    private fun handleLocationSwitchOn() {
        if (!isGPSEnabled()) {
            showEnableGPSDialog()
        }
        lifecycleScope.launch {
            getMyLastLocation()
        }
    }

    private fun handleLocationSwitchOff() {
        binding.tvLat.text = getString(R.string.latitude, null)
        binding.tvLong.text = getString(R.string.longtitute, null)
        currentLocation = null
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        }
    }

    private fun startCamera() {
        if (checkPermission(Manifest.permission.CAMERA)) {
            currentImageUri = getImageUri(this)
            launcherIntentCamera.launch(currentImageUri)
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun uploadImage() {
        var token: String
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val description = binding.edtDescription.text.toString()
            showLoading(true)

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )

            uploadStoryViewModel.getSession().observe(this) { user ->
                token = user.token
                uploadStoryViewModel.uploadStory(token, multipartBody, requestBody, currentLocation)
            }

        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showToast(messageResId: Int, duration: Int) {
        Toast.makeText(this, messageResId, duration).show()
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation() {
        val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION

        if (checkPermission(fineLocationPermission) && checkPermission(coarseLocationPermission)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    val latitude = currentLocation?.latitude?.toString()?.slice(1..7)
                    binding.tvLat.text = getString(R.string.latitude, latitude)
                    binding.tvLong.text = getString(R.string.longtitute, latitude)
                } else {
                    showToast(R.string.error_no_location, Toast.LENGTH_SHORT)
                    getNewLocation()
                }
            }
        } else {
            requestPermissionLauncher.launch(arrayOf(fineLocationPermission, coarseLocationPermission))
        }
    }

    private fun getNewLocation() {
        showToast(R.string.get_new_location, Toast.LENGTH_SHORT)
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = TimeUnit.SECONDS.toMillis(1)
            fastestInterval = 0
            numUpdates = 1
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Looper.myLooper()?.let {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, it
                )
            }
        }
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation = locationResult.lastLocation
        }
    }

    private fun isGPSEnabled(): Boolean =
        (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun showEnableGPSDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.gps_tittle))
            .setMessage(getString(R.string.gps_message))
            .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}