package com.thomasphillips3.dreamr.util.imagepicker.util;

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.*

fun getRealPath(context: Context, uri: Uri): String? {
    var path = getPathFromLocalUri(context, uri)
    if (path == null) {
        path = getPathFromRemoteUri(context, uri)
    }
    return path
}

private fun getPathFromLocalUri(context: Context, uri: Uri): String? {
    val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        if (!isExternalStorageDocument(uri)) {

            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            return if ("primary".equals(type, ignoreCase = true)) {
                if (split.size > 1) {
                    context.getExternalFilesDir(DIRECTORY_PICTURES).toString() + "/" + split[1]
                } else {
                    context.getExternalFilesDir(DIRECTORY_PICTURES).toString() + "/"
                }
            } else {
                val path = "storage" + "/" + docId.replace(":", "/")
                if (File(path).exists()) {
                    path
                } else {
                    "/storage/sdcard/" + split[1]
                }
            }
        } else if (isDownloadsDocument(uri)) {
            val fileName = getFilePath(context, uri)
            if (fileName != null) {
                return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
            }

            val id = DocumentsContract.getDocumentId(uri)
            val contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                java.lang.Long.valueOf(id)
            )
            return getDataColumn(context, contentUri, null, null)
        } else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type = split[0]

            var contentUri: Uri? = null
            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs)
        }
    } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
        return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
            context,
            uri,
            null,
            null
        )
    } else if ("file".equals(uri.scheme!!, ignoreCase = true)) return uri.path
    return null
}

private fun getDataColumn(
    context: Context,
    uri: Uri?,
    selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(column)

    try {
        cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } catch (e: Exception) {
    } finally {
        cursor?.close()
    }
    return null
}

private fun getFilePath(context: Context, uri: Uri): String? {

    var cursor: Cursor? = null
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

    try {
        cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

private fun getPathFromRemoteUri(context: Context, uri: Uri): String? {
    var file: File? = null
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    var success = false

    try {
        val extension = getImageExtension(uri)
        inputStream = context.contentResolver.openInputStream(uri)
        file = getImageFile(context.cacheDir, extension)
        outputStream = FileOutputStream(file)
        if (inputStream != null) {
            inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
            success = true
        }
    } catch (ignored: IOException) {
    } finally {
        try {
            inputStream?.close()
        } catch (ignored: IOException) {
        }
        try {
            outputStream?.close()
        } catch (ignored: IOException) {
            success = false
        }
    }
    return if (success) file!!.path else null
}

private fun getImageExtension(uriImage: Uri): String {
    var extension: String? = null

    try {
        val imagePath = uriImage.path
        if (imagePath != null && imagePath.lastIndexOf(".") != -1) {
            extension = imagePath.substring(imagePath.lastIndexOf(".") + 1)
        }
    } catch (e: Exception) {
        extension = null
    }

    if (extension == null || extension.isEmpty()) {
        extension = "jpg"
    }
    return ".$extension"
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

private fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}