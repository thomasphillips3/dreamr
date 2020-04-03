package com.thomasphillips3.dreamr.util.imagepicker.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun isPermissionGranted(context: Context, permissions: Array<String>): Boolean {
    return permissions.filter {
        hasPermission(context, it)
    }.size == permissions.size
}

fun isPermissionInManifest(context: Context, permission: String): Boolean {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
    val permissions = packageInfo.requestedPermissions

    if (permissions.isNullOrEmpty()) return false

    for (p in permissions) {
        if (p == permission) return true
    }
    return false
}

private fun hasPermission(context: Context, permission: String): Boolean {
    val selfPermission = ContextCompat.checkSelfPermission(context, permission)
    return selfPermission == PackageManager.PERMISSION_GRANTED
}