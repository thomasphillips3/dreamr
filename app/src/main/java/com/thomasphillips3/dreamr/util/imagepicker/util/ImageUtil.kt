package com.thomasphillips3.dreamr.util.imagepicker.util

import android.graphics.*
import android.media.ExifInterface
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Throws(IOException::class)
fun compressImage(
    imageFile: File,
    reqWidth: Float,
    reqHeight: Float,
    compressFormat: Bitmap.CompressFormat,
    quality: Int,
    destinationPath: String
): File {
    var fileOutputStream: FileOutputStream? = null
    val file = File(destinationPath).parentFile

    if (!file.exists()) {
        file.mkdirs()
    }

    try {
        fileOutputStream = FileOutputStream(destinationPath)
        decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight)!!.compress(
            compressFormat,
            quality,
            fileOutputStream
        )
    } finally {
        if (fileOutputStream != null) {
            fileOutputStream.flush()
            fileOutputStream.close()
        }
    }
    return File(destinationPath)
}

@Throws(IOException::class)
private fun decodeSampledBitmapFromFile(
    imageFile: File, reqWidth: Float, reqHeight: Float
): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    var bmp: Bitmap? = BitmapFactory.decodeFile(imageFile.absolutePath, options)

    var actualHeight = options.outHeight
    var actualWidth = options.outWidth
    var imageRatio = actualWidth.toFloat() / actualHeight.toFloat()
    val maxRatio = reqWidth / reqHeight

    if (actualHeight > reqHeight || actualWidth > reqWidth) {
        if (imageRatio < maxRatio) {
            imageRatio = reqHeight / actualHeight
            actualWidth = (imageRatio * actualWidth).toInt()
        } else if (imageRatio > maxRatio) {
            imageRatio = reqWidth / actualWidth
            actualHeight = (imageRatio * actualHeight).toInt()
            actualWidth = reqWidth.toInt()
        }
    }

    options.inSampleSize = calculatInSampleSize(options, actualWidth, actualHeight)
    options.inJustDecodeBounds = false

    if (bmp != null && canUseForBitmap(bmp, options)) {
        options.inMutable = true
        options.inBitmap = bmp
    }
    options.inTempStorage = ByteArray(16 * 1024)

    try {
        bmp = BitmapFactory.decodeFile(imageFile.absolutePath, options)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
    }

    var scaledBitmap: Bitmap? = null
    try {
        scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
    } catch (e: OutOfMemoryError) {
        e.printStackTrace()
    }

    val ratioX = actualWidth / options.outWidth.toFloat()
    val ratioY = actualHeight / options.outHeight.toFloat()
    val middleX = actualWidth / 2.0f
    val middleY = actualHeight / 2.0f

    val scaleMatrix = Matrix()
    scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

    val canvas = Canvas(scaledBitmap!!)
    canvas.setMatrix(scaleMatrix)
    canvas.drawBitmap(
        bmp!!, middleX - bmp.width / 2,
        middleY - bmp.height / 2, Paint(Paint.FILTER_BITMAP_FLAG)
    )
    bmp.recycle()
    val exif: ExifInterface
    try {
        exif = ExifInterface(imageFile.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
        val matrix = Matrix()
        if (orientation == 6) {
            matrix.postRotate(90f)
        } else if (orientation == 3) {
            matrix.postRotate(180f)
        } else if (orientation == 8) {
            matrix.postRotate(270f)
        }
        scaledBitmap = Bitmap.createBitmap(
            scaledBitmap,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height,
            matrix,
            true
        )
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return scaledBitmap
}

private fun calculatInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        inSampleSize *= 2
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (
            (halfHeight / inSampleSize >= reqHeight) &&
            (halfWidth / inSampleSize >= reqWidth)
        ) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

private fun canUseForBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val width: Int = targetOptions.outWidth / targetOptions.inSampleSize
        val height: Int = targetOptions.outHeight / targetOptions.inSampleSize
        val byteCount: Int = width * height * getBytesPerPixel(candidate.config)
        byteCount <= candidate.allocationByteCount
    } else {
        candidate.width ==
                targetOptions.outWidth &&
                candidate.height == targetOptions.outHeight &&
                targetOptions.inSampleSize == 1
    }
}

private fun getBytesPerPixel(config: Bitmap.Config): Int {
    return when (config) {
        Bitmap.Config.ARGB_8888 -> 4
        Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> 2
        Bitmap.Config.ALPHA_8 -> 1
        else -> 1
    }
}