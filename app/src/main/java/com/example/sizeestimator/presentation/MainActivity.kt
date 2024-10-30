package com.example.sizeestimator.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sizeestimator.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


/**
 * This code is taken substantially from the Google Codelab on Camera X:
 * https://developer.android.com/codelabs/camerax-getting-started#0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                viewModel.onError("Permission request denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setContentView(viewBinding.root)

        viewBinding.composeView.setContent {
            SizeEstimatorScreen(viewModel)
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
        private const val HIRES_FILENAME = "hires.jpg"
    }
}
