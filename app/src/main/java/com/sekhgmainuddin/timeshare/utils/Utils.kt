package com.sekhgmainuddin.timeshare.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Environment.DIRECTORY_PICTURES
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.*
import android.text.format.DateUtils
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.sekhgmainuddin.timeshare.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Matcher
import java.util.regex.Pattern


object Utils {

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

    fun Long.getMessageTIme(context: Context): String? {
        val time = DateUtils.formatDateTime(context, this, DateUtils.FORMAT_SHOW_TIME)
        val date = DateUtils.formatDateTime(context, this, DateUtils.FORMAT_SHOW_DATE)
        return time
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
    fun changeTextColorGradient(tv: TextView){
        val context= tv.context
        tv.setTextColor(context.getColor(R.color.orange))
        val textShader: Shader = LinearGradient(
            0f, 0f, tv.paint.measureText(tv.text.toString()), tv.textSize, intArrayOf(
                context.getColor(R.color.orange), context.getColor(R.color.orangePink)
            ), floatArrayOf(0f, 1f), Shader.TileMode.REPEAT
        )
        tv.paint.shader= textShader
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

    private val imageExtensionList= arrayListOf("jpg", "jpeg", "jpe" ,"jif", "jfif", "jfi","gif","webp","tiff","tif","psd","bmp","svg","svgz","pdf")
    private val videoExtensionList= arrayListOf("mp4")

    fun String.isImageOrVideo() :Int{
        if (this in imageExtensionList)
            return 0
        else if (this in videoExtensionList)
            return 1
        else
            return -1
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @Throws(Throwable::class)
    fun retrieveVideoFrameFromVideo(videoPath: String?): Bitmap? {
        var bitmap: Bitmap? = null
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(
                videoPath,
                HashMap()
            )
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            e.printStackTrace()
            throw Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.message)
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }

    fun File.getFirstPage(): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(this, ParcelFileDescriptor.MODE_READ_ONLY))
            val pageCount = renderer.pageCount
            if (pageCount > 0) {
                val page = renderer.openPage(0)
                bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    fun fileFromContentUri(name: String, context: Context, contentUri: Uri): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(contentUri, context)
        val fileName = name + if (fileExtension != null) ".$fileExtension" else ""

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }

}