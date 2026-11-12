package com.foodpal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.foodpal.ui.FoodPalRoot

class MainActivity : ComponentActivity() {

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraPermissionResult?.invoke(granted)
        cameraPermissionResult = null
    }

    @Volatile
    private var cameraPermissionResult: ((Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FoodPalRoot() }
    }

    fun requestCameraPermission(onResult: (Boolean) -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onResult(true)
        } else {
            cameraPermissionResult = onResult
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }
}
