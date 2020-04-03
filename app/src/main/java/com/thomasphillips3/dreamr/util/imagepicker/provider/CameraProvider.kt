package com.thomasphillips3.dreamr.util.imagepicker.provider

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.ActivityCompat.requestPermissions
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.ImagePickerActivity
import com.thomasphillips3.dreamr.util.imagepicker.util.getCameraIntent
import com.thomasphillips3.dreamr.util.imagepicker.util.getImageFile
import com.thomasphillips3.dreamr.util.imagepicker.util.isPermissionGranted
import com.thomasphillips3.dreamr.util.imagepicker.util.isPermissionInManifest
import java.io.File

class CameraProvider(activity: ImagePickerActivity) : BaseProvider(activity) {
    companion object {
        private const val STATE_CAMERA_FILE = "state.camera_file"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private val REQUIRED_PERMISSIONS_EXTENDED = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )

        private const val CAMERA_INTENT_REQ_CODE = 4281
        private const val PERMISSION_INTENT_REQ_CODE = 4282
    }

    private var cameraFile: File? = null
    private val askCameraPermission =
        isPermissionInManifest(this, Manifest.permission.CAMERA)

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(STATE_CAMERA_FILE, cameraFile)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        cameraFile = savedInstanceState?.getSerializable(STATE_CAMERA_FILE) as File?
    }

    fun startIntent() {
        checkPermission()
    }

    private fun checkPermission() {
        if (isPermissionGranted(this)) {
            startCameraIntent()
        } else {
            requestPermission()
        }
    }

    private fun startCameraIntent() {
        val file = getImageFile()
        cameraFile = file

        if (file != null && file.exists()) {
            val cameraIntent = getCameraIntent(this, file)
            activity.startActivityForResult(cameraIntent, CAMERA_INTENT_REQ_CODE)
        } else {
            setError(R.string.error_failed_to_create_camera_image_file)
        }
    }

    fun onRequestPermissionsResult(requestCode: Int) {
        if (requestCode == PERMISSION_INTENT_REQ_CODE) {
            if (isPermissionGranted(this)) {
                startCameraIntent()
            } else {
                val errorRes = if (askCameraPermission) {
                    R.string.permission_camera_extended_denied
                } else {
                    R.string.permission_camera_denied
                }
                setError(getString(errorRes))
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_INTENT_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(data)
            } else {
                setResultCancel()
            }
        }
    }

    private fun handleResult(data: Intent?) {
        activity.setImage(cameraFile!!)
    }

    override fun onFailure() {
        cameraFile?.delete()
    }

    private fun requestPermission() {
        if (askCameraPermission) {
            requestPermissions(activity, REQUIRED_PERMISSIONS_EXTENDED, PERMISSION_INTENT_REQ_CODE)
        } else {
            requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_INTENT_REQ_CODE)
        }
    }

    private fun isPermissionGranted(context: Context): Boolean {
        if (askCameraPermission && isPermissionGranted(context, REQUIRED_PERMISSIONS_EXTENDED)) {
            return true
        } else if (!askCameraPermission && isPermissionGranted(context, REQUIRED_PERMISSIONS)) {
            return true
        }
        return false
    }
}
