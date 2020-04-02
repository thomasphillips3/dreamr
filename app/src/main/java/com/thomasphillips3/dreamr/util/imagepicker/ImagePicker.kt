package com.thomasphillips3.dreamr.util.imagepicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import com.thomasphillips3.dreamr.util.imagepicker.constant.ImageProvider
import com.thomasphillips3.dreamr.util.imagepicker.listener.ResultListener
import com.thomasphillips3.dreamr.util.imagepicker.util.showChooseAppDialog
import java.io.File

open class ImagePicker {
    companion object {
        const val REQUEST_CODE = 2404
        const val RESULT_ERROR = 64

        internal const val EXTRA_IMAGE_PROVIDER = "extra.image_provider"
        internal const val EXTRA_IMAGE_MAX_SIZE = "extra.image_max_size"
        internal const val EXTRA_CROP = "extra.crop"
        internal const val EXTRA_CROP_X = "extra.crop_x"
        internal const val EXTRA_CROP_Y = "extra.crop_y"
        internal const val EXTRA_MAX_WIDTH = "extra.max_width"
        internal const val EXTRA_MAX_HEIGHT = "extra.max_height"

        internal const val EXTRA_ERROR = "extra.error"
        internal const val EXTRA_FILE_PATH = "extra.file_path"

        fun with(activity: Activity): Builder {
            return Builder(activity)
        }

        fun with(fragment: Fragment): Builder {
            return Builder(fragment)
        }

        fun getError(data: Intent?): String {
            return data?.getStringExtra(EXTRA_ERROR) ?: "Unknown Error!"
        }

        fun getFilePath(data: Intent?): String? {
            Log.d("getFilePath", "data intent: {$data}")
            return data?.getStringExtra(EXTRA_FILE_PATH)
        }

        fun getFile(data: Intent?): File? {
            val path = getFilePath(data)
            if (path != null) {
                return File(path)
            }
            return null
        }
    }

    class Builder(private val activity: Activity) {

        private var fragment: Fragment? = null
        private var imageProvider = ImageProvider.BOTH

        private var cropX: Float = 0f
        private var cropY: Float = 0f
        private var crop: Boolean = false

        private var maxWidth: Int = 0
        private var maxHeight: Int = 0

        private var maxSize: Long = 0

        constructor(fragment: Fragment) : this(fragment.activity!!) {
            this.fragment = fragment
        }

        fun provider(imageProvider: ImageProvider) = apply {
            this.imageProvider = imageProvider
        }

        private fun crop(x: Float, y: Float): Builder {
            cropX = x
            cropY = y
            return crop()
        }

        fun crop() = apply {
            this.crop = true
        }

        fun cropSquare(): Builder {
            return crop(1f, 1f)
        }

        fun maxResultSize(width: Int, height: Int) = apply {
            this.maxWidth = width
            this.maxHeight = height
        }

        fun compress(maxSize: Int) = apply {
            this.maxSize = maxSize * 1024L
        }

        fun start() {
            start(REQUEST_CODE)
        }

        fun start(reqCode: Int) {
            if (imageProvider == ImageProvider.BOTH) {
                showImageProviderDialog(reqCode)
            } else {
                startActivity(reqCode)
            }
        }

        fun start(completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null) {
            if (imageProvider == ImageProvider.BOTH) {
                showImageProviderDialog(completionHandler)
            } else {
                startActivity(completionHandler)
            }
        }

        private fun showImageProviderDialog(reqCode: Int) {
            showChooseAppDialog(activity, object : ResultListener<ImageProvider> {
                override fun onResult(t: ImageProvider?) {
                    t?.let {
                        imageProvider = it
                        startActivity(reqCode)
                    }
                }
            })
        }

        private fun showImageProviderDialog(completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null) {
            showChooseAppDialog(activity, object : ResultListener<ImageProvider> {
                override fun onResult(t: ImageProvider?) {
                    if (t != null) {
                        imageProvider = t
                        startActivity(completionHandler)
                    } else {
                        val intent = ImagePickerActivity.getCanceledIntent(activity)
                        completionHandler?.invoke(Activity.RESULT_CANCELED, intent)
                    }
                }
            })
        }

        private fun getBundle(): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(EXTRA_IMAGE_PROVIDER, imageProvider)

            bundle.putBoolean(EXTRA_CROP, crop)
            bundle.putFloat(EXTRA_CROP_X, cropX)
            bundle.putFloat(EXTRA_CROP_Y, cropY)

            bundle.putInt(EXTRA_MAX_WIDTH, maxWidth)
            bundle.putInt(EXTRA_MAX_HEIGHT, maxHeight)

            bundle.putLong(EXTRA_IMAGE_MAX_SIZE, maxSize)

            return bundle
        }

        private fun startActivity(completionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = null) {

            try {
                val intent = Intent(activity, ImagePickerActivity::class.java)
                intent.putExtras(getBundle())
                if (fragment != null) {

                    fragment?.startForResult(intent) { result ->
                        completionHandler?.invoke(result.resultCode, result.data)
                    }?.onFailed { result ->
                        completionHandler?.invoke(result.resultCode, result.data)
                    }
                } else {
                    (activity as AppCompatActivity).startForResult(intent) { result ->
                        completionHandler?.invoke(result.resultCode, result.data)
                    }.onFailed { result ->
                        completionHandler?.invoke(result.resultCode, result.data)
                    }
                }
            } catch (e: Exception) {
                if (e is ClassNotFoundException) {
                    Toast.makeText(
                        if (fragment != null) fragment!!.context else activity,
                        "InlineActivityResult library not installed falling back to default method, please install " +
                                "it from https://github.com/florent37/InlineActivityResult if you want to get inline activity results.",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(REQUEST_CODE)
                }
            }
        }
        private fun startActivity(reqCode: Int) {
            val intent = Intent(activity, ImagePickerActivity::class.java)
            intent.putExtras(getBundle())
            if (fragment != null) {
                fragment?.startActivityForResult(intent, reqCode)
            } else {
                activity.startActivityForResult(intent, reqCode)
            }
        }
    }
}