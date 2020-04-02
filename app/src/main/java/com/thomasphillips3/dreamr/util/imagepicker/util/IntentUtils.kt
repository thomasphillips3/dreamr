package com.thomasphillips3.dreamr.util.imagepicker.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.thomasphillips3.dreamr.R
import java.io.File

fun getGalleryIntent(context: Context): Intent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        var intent = getGalleryDocumentIntent()
        if (intent.resolveActivity(context.packageManager) == null) {
            intent = getGalleryPickIntent()
        }
        intent
    } else {
        getGalleryPickIntent()
    }
}

private fun getGalleryDocumentIntent(): Intent {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    intent.type = "image/*"
    return intent
}

private fun getGalleryPickIntent(): Intent {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    return intent
}

fun getCameraIntent(context: Context, file: File): Intent? {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val authority = context.packageName + context.getString(R.string.image_picker_provider_authority_suffix)
        val photoUri = FileProvider.getUriForFile(context, authority, file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
    } else {
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file))
    }
    return intent
}

fun isCameraHardwareAvailable(context: Context): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}