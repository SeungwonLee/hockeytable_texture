package com.example.seungwon.hockytable.touch

import android.R
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.opengl.Matrix.multiplyMV
import com.example.seungwon.hockytable.texture.MatrixHelper
import com.example.seungwon.hockytable.texture.Table
import com.example.seungwon.hockytable.texture.TextureShaderProgram
import com.example.seungwon.hockytable.threed.ColorShaderProgram
import com.example.seungwon.hockytable.threed.model.Mallet
import com.example.seungwon.hockytable.threed.model.Puck
import com.example.seungwon.hockytable.utils.Geometry
import com.example.seungwon.hockytable.utils.TextureHelper
import javax.microedition.khronos.opengles.GL10


class AirHockey3dTouchRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private val invertedViewProjectionMatrix = FloatArray(16)

    private val projectionMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val viewProjectionMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private var table: Table? = null
    private var mallet: Mallet? = null
    private var puck: Puck? = null

    private var textureProgram: TextureShaderProgram? = null
    private var colorProgram: ColorShaderProgram? = null

    private var texture = 0

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        table = Table()
        mallet = Mallet(0.08f, 0.15f, 32)
        puck = Puck(0.06f, 0.02f, 32)
        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
        texture = TextureHelper.loadTexture(context, R.drawable.bottom_bar)

        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0);
    }

    override fun onSurfaceChanged(glUnused: GL10?, width: Int, height: Int) {
        // Set the OpenGL viewport to fill the entire surface.
        GLES20.glViewport(0, 0, width, height)
        MatrixHelper.perspectiveM(
            projectionMatrix, 45f, width.toFloat()
                    / height.toFloat(), 1f, 10f
        )
        Matrix.setLookAtM(viewMatrix, 0, 0f, 1.2f, 2.2f, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onDrawFrame(glUnused: GL10?) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Multiply the view and projection matrices together.
        Matrix.multiplyMM(
            viewProjectionMatrix,
            0,
            projectionMatrix,
            0,
            viewMatrix,
            0
        )

        // Draw the table.
        positionTableInScene()
        textureProgram?.useProgram()
        textureProgram?.setUniforms(modelViewProjectionMatrix, texture)
        table?.bindData(textureProgram!!)
        table?.draw()

        // Draw the mallets.
        positionObjectInScene(0f, mallet!!.height / 2f, -0.4f)
        colorProgram!!.useProgram()
        colorProgram!!.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet!!.bindData(colorProgram!!)
        mallet!!.draw()
        positionObjectInScene(0f, mallet!!.height / 2f, 0.4f)
        colorProgram!!.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f)
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet!!.draw()

        // Draw the puck.
        positionObjectInScene(0f, puck!!.height / 2f, 0f)
        colorProgram!!.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f)
        puck!!.bindData(colorProgram!!)
        puck!!.draw()
    }

    private fun positionTableInScene() {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, -90f, 1f, 0f, 0f)
        Matrix.multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    // The mallets and the puck are positioned on the same plane as the table.
    private fun positionObjectInScene(
        x: Float,
        y: Float,
        z: Float
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.multiplyMM(
            modelViewProjectionMatrix, 0, viewProjectionMatrix,
            0, modelMatrix, 0
        )
    }

    private fun convertNormalized2DPointToRay(
        normalizedX: Float, normalizedY: Float
    ): Ray? {
        // We'll convert these normalized device coordinates into world-space
        // coordinates. We'll pick a point on the near and far planes, and draw a
        // line between them. To do this transform, we need to first multiply by
        // the inverse matrix, and then we need to undo the perspective divide.
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)
        multiplyMV(
            nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0
        )
        multiplyMV(
            farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0
        )

        // Why are we dividing by W? We multiplied our vector by an inverse
        // matrix, so the W value that we end up is actually the *inverse* of
        // what the projection matrix would create. By dividing all 3 components
        // by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld)
        divideByW(farPointWorld)

        // We don't care about the W value anymore, because our points are now
        // in world coordinates.
        val nearPointRay =
            Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay =
            Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        return Ray(
            nearPointRay,
            Geometry.vectorBetween(nearPointRay, farPointRay)
        )
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }
}