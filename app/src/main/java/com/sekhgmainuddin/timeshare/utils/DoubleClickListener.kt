import android.os.Handler
import android.os.Looper
import android.view.View
import java.util.*

abstract class DoubleClickListener : View.OnClickListener {
    private var timer: Timer? = null //at class level;
    private val DELAY = 400L
    var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            processDoubleClickEvent(v)
        } else {
            processSingleClickEvent(v)
        }
        lastClickTime = clickTime
    }

    fun processSingleClickEvent(v: View?) {
        val handler = Handler(Looper.getMainLooper())
        val mRunnable = Runnable {
            onSingleClick(v) //Do what ever u want on single click
        }
        val timertask: TimerTask = object : TimerTask() {
            override fun run() {
                handler.post(mRunnable)
            }
        }
        timer = Timer()
        timer!!.schedule(timertask, DELAY)
    }

    fun processDoubleClickEvent(v: View?) {
        if (timer != null) {
            timer!!.cancel() //Cancels Running Tasks or Waiting Tasks.
            timer!!.purge() //Frees Memory by erasing cancelled Tasks.
        }
        onDoubleClick(v) //Do what ever u want on Double Click
    }

    abstract fun onSingleClick(v: View?)
    abstract fun onDoubleClick(v: View?)

    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}