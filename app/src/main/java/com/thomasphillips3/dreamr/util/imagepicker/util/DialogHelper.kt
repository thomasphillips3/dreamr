package com.thomasphillips3.dreamr.util.imagepicker.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.thomasphillips3.dreamr.R
import com.thomasphillips3.dreamr.util.imagepicker.constant.ImageProvider
import com.thomasphillips3.dreamr.util.imagepicker.listener.ResultListener
import kotlinx.android.synthetic.main.dialog_choose_app.view.*

fun showChooseAppDialog(context: Context, listener: ResultListener<ImageProvider>) {
    val layoutInflater = LayoutInflater.from(context)
    val customView = layoutInflater.inflate(R.layout.dialog_choose_app, null)

    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.title_choose_image_provider)
        .setView(customView)
        .setOnCancelListener {
            listener.onResult(null)
        }
        .show()

    customView.cameraPick.setOnClickListener {
        listener.onResult(ImageProvider.CAMERA)
        dialog.dismiss()
    }

    customView.galleryPick.setOnClickListener {
        listener.onResult(ImageProvider.GALLERY)
        dialog.dismiss()
    }
}