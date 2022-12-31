package com.sekhgmainuddin.timeshare.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
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
import java.io.File
import java.io.FileOutputStream
import java.util.regex.Matcher
import java.util.regex.Pattern

object Utils {

    const val MSG_BY_USER = 1
    const val MSG_BY_OPPOSITE = 2

    const val MSG_TYPE_MESSAGE = 1
    const val MSG_TYPE_IMAGE = 2
    const val MSG_TYPE_IMAGE_AND_MESSAGE = 3
    const val MSG_TYPE_PDF = 4
    const val MSG_TYPE_FILE = 5
    const val MSG_TYPE_VOICE = 6

    const val MSG_SENT = 1
    const val MSG_RECEIVED = 2
    const val MSG_SEEN = 3
    const val MSG_OLD = 4

    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    private const val MONTH_MILLIS = 30L * DAY_MILLIS
    private const val YEAR_MILLIS = 365L * DAY_MILLIS

    fun Long.getTimeAgo(): String? {
        val now: Long = System.currentTimeMillis()
        if (this > now || this <= 0) {
            return null
        }

        val diff = now - this
        return if (diff < MINUTE_MILLIS) {
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else if (diff < WEEK_MILLIS){
            (diff / DAY_MILLIS).toString() + " days ago"
        }
        else if (diff < MONTH_MILLIS){
            (diff / WEEK_MILLIS).toString() + " weeks ago"
        }
        else if (diff < 2L * YEAR_MILLIS){
            "1 year ago"
        }
        else {
            (diff / YEAR_MILLIS).toString() + " years ago"
        }
    }

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