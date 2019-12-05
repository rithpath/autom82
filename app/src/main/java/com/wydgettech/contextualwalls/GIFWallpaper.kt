package com.wydgettech.contextualwalls

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
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.MotionEvent
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener


class GIFWallpaper : WallpaperService() {

    var engine: Engine? = null
    override fun onCreateEngine(): Engine? {
        try {
            engine = WallPaperEngine()
            return engine
        } catch (e: IOException) {
            Log.w(TAG, "Error creating WallPaperEngine", e)
            stopSelf()
            return null
        }
    }



    internal inner class WallPaperEngine @Throws(IOException::class)
    constructor() : Engine() {

        private var liveMovie: Movie? = null
        private var duration: Int = 0
        private var runnable: Runnable
        var mScaleX: Float = 0.toFloat()
        var mScaleY: Float = 0.toFloat()
        var width: Int = 0
        var height: Int = 0
        var mWhen: Int = 0
        var mStart: Long = 0
        var canvas: Canvas? = null
        var statics: Boolean = false
        var link: String = ""
        var weather: String = "sunny"

        init {
            Log.d("htt", "walls resetes")
            initStuff(resources.openRawResource(R.raw.rainy))
            runnable = Runnable { runWall() }
            val filter = IntentFilter("com.wydgettech.contextualwalls.CHANGEWALLPAPER")
            var receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.hasExtra("link")){
                        //convert to bitmap
                        link = intent.getStringExtra("link")
                        statics = true
                        Log.d("link", link)

                    }

                    if (intent.hasExtra("weather")){
                        //convert to bitmap
                        weather = intent.getStringExtra("weather")
                        val id = context.resources.getIdentifier(
                            weather,
                            "raw",
                            context.packageName
                        )
                        initStuff(resources.openRawResource(id))
                        statics = false

                    }
                   // initStuff(resources.openRawResource(R.raw.sunny))
                }
            }
            registerReceiver(receiver, filter)
        }


        fun initStuff(input: InputStream) {
            val `is`  = input

            if (`is` != null) {

                try {
                    liveMovie = Movie.decodeStream(`is`)
                    mScaleX = width / (1f * liveMovie!!.width())
                    mScaleY = height / (1f * liveMovie!!.height())
                    duration = liveMovie!!.duration()

                } finally {
                    `is`.close()
                }
            } else {
                throw IOException("Unable to open R.raw.hand")
            }
            mWhen = -1
        }

        override fun onDestroy() {
            super.onDestroy()
            liveHandler.removeCallbacks(runnable)
        }

        private var gestureDetector = GestureDetector(applicationContext, object: GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent?): Boolean {
                Toast.makeText(applicationContext, "Loading new walls", Toast.LENGTH_SHORT).show()
                Log.d("walls", link)
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
            }
            else {
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
            mScaleX = width / (1f * liveMovie!!.width())
            mScaleY = height / (1f * liveMovie!!.height())
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
                    if(statics)
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
            if (mWhen.toLong() == -1L) {
                mWhen = 0
                mStart = SystemClock.uptimeMillis()
            } else {
                val mDiff = SystemClock.uptimeMillis() - mStart
                mWhen = (mDiff % duration).toInt()
            }
        }

        fun drawGif(canvas: Canvas) {
            canvas.save()
            canvas.scale(mScaleX, mScaleY)
            liveMovie!!.setTime(mWhen)
            liveMovie!!.draw(canvas, 0f, 0f)
            canvas.restore()
        }

        fun drawStatic() {
            //canvas!!.scale(mScaleX, mScaleY)
            Log.d("trying to draw", "trying")
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
                        Log.d("succ", "succcc")
//
//                        val scaleX = width / (1f * resource!!.width)
//                        val scaleY = height / (1f * resource!!.height)
//                        canvas!!.scale(scaleX, scaleY)
                        canvas!!.drawBitmap(resource!!, 0f, 0f, null)
                        return true
                    }

                }
                ).submit()
        }


    }

    companion object {
        internal val TAG = "LIVE_WALLPAPER"
        internal val liveHandler = Handler()
    }
}