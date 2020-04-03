package com.thomasphillips3.dreamr.util.imagepicker.provider

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.ImagePicker
import com.thomasphillips3.dreamr.util.imagepicker.ImagePickerActivity
import com.thomasphillips3.dreamr.util.imagepicker.util.compressImage
import com.thomasphillips3.dreamr.util.imagepicker.util.getImageFile
import java.io.File

class CompressionProvider(activity: ImagePickerActivity) : BaseProvider(activity) {
    companion object {
        private val TAG = CompressionProvider::class.java.simpleName
    }

    private val maxWidth: Int
    private val maxHeight: Int
    private val maxFileSize: Long

    private var originalFile: File? = null

    init {
        val bundle = activity.intent.extras!!
        maxWidth = bundle.getInt(ImagePicker.EXTRA_MAX_WIDTH, 0)
        maxHeight = bundle.getInt(ImagePicker.EXTRA_MAX_HEIGHT, 0)
        maxFileSize = bundle.getLong(ImagePicker.EXTRA_IMAGE_MAX_SIZE, 0)
    }

    fun isCompressionEnabled(): Boolean {
        return maxFileSize > 0L
    }

    fun isCompressionRequired(file: File): Boolean {
        val status = isCompressionEnabled() && getSizeDiff(file) > 0L
        if (!status && maxWidth > 0 && maxHeight > 0) {
            val sizes = getImageSize(file)
            return sizes[0] > maxWidth || sizes[1] > maxHeight
        }
        return status
    }

    private fun getSizeDiff(file: File): Long {
        return file.length() - maxFileSize
    }

    fun compress(file: File) {
        startCompressionWorker(file)
    }

    @SuppressLint("StaticFieldLeak")
    private fun startCompressionWorker(file: File) {
        originalFile = file
        object : AsyncTask<File, Void, File>() {
            override fun doInBackground(vararg params: File): File? {
                return startCompression(params[0])
            }

            override fun onPostExecute(result: File) {
                super.onPostExecute(result)
                if (file != null) {
                    handleResult(file)
                } else {
                    setError(R.string.error_failed_to_compress_image)
                }
            }
        }.execute(file)
    }

    private fun startCompression(file: File): File? {
        var newFile: File? = null
        var attempt = 0
        var lastAttempt = 0
        do {
            newFile?.delete()
            newFile = applyCompression(file, attempt)
            if (newFile == null) {
                return if (attempt > 0) {
                    applyCompression(file, lastAttempt)
                } else {
                    null
                }
            }
            lastAttempt = attempt
            if (maxFileSize > 0) {
                val diff = getSizeDiff(newFile)
                attempt += when {
                    diff > 1024 * 1024 -> 3
                    diff > 500 * 1024 -> 2
                    else -> 1
                }
            } else {
                attempt++
            }
        } while (isCompressionRequired(newFile!!))

        return newFile
    }

    private fun applyCompression(file: File, attempt: Int): File? {
        val resList = resolutionList()
        if (attempt >= resList.size) {
            return null
        }

        // Apply logic to get scaled bitmap resolution.
        val resolution = resList[attempt]
        var maxWidth = resolution[0]
        var maxHeight = resolution[1]

        if (maxWidth > 0 && maxHeight > 0) {
            if (maxWidth > maxWidth || maxHeight > maxHeight) {
                maxHeight = maxHeight
                maxWidth = maxWidth
            }
        }

        var format = Bitmap.CompressFormat.JPEG
        var quality = 90
        if (file.absolutePath.endsWith(".png")) {
            format = Bitmap.CompressFormat.PNG
            quality = 100
        }

        val compressFile: File? = getImageFile()
        return if (compressFile != null) {
            compressImage(
                file, maxWidth.toFloat(), maxHeight.toFloat(),
                format, quality, compressFile.absolutePath
            )
        } else {
            null
        }
    }

    private fun resolutionList(): List<IntArray> {
        return listOf(
            intArrayOf(2448, 3264), // 8.0 MP
            intArrayOf(2008, 3032), // 6.0 MP
            intArrayOf(1944, 2580), // 5.0 MP
            intArrayOf(1680, 2240), // 4.0 MP
            intArrayOf(1536, 2048), // 3.0 MP
            intArrayOf(1200, 1600), // 2.0 MP
            intArrayOf(1024, 1392), // 1.3 MP
            intArrayOf(960, 1280), // 1.0 MP
            intArrayOf(768, 1024), // 0.7 MP
            intArrayOf(600, 800), // 0.4 MP
            intArrayOf(480, 640), // 0.3 MP
            intArrayOf(240, 320), // 0.15 MP
            intArrayOf(120, 160), // 0.08 MP
            intArrayOf(60, 80), // 0.04 MP
            intArrayOf(30, 40) // 0.02 MP
        )
    }

    private fun handleResult(file: File) {
        activity.setCompressedImage(file)
    }

    private fun getImageSize(file: File): IntArray {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, options)
        return intArrayOf(options.outWidth, options.outHeight)
    }
}