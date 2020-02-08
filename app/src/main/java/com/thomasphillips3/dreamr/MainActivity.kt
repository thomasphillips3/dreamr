package com.thomasphillips3.dreamr

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonLoadImage.setOnClickListener { showPictureDialog() }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")

        val pictureDialogItems = arrayOf("Select photo from gallery", "Take a photo")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY) {
            if (data != null) {
                val contentUri = data!!.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                    val path = saveImage(bitmap)
                    Toast.makeText(this@MainActivity, "Image saved", Toast.LENGTH_SHORT).show()
                    imageView!!.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "Save failed", Toast.LENGTH_SHORT).show()
                }
            } else if (requestCode == CAMERA) {
                val thumbnail = data!!.extras!!.get("data") as Bitmap
                imageView!!.setImageBitmap(thumbnail)
                saveImage(thumbnail)
                Toast.makeText(this@MainActivity, "Image saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveImage(bitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory =
            File((Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY)
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs()
        }

        try {
            val f = File(
                wallpaperDirectory,
                ((Calendar.getInstance()).timeInMillis.toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(this, arrayOf(f.getPath()), arrayOf("image/jpeg"), null)
            fo.close()
            return f.getAbsolutePath()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@MainActivity, "Save failed", Toast.LENGTH_SHORT).show()
        }
        return ""
    }

    companion object {
        private const val IMAGE_DIRECTORY: String = "/dreamr"
        private const val GALLERY = 1
        private const val CAMERA = 2

    }
}
