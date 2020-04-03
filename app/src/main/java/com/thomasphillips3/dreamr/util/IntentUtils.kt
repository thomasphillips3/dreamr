package com.thomasphillips3.dreamr.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import java.io.File

fun showImage(activity: Activity, file: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.provider", file)
    intent.setDataAndType(uri, "image/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    activity.startActivity(intent)
}

fun openUrl(activity: Activity, url: String) {
    val link = Uri.parse(url)
    CustomTabsIntent.Builder()
        .build()
        .launchUrl(activity, link)
}