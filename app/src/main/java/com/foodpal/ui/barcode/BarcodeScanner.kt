package com.foodpal.ui.barcode

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.foodpal.MainActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Compose helper that launches the ZXing barcode scanner via Activity Result API.
 *
 * Handles CAMERA permission request, scan intent, and the scanned-barcode
 * (or "user cancelled" / "permission denied") flow.
 *
 * Usage:
 *   var showScanner by remember { mutableStateOf(false) }
 *   BarcodeScanner(
 *       active = showScanner,
 *       onResult = { barcode -> showScanner = false; lookupProduct(barcode) },
 *       onCancel = { showScanner = false },
 *   )
 */
@Composable
fun BarcodeScanner(
    active: Boolean,
    onResult: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val mainActivity = context as? MainActivity

    var permissionDenied by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result: ScanIntentResult ->
        val contents = result.contents
        if (!contents.isNullOrEmpty()) {
            onResult(contents)
        } else {
            onCancel()
        }
    }

    if (!active) return

    if (permissionDenied) {
        AlertDialog(
            onDismissRequest = onCancel,
            title = { Text("Camera permission denied") },
            text = {
                Text(
                    "FoodPal needs camera access to scan barcodes. " +
                        "You can grant it in Android Settings → Apps → FoodPal → Permissions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = { TextButton(onClick = onCancel) { Text("OK") } },
        )
        return
    }

    LaunchedEffect(Unit) {
        val granted = if (mainActivity != null) {
            suspendCancellableCoroutine { cont ->
                mainActivity.requestCameraPermission { result ->
                    if (cont.isActive) cont.resume(result)
                }
            }
        } else {
            false
        }
        if (granted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.EAN_13, ScanOptions.EAN_8, ScanOptions.UPC_A, ScanOptions.UPC_E)
                setPrompt("Point the camera at a product barcode")
                setBeepEnabled(true)
                setOrientationLocked(false)
            }
            scanLauncher.launch(options)
        } else {
            permissionDenied = true
        }
    }
}
