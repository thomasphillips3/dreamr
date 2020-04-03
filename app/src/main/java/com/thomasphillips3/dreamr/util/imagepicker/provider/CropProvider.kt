package com.thomasphillips3.dreamr.util.imagepicker.provider

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.ImagePicker
import com.thomasphillips3.dreamr.util.imagepicker.ImagePickerActivity
import com.thomasphillips3.dreamr.util.imagepicker.util.getImageFile
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.IOException

class CropProvider(activity: ImagePickerActivity) : BaseProvider(activity) {
    companion object {
        private val TAG = CropProvider::class.java.simpleName
        private const val STATE_CROP_FILE = "state.crop_file"
    }

    private val maxWidth: Int
    private val maxHeight: Int

    private val crop: Boolean
    private val cropAspectX: Float
    private val cropAspectY: Float
    private var cropImageFile: File? = null

    init {
        val bundle = activity.intent.extras!!
        maxWidth = bundle.getInt(ImagePicker.EXTRA_MAX_WIDTH, 0)
        maxHeight = bundle.getInt(ImagePicker.EXTRA_MAX_HEIGHT, 0)

        crop = bundle.getBoolean(ImagePicker.EXTRA_CROP, false)
        cropAspectX = bundle.getFloat(ImagePicker.EXTRA_CROP_X, 0f)
        cropAspectY = bundle.getFloat(ImagePicker.EXTRA_CROP_Y, 0f)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(STATE_CROP_FILE, cropImageFile)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        cropImageFile = savedInstanceState?.getSerializable(STATE_CROP_FILE) as File?
    }

    fun isCropEnabled() = crop

    fun startIntent(file: File) {
        cropImage(file)
    }

    @Throws(IOException::class)
    private fun cropImage(file: File) {
        cropImageFile = getImageFile()

        if (cropImageFile == null || !cropImageFile!!.exists()) {
            Log.e(TAG, "Failed to create file for cropping")
            setError(R.string.error_failed_to_crop_image)
            return
        }

        val options = UCrop.Options()
        val uCrop = UCrop.of(Uri.fromFile(file), Uri.fromFile(cropImageFile))
            .withOptions(options)

        if (cropAspectX > 0 && cropAspectY > 0) {
            uCrop.withAspectRatio(cropAspectX, cropAspectY)
        }

        if (maxWidth > 0 && maxHeight > 0) {
            uCrop.withMaxResultSize(maxWidth, maxHeight)
        }

        try {
            uCrop.start(activity, UCrop.REQUEST_CROP)
        } catch (e: ActivityNotFoundException) {
            setError("uCrop not specified in manifest." +
                    "Add UCropActivity in Manifest" +
                    "<activity\n" +
                    "    android:name=\"com.yalantis.ucrop.UCropActivity\"\n" +
                    "    android:screenOrientation=\"portrait\"\n" +
                    "    android:theme=\"@style/Theme.AppCompat.Light.NoActionBar\"/>"
            )
            e.printStackTrace()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                handleResult(cropImageFile)
            } else {
                setResultCancel()
            }
        }
    }

    private fun handleResult(file: File?) {
        if (file != null) {
            activity.setCropImage(file)
        } else {
            setError(R.string.error_failed_to_crop_image)
        }
    }

    override fun onFailure() {
        cropImageFile?.delete()
    }
}