package com.thomasphillips3.dreamr.util.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.constant.ImageProvider
import com.thomasphillips3.dreamr.util.imagepicker.provider.CameraProvider
import com.thomasphillips3.dreamr.util.imagepicker.provider.CompressionProvider
import com.thomasphillips3.dreamr.util.imagepicker.provider.CropProvider
import com.thomasphillips3.dreamr.util.imagepicker.provider.GalleryProvider
import java.io.File

class ImagePickerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "image_picker"
        private const val STATE_IMAGE_FILE = "state.image_file"

        internal fun getCanceledIntent(context: Context): Intent {
            val intent = Intent()
            val message = context.getString(R.string.error_task_canceled)
            intent.putExtra(ImagePicker.EXTRA_ERROR, message)
            return intent
        }
    }

    private var galleryProvider: GalleryProvider? = null
    private var cameraProvider: CameraProvider? = null
    private lateinit var cropProvider: CropProvider
    private lateinit var compressionProvider: CompressionProvider
    private var imageFile: File? = null
    private var cropFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstanceState(savedInstanceState)
        loadBundle(savedInstanceState)
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            imageFile = savedInstanceState.getSerializable(STATE_IMAGE_FILE) as File?
        }
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(STATE_IMAGE_FILE, imageFile)
        cameraProvider?.onSaveInstanceState(outState)
        cropProvider.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun loadBundle(savedInstanceState: Bundle?) {
        cropProvider = CropProvider(this)
        cropProvider.onRestoreInstanceState(savedInstanceState)

        compressionProvider = CompressionProvider(this)

        val provider: ImageProvider? =
            intent?.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PROVIDER) as ImageProvider?

        when (provider) {
            ImageProvider.GALLERY -> {
                galleryProvider = GalleryProvider(this)
                savedInstanceState ?: galleryProvider?.startIntent()
            }

            ImageProvider.CAMERA -> {
                cameraProvider = CameraProvider(this)
                cameraProvider?.onRestoreInstanceState(savedInstanceState)
                savedInstanceState ?: cameraProvider?.startIntent()
            }

            else -> {
                Log.e(TAG, "Image provider can never be null")
                setError(getString(R.string.error_task_canceled))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        cameraProvider?.onRequestPermissionsResult(requestCode)
        galleryProvider?.onRequestPermissionResult(requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        cameraProvider?.onActivityResult(requestCode, resultCode, data)
        galleryProvider?.onActivityResult(requestCode, resultCode, data)
        cropProvider.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        setResultCancel()
    }

    fun setImage(file: File) {
        imageFile = file
        when {
            cropProvider.isCropEnabled() -> cropProvider.startIntent(file)
            compressionProvider.isCompressionRequired(file) -> compressionProvider.compress(file)
            else -> setResult(file)
        }
    }

    fun setCropImage(file: File) {
        cropFile = file
        cameraProvider?.let {
            imageFile?.delete()
            imageFile = null
        }

        if (compressionProvider.isCompressionRequired(file)) {
            compressionProvider.compress(file)
        } else {
            setResult(file)
        }
    }
    fun setCompressedImage(file: File) {
        cameraProvider?.let {
            imageFile?.delete()
        }

        cropFile?.delete()
        cropFile = null

        setResult(file)
    }

    private fun setResult(file: File) {
        val intent = Intent()
        intent.data = Uri.fromFile(file)
        intent.putExtra(ImagePicker.EXTRA_FILE_PATH, file.absoluteFile)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun setResultCancel() {
        setResult(Activity.RESULT_CANCELED, getCanceledIntent(this))
        finish()
    }

    fun setError(message: String) {
        val intent = Intent()
        intent.putExtra(ImagePicker.EXTRA_ERROR, message)
        setResult(ImagePicker.RESULT_ERROR, intent)
        finish()
    }
}
