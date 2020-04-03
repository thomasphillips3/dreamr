package com.thomasphillips3.dreamr

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.mobile.client.AWSMobileClient
import com.thomasphillips3.dreamr.util.getFileInfo
import com.thomasphillips3.dreamr.util.imagepicker.ImagePicker
import com.thomasphillips3.dreamr.util.imagepicker.constant.ImageProvider
import com.thomasphillips3.dreamr.util.openUrl
import com.thomasphillips3.dreamr.util.showImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_camera_only.*
import kotlinx.android.synthetic.main.content_gallery_only.*
import kotlinx.android.synthetic.main.content_profile.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private var cameraFile: File? = null
    private var galleryFile: File? = null
    private var profileFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AWSMobileClient.getInstance().initialize(this).execute()
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        imageProfile.setDrawableImage(R.drawable.ic_person, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_github -> {
                openUrl(this, GITHUB_REPOSITORY)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun pickProfileImage(view: View) {
        ImagePicker.with(this)
            .cropSquare()
            .maxResultSize(512, 512)
            .start(PROFILE_IMAGE_REQ_CODE)
    }

    fun pickGalleryImage(view: View) {
        ImagePicker.with(this)
            .crop()
            .provider(ImageProvider.GALLERY)
            .maxResultSize(1080, 1920)
            .start(GALLERY_IMAGE_REQ_CODE)
    }

    fun pickCameraImage(view: View) {
        ImagePicker.with(this)
            .provider(ImageProvider.CAMERA)
            .compress(1024)
            .start(CAMERA_IMAGE_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "data intent: {$data}\nresultCode: {$resultCode}\nrequestCode: {$requestCode}\nextras: {${data?.extras}}")

        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.e(TAG, "Path:${ImagePicker.getFilePath(data)}")
                val file = ImagePicker.getFile(data)!!
                when(requestCode) {
                    PROFILE_IMAGE_REQ_CODE -> {
                        profileFile = file
                        imageProfile.setLocalImage(file, true)
                    }

                    GALLERY_IMAGE_REQ_CODE -> {
                        galleryFile = file
                        imageGallery.setLocalImage(file)
                    }

                    CAMERA_IMAGE_REQ_CODE -> {
                        cameraFile = file
                        imageCamera.setLocalImage(file, false)
                    }
                }
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Task canceled", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun showImage(view: View) {
        val file = when(view) {
            imageProfile -> profileFile
            imageCamera -> cameraFile
            imageGallery -> galleryFile
            else -> null
        }

        file?.let {
            showImage(this, file)
        }
    }

    fun showImageInfo(view: View) {
        val file = when(view) {
            imageProfileInfo -> profileFile
            imageCameraInfo -> cameraFile
            imageGalleryInfo -> galleryFile
            else -> null
        }

        AlertDialog.Builder(this)
            .setTitle("Image Info")
            .setMessage(getFileInfo(file))
            .setPositiveButton("Ok", null)
            .show()
    }

    companion object {
        private const val GITHUB_REPOSITORY = "https://github.com/thomasphillips3/dreamr"
        private const val TAG = "MainActivity"
        private const val PROFILE_IMAGE_REQ_CODE = 101
        private const val GALLERY_IMAGE_REQ_CODE = 102
        private const val CAMERA_IMAGE_REQ_CODE = 103
    }
}
