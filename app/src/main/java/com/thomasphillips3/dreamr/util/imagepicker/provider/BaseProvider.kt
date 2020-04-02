package com.thomasphillips3.dreamr.util.imagepicker.provider

import android.content.ContextWrapper
import android.os.Bundle
import android.widget.Toast
import com.thomasphillips3.dreamr.util.imagepicker.ImagePickerActivity

abstract class BaseProvider(protected val activity: ImagePickerActivity): ContextWrapper(activity) {
    protected fun setError(error: String) {
        onFailure()
        activity.setError(error)
    }

    protected fun setError(errorRes: Int) {
        setError(getString(errorRes))
    }

    protected fun showToast(messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

    protected fun setResultCancel() {
        onFailure()
        activity.setResultCancel()
    }

    protected open fun onFailure() { }
    open fun onSaveInstanceState(outState: Bundle) { }
    open fun onRestoreInstanceState(savedInstanceState: Bundle?) { }
}