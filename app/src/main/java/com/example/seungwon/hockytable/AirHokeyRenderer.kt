package com.example.seungwon.hockytable

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.seungwon.hockytable.utils.ShaderHelper
import com.example.seungwon.hockytable.utils.TextResourceReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class AirHokeyRenderer : GLSurfaceView.Renderer {
    private val U_COLOR = "u_Color"
    private val A_POSITION = "a_Position"
    private val POSITION_COMPONENT_COUNT = 2
    private val BYTES_PER_FLOAT = 4
    private var program: Int = 0
    private var uColorLocation: Int = 0
    private var aPositionLocation: Int = 0

    private var vertexData: FloatBuffer? = null
    private var context: Context? = null

//    val tableVertices = floatArrayOf(0f, 0f,
//            0f, 14f,
//            9f, 14f,
//            9f, 0f)
//
//    val trangle = floatArrayOf(
//            0f, 0f,
//            9f, 14f
//    )
//
//    val mallet = floatArrayOf(
//            4.5f, 2f,
//            4.5f, 12f
//    )

    constructor(context: Context) {
        this.context = context

        val tableVerticesWithTriangles = floatArrayOf(
                // Triangle 1
                -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,

                // Triangle 2
                -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f,

                // Line 1
                -0.5f, 0f, 0.5f, 0f,

                // Mallets
                0f, -0.25f, 0f, 0.25f)

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()

        vertexData?.put(tableVerticesWithTriangles)
    }


    override fun onDrawFrame(p0: GL10?) {
        // Clear the rendering surface.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Draw the table.
        GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        // Draw the center dividing line.
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        // Draw the first mallet blue.
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        // Draw the second mallet red.
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);
    }

    override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {

    }

    override fun onSurfaceCreated(gl: GL10?, config: javax.microedition.khronos.egl.EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        var vertexShaderSource = ""
        var fragmentShaderSource = ""

        context?.let {
            vertexShaderSource = TextResourceReader
                    .readTextFileFromResource(context as Context, R.raw.simple_vertex_shader)


            fragmentShaderSource = TextResourceReader
                    .readTextFileFromResource(context as Context, R.raw.simple_fragment_shader)
        }

        val vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource)

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)

        ShaderHelper.validateProgram(program)

        glUseProgram(program)

        uColorLocation = glGetUniformLocation(program, U_COLOR)

        aPositionLocation = glGetAttribLocation(program, A_POSITION)
        Log.d("readTextFileFromResource", "uColorLocation $uColorLocation")
        Log.d("readTextFileFromResource", "aPositionLocation $aPositionLocation")

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData?.position(0)
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL10.GL_FLOAT,
                false, 0, vertexData)

        glEnableVertexAttribArray(aPositionLocation)

    }

    companion object {
        const val POSITION_COMPONENT_COUNT = 2

    }
}