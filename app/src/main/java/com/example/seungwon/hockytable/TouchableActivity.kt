package com.example.seungwon.hockytable

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.example.seungwon.hockytable.threed.AirHockeyTable3dPuckRenderer
import com.example.seungwon.hockytable.touch.AirHockey3dTouchRenderer

class TouchableActivity : AppCompatActivity() {

    private var glSurfaceView: GLSurfaceView? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glSurfaceView = findViewById(R.id.gl_surface_view)

        val renderer = AirHockey3dTouchRenderer(this)
        glSurfaceView?.setEGLContextClientVersion(2)
        glSurfaceView?.setRenderer(renderer)
        glSurfaceView?.setOnTouchListener { v, event ->
            if (event != null) { // Convert touch coordinates into normalized device
                // coordinates, keeping in mind that Android's Y
                // coordinates are inverted.
                val normalizedX = event.x / v.width.toFloat() * 2 - 1
                val normalizedY = -(event.y / v.height.toFloat() * 2 - 1)
                if (event.action == MotionEvent.ACTION_DOWN) {
                    glSurfaceView?.queueEvent {
                        renderer.handleTouchPress(
                                normalizedX, normalizedY)
                    }
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    glSurfaceView?.queueEvent {
                        renderer.handleTouchDrag(
                                normalizedX, normalizedY)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }
}
