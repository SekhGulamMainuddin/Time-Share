package com.sekhgmainuddin.timeshare.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.*
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.view.isVisible
import com.google.common.net.MediaType.JPEG
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {

    fun View.slideVisibility(visibility: Boolean, durationTime: Long = 300) {
        val transition = Slide(Gravity.START)
        transition.apply {
            duration = durationTime
            addTarget(this@slideVisibility)
        }
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
        this.isVisible = visibility
    }

    fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    fun String.isValidPassword() : Boolean {
        val PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!/'|*.,<>;:()_`~?])(?=\\S+$).{8,}$"
        val pattern: Pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher? = this.let { pattern.matcher(it) }
        return matcher?.matches() == true
    }

    fun getFileExtension(uri: Uri, context: Context): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun Uri.getBitmap(contentResolver: ContentResolver) : Bitmap? = this.let {
        if(SDK_INT < 28) {
            return MediaStore.Images.Media.getBitmap(
                contentResolver,
                this
            )
        } else {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, this))
        }
    }

    fun Bitmap.saveAsJPG(filename: String, applicationContext: Context) = "$filename.jpg".let { name ->
        if (SDK_INT < Q)
            @Suppress("DEPRECATION")
            FileOutputStream(File(applicationContext.getExternalFilesDir(DIRECTORY_PICTURES), name))
                .use { compress(Bitmap.CompressFormat.JPEG, 90, it) }
        else {
            val values = ContentValues().apply {
                put(DISPLAY_NAME, name)
                put(MIME_TYPE, "image/jpg")
                put(RELATIVE_PATH, DIRECTORY_PICTURES)
                put(IS_PENDING, 1)
            }

            val resolver = applicationContext.contentResolver
            val uri = resolver.insert(EXTERNAL_CONTENT_URI, values)
            uri?.let { resolver.openOutputStream(it) }
                ?.use { compress(Bitmap.CompressFormat.JPEG, 70, it) }

            values.clear()
            values.put(IS_PENDING, 0)
            uri?.also {
                resolver.update(it, values, null, null) }
        }
    }

}