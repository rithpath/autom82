package com.wydgettech.contextualwalls

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.os.SystemClock
import android.view.SurfaceHolder
import android.graphics.Movie
import android.graphics.Canvas
import android.os.Handler
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.MotionEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class GIFWallpaper : WallpaperService() {

    var engine: Engine? = null
    override fun onCreateEngine(): Engine? {
        try {
            engine = WallPaperEngine()
            return engine
        } catch (e: IOException) {
            Log.w("Walls", "Error creating Engine", e)
            stopSelf()
            return null
        }
    }

    internal inner class WallPaperEngine @Throws(IOException::class)
    constructor() : Engine() {

        private var gifMovie: Movie? = null
        private var duration: Int = 0
        private var runnable: Runnable
        var gifScaleX: Float = 0.toFloat()
        var gifScaleY: Float = 0.toFloat()
        var width: Int = 0
        var height: Int = 0
        var timeInMovie: Int = 0
        var gifStart: Long = 0
        var canvas: Canvas? = null
        var statics: Boolean = false
        var link: String = ""
        var weather: String = "sunny"
        var receiver: BroadcastReceiver
        var filter: IntentFilter
        var gifID: Int = R.raw.default1
        var sharedPreferences: SharedPreferences

        init {
            sharedPreferences = applicationContext.getSharedPreferences("ContextualWalls", 0)
            if (gifMovie == null) {
                val weather = sharedPreferences.getString("weatherType", "sunny")!!
                if (weather != null)
                    openGIF(weather)
                else
                    openGIF("default1")
            }
            if (link == "")
                link = sharedPreferences.getString("linkURL", "")!!

            statics = sharedPreferences.getBoolean("static", false)

            runnable = Runnable { runWall() }
            filter = IntentFilter("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    statics = sharedPreferences.getBoolean("static", false)
                    if (intent.hasExtra("link")) {
                        link = intent.getStringExtra("link")
                        Log.d("link", link)
                    }
                    if (intent.hasExtra("weather")) {
                        weather = intent.getStringExtra("weather")
                        openGIF(weather)
                    }
                }
            }
            registerReceiver(receiver, filter)
        }

        private fun openGIF(weather: String) {
            gifID = applicationContext.resources.getIdentifier(
                weather,
                "raw",
                applicationContext.packageName
            )

            initStuff(resources.openRawResource(gifID))
            statics = false
        }

        fun initStuff(input: InputStream) {
            val `is` = input

            if (`is` != null) {

                try {
                    gifMovie = Movie.decodeStream(`is`)
                    gifScaleX = width / (1f * gifMovie!!.width())
                    gifScaleY = height / (1f * gifMovie!!.height())
                    duration = gifMovie!!.duration()

                } finally {
                    `is`.close()
                }
            } else {
                throw IOException("Unable to open the gif wallpaper")
            }
            timeInMovie = -1
        }

        override fun onDestroy() {
            super.onDestroy()
            liveHandler.removeCallbacks(runnable)
            unregisterReceiver(receiver)
        }


        private var gestureDetector =
            GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (sharedPreferences.getBoolean("quick", false) && sharedPreferences.getString(
                            "package",
                            ""
                        ) != ""
                    ) {
                        Toast.makeText(
                            applicationContext,
                            "Opening Application",
                            Toast.LENGTH_SHORT
                        ).show()
                        val launchIntent =
                            packageManager.getLaunchIntentForPackage(
                                sharedPreferences.getString(
                                    "package",
                                    ""
                                )!!
                            )
                        startActivity(launchIntent)
                    } else {
                        Utility.getLocation(applicationContext) {
                            if (it != null)
                                Utility.getWeatherMessage(
                                    it?.latitude.toString(),
                                    it?.longitude.toString()
                                ) {
                                    if (it != null)
                                        startNotification(it)
                                }
                        }
                    }
                    return super.onDoubleTap(e)
                }
            })


        override fun onTouchEvent(event: MotionEvent?) {
            Log.d("touch", "touchevent")
            gestureDetector.onTouchEvent(event)
            super.onTouchEvent(event)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                runWall()
            } else {
                liveHandler.removeCallbacks(runnable)
            }
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder, format: Int,
            width: Int, height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width
            this.height = height
            gifScaleX = width / (1f * gifMovie!!.width())
            gifScaleY = height / (1f * gifMovie!!.height())
            runWall()
        }

        override fun onOffsetsChanged(
            xOffset: Float, yOffset: Float,
            xOffsetStep: Float, yOffsetStep: Float, xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(
                xOffset, yOffset, xOffsetStep, yOffsetStep,
                xPixelOffset, yPixelOffset
            )
        }

        fun runWall() {
            tick()
            val surfaceHolder = surfaceHolder
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    if (statics)
                        drawStatic()
                    else
                        drawGif(canvas!!)
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }

            liveHandler.removeCallbacks(runnable)
            if (isVisible) {
                liveHandler.postDelayed(runnable, 1000L / 25L)
            }
        }

        fun tick() {
            if (timeInMovie.toLong() == -1L) {
                timeInMovie = 0
                gifStart = SystemClock.uptimeMillis()
            } else {
                val sincePrev = SystemClock.uptimeMillis() - gifStart
                timeInMovie = (sincePrev % duration).toInt()
            }
        }

        fun drawGif(canvas: Canvas) {

            canvas.save()
            canvas.scale(gifScaleX, gifScaleY)
            gifMovie!!.setTime(timeInMovie)
            gifMovie!!.draw(canvas, 0f, 0f)
            canvas.restore()
        }

        fun drawStatic() {

            Glide.with(applicationContext)
                .asBitmap().load(link)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("failed", "failure")
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        val bitmap = Bitmap.createScaledBitmap(
                            resource!!,
                            canvas!!.getWidth(),
                            canvas!!.getHeight(),
                            true
                        );
                        canvas!!.drawBitmap(bitmap, 0f, 0f, null)
                        return true
                    }
                }
                ).submit()
        }

        private fun startNotification(notifText: String) {
            createNotificationChannel()
            var builder = NotificationCompat.Builder(applicationContext, "WallsChannel")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle("Weather Requested!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notifText)
                )
                .setContentText(notifText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(applicationContext)) {
                // notificationId is a unique int for each notification that you must define
                notify(1, builder.build())
            }
        }

        private fun createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "ContextualWalls"
                val descriptionText = "Weather"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("WallsChannel", name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

    }

    companion object {
        internal val liveHandler = Handler()
    }
}