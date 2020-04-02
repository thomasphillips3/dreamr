package com.thomasphillips3.dreamr.util.imagepicker.util

import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

fun getImageFile(dir: File? = null, extension: String? = null): File? {
    try {
        val ext = extension ?: ".jpg"
        val imageFileName = "IMG_${getTimestamp()}$ext"
        val storageDir = dir ?: getCameraDirectory()

        if (!storageDir.exists()) storageDir.mkdirs()

        val file = File(storageDir, imageFileName)
        file.createNewFile()

        return file
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}

private fun getCameraDirectory(): File {
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
    return File(dir, "Camera")
}

private fun getTimestamp(): String {
    val timeFormat = "yyyyMMdd_HHmmssSSSS"
    return SimpleDateFormat(timeFormat, Locale.getDefault()).format(Date())
}

fun getFreeSpace(file: File): Long {
    val stat = StatFs(file.path)
    val availableBlocks = stat.availableBlocksLong
    val blockSize = stat.blockSizeLong
    return availableBlocks * blockSize
}