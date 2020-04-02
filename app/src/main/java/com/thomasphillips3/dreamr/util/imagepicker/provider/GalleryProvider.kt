package com.thomasphillips3.dreamr.util.imagepicker.provider

import android.Manifest
import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat.requestPermissions
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.ImagePickerActivity
import com.thomasphillips3.dreamr.util.imagepicker.util.getGalleryIntent
import com.thomasphillips3.dreamr.util.imagepicker.util.getRealPath
import com.thomasphillips3.dreamr.util.imagepicker.util.isPermissionGranted
import java.io.File

class GalleryProvider(activity: ImagePickerActivity): BaseProvider(activity) {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        private const val GALLERY_INTENT_REQ_CODE = 4261
        private const val PERMISSION_INTENT_REQ_CODE = 4262
    }

    fun startIntent() {
        checkPermission()
    }

    private fun checkPermission() {
        if (!isPermissionGranted(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(activity, REQUIRED_PERMISSIONS, PERMISSION_INTENT_REQ_CODE)
        } else {
            startGalleryIntent()
        }
    }

    private fun startGalleryIntent() {
        val galleryIntent = getGalleryIntent(activity)
        activity.startActivityForResult(galleryIntent, GALLERY_INTENT_REQ_CODE)
    }

    fun onRequestPermissionResult(requestCode: Int) {
        if (requestCode == PERMISSION_INTENT_REQ_CODE) {
            if (isPermissionGranted(this, REQUIRED_PERMISSIONS)) {
                startGalleryIntent()
            } else {
                setError(getString(R.string.permission_gallery_denied))
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GALLERY_INTENT_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(data)
            } else {
                setResultCancel()
            }
        }
    }

    private fun handleResult(data: Intent?) {
        val uri = data?.data
        if(uri != null) {
             val filePath: String? = getRealPath(activity, uri)
            if (!filePath.isNullOrEmpty()) {
                activity.setImage(File(filePath))
            } else {
                setError(R.string.error_failed_pick_gallery_image)
            }
        } else {
            setError(R.string.error_failed_pick_gallery_image)
        }
    }
}