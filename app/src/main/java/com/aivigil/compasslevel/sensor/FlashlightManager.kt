package com.aivigil.compasslevel.sensor

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashlightManager(context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private var cameraId: String? = null

    init {
        try {
            val cameraIds = cameraManager?.cameraIdList
            cameraId = cameraIds?.firstOrNull()
        } catch (e: Exception) {
            // Flashlight hardware not available
        }
    }

    fun toggleFlashlight() {
        val cid = cameraId ?: return
        val newState = !_isTorchOn.value
        try {
            cameraManager?.setTorchMode(cid, newState)
            _isTorchOn.value = newState
        } catch (e: Exception) {
            _isTorchOn.value = false
        }
    }

    fun turnOff() {
        val cid = cameraId ?: return
        if (_isTorchOn.value) {
            try {
                cameraManager?.setTorchMode(cid, false)
            } catch (e: Exception) {
                // Ignore
            }
            _isTorchOn.value = false
        }
    }
}
