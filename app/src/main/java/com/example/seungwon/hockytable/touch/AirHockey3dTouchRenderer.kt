package com.example.seungwon.hockytable.touch

import android.R
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
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

    private val table: Table = Table()
    private var mallet: Mallet = Mallet(0.08f, 0.15f, 32)
    private var puck: Puck = Puck(0.06f, 0.02f, 32)

    private var textureProgram: TextureShaderProgram? = null
    private var colorProgram: ColorShaderProgram? = null

    private var texture = 0

    private var malletPressed = false
    private var blueMalletPosition: Geometry.Point = Geometry.Point(0f, mallet.height / 2f, 0.4f)

    private val leftBound = -0.5f
    private val rightBound = 0.5f
    private val farBound = -0.8f
    private val nearBound = 0.8f

    private var previousBlueMalletPosition: Geometry.Point? = null

    private var puckPosition: Geometry.Point = Geometry.Point(0f, puck.height / 2f, 0f)
    private var puckVector: Geometry.Vector = Geometry.Vector(0f, 0f, 0f)

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        textureProgram = TextureShaderProgram(context)
        colorProgram = ColorShaderProgram(context)
        texture = TextureHelper.loadTexture(context, R.drawable.bottom_bar)

        mallet = Mallet(0.08f, 0.15f, 32)
        puck = Puck(0.06f, 0.02f, 32)

        blueMalletPosition = Geometry.Point(0f, mallet.height / 2f, 0.4f)
        puckPosition = Geometry.Point(0f, puck.height / 2f, 0f)
        puckVector = Geometry.Vector(0f, 0f, 0f)

        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)
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


    override fun onDrawFrame(glUnused: GL10?) { // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // Translate the puck by its vector
        puckPosition = puckPosition.translate(puckVector)
        // If the puck struck a side, reflect it off that side.
        if (puckPosition.x < leftBound + puck.radius
                || puckPosition.x > rightBound - puck.radius) {
            puckVector = Geometry.Vector(-puckVector.x, puckVector.y, puckVector.z)
            puckVector = puckVector.scale(0.9f)
        }
        if (puckPosition.z < farBound + puck.radius
                || puckPosition.z > nearBound - puck.radius) {
            puckVector = Geometry.Vector(puckVector.x, puckVector.y, -puckVector.z)
            puckVector = puckVector.scale(0.9f)
        }
        // Clamp the puck position.
        puckPosition = Geometry.Point(
                clamp(puckPosition.x, leftBound + puck.radius, rightBound - puck.radius),
                puckPosition.y,
                clamp(puckPosition.z, farBound + puck.radius, nearBound - puck.radius)
        )
        // Friction factor
        puckVector = puckVector.scale(0.99f)
        // Update the viewProjection matrix, and create an inverted matrix for
        // touch picking.
        Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0,
                viewMatrix, 0)
        Matrix.invertM(invertedViewProjectionMatrix, 0, viewProjectionMatrix, 0)
        // Draw the table.
        positionTableInScene()
        textureProgram?.useProgram()
        textureProgram?.setUniforms(modelViewProjectionMatrix, texture)
        table.bindData(textureProgram!!)
        table.draw()
        // Draw the mallets.
        positionObjectInScene(0f, mallet.height / 2f, -0.4f)
        colorProgram?.useProgram()
        colorProgram?.setUniforms(modelViewProjectionMatrix, 1f, 0f, 0f)
        mallet.bindData(colorProgram!!)
        mallet.draw()
        positionObjectInScene(blueMalletPosition.x, blueMalletPosition.y,
                blueMalletPosition.z)
        colorProgram?.setUniforms(modelViewProjectionMatrix, 0f, 0f, 1f)
        // Note that we don't have to define the object data twice -- we just
        // draw the same mallet again but in a different position and with a
        // different color.
        mallet.draw()
        // Draw the puck.
        positionObjectInScene(puckPosition.x, puckPosition.y, puckPosition.z)
        colorProgram?.setUniforms(modelViewProjectionMatrix, 0.8f, 0.8f, 1f)
        puck.bindData(colorProgram!!)
        puck.draw()
    }

    fun handleTouchPress(normalizedX: Float, normalizedY: Float) {
        val ray: Geometry.Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
        // Now test if this ray intersects with the mallet by creating a
// bounding sphere that wraps the mallet.
        val malletBoundingSphere = Geometry.Sphere(Geometry.Point(
                blueMalletPosition.x,
                blueMalletPosition.y,
                blueMalletPosition.z),
                mallet.height / 2f)
        // If the ray intersects (if the user touched a part of the screen that
// intersects the mallet's bounding sphere), then set malletPressed =
// true.
        malletPressed = Geometry.intersects(malletBoundingSphere, ray)
    }

    private fun convertNormalized2DPointToRay(
            normalizedX: Float, normalizedY: Float): Geometry.Ray { // We'll convert these normalized device coordinates into world-space
// coordinates. We'll pick a point on the near and far planes, and draw a
// line between them. To do this transform, we need to first multiply by
// the inverse matrix, and then we need to undo the perspective divide.
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)
        Matrix.multiplyMV(
                nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0)
        Matrix.multiplyMV(
                farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0)
        // Why are we dividing by W? We multiplied our vector by an inverse
// matrix, so the W value that we end up is actually the *inverse* of
// what the projection matrix would create. By dividing all 3 components
// by W, we effectively undo the hardware perspective divide.
        divideByW(nearPointWorld)
        divideByW(farPointWorld)
        // We don't care about the W value anymore, because our points are now
// in world coordinates.
        val nearPointRay = Geometry.Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay = Geometry.Point(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        return Geometry.Ray(nearPointRay,
                Geometry.vectorBetween(nearPointRay, farPointRay))
    }

    private fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }

    fun handleTouchDrag(normalizedX: Float, normalizedY: Float) {
        if (malletPressed) {
            val ray: Geometry.Ray = convertNormalized2DPointToRay(normalizedX, normalizedY)
            // Define a plane representing our air hockey table.
            val plane = Geometry.Plane(Geometry.Point(0f, 0f, 0f), Geometry.Vector(0f, 1f, 0f))
            // Find out where the touched point intersects the plane
            // representing our table. We'll move the mallet along this plane.
            val touchedPoint: Geometry.Point = Geometry.intersectionPoint(ray, plane)
            // Clamp to bounds
            previousBlueMalletPosition = blueMalletPosition

//            blueMalletPosition =
//                    Geometry.Point(touchedPoint.x, mallet.height / 2f, touchedPoint.z)

            // Clamp to bounds
            blueMalletPosition = Geometry.Point(
                    clamp(touchedPoint.x,
                            leftBound + mallet.radius,
                            rightBound - mallet.radius),
                    mallet.height / 2f,
                    clamp(touchedPoint.z,
                            0f + mallet.radius,
                            nearBound - mallet.radius))
            // Now test if mallet has struck the puck.
            val distance: Float = Geometry.vectorBetween(blueMalletPosition, puckPosition).length()
            if (distance < puck.radius + mallet.radius) { // The mallet has struck the puck. Now send the puck flying
                // based on the mallet velocity.
                puckVector = Geometry.vectorBetween(
                        previousBlueMalletPosition!!, blueMalletPosition)
            }
        }
    }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return Math.min(max, Math.max(value, min))
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
}