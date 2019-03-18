package com.example.seungwon.hockytable

import android.content.Context
import android.opengl.GLES10
import android.opengl.GLES20
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLSurfaceView
import android.opengl.Matrix.orthoM
import com.example.seungwon.hockytable.utils.ShaderHelper
import com.example.seungwon.hockytable.utils.TextResourceReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class AirHockeyMatrixRenderer : GLSurfaceView.Renderer {

    private var program: Int = 0
    private var aPositionLocation: Int = 0
    private var aColorLocation = 0
    private var uMatrixLocation: Int = 0

    private var vertexData: FloatBuffer? = null
    private var context: Context? = null

    private val projectionMatrix = FloatArray(16)


    constructor(context: Context) {
        this.context = context

        //
        // Vertex data is stored in the following manner:
        //
        // The first two numbers are part of the position: X, Y
        // The next three numbers are part of the color: R, G, B
        //
        val tableVerticesWithTriangles = floatArrayOf(
                // Order of coordinates: X, Y, R, G, B
                // Triangle Fan
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
// Line 1
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 1f, 0f, 0f,
// Mallets
                0f, -0.4f, 0f, 0f, 1f,
                0f, 0.4f, 1f, 0f, 0f
        )

        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        vertexData?.put(tableVerticesWithTriangles)
    }


    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Assign the matrix
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // Draw the table.
        GLES10.glDrawArrays(GLES10.GL_TRIANGLE_FAN, 0, 6)

        // Draw the center dividing line.
        GLES10.glDrawArrays(GLES20.GL_LINES, 6, 2)

        // Draw the first mallet.
        GLES10.glDrawArrays(GLES20.GL_POINTS, 8, 1)

        // Draw the second mallet.
        GLES10.glDrawArrays(GLES20.GL_POINTS, 9, 1)
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val aspectRatio = if (width > height)
            width.toFloat() / height
        else
            height.toFloat() / width

        if (width >= height) {
            // Landscape
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
        } else {
            // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        var vertexShaderSource = ""
        var fragmentShaderSource = ""

        context?.let {
            vertexShaderSource = TextResourceReader.readTextFileFromResource(context as Context, R.raw.simple_vertex_shader_matrix)
            fragmentShaderSource = TextResourceReader.readTextFileFromResource(context as Context, R.raw.simple_fragment_shader_with_color)
        }

        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource)

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)

        ShaderHelper.validateProgram(program)

        GLES20.glUseProgram(program)

        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION)
        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR)

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData?.position(0)
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT,
                false, STRIDE, vertexData)

        GLES20.glEnableVertexAttribArray(aPositionLocation)

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_COLOR_LOCATION.
        vertexData?.position(POSITION_COMPONENT_COUNT)
        GLES20.glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT,
                false, STRIDE, vertexData)

        GLES20.glEnableVertexAttribArray(aColorLocation)

    }

    companion object {
        private const val A_POSITION = "a_Position"
        private const val A_COLOR = "a_Color"
        private const val U_MATRIX = "u_Matrix"

        private const val POSITION_COMPONENT_COUNT = 2
        private const val BYTES_PER_FLOAT = 4
        private const val COLOR_COMPONENT_COUNT = 3
        private const val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }
}