package com.example.seungwon.hockytable.threed

import android.content.Context
import android.opengl.GLES20
import com.example.seungwon.hockytable.R
import com.example.seungwon.hockytable.texture.ShaderProgram

class ColorShaderProgram(context: Context) :
    ShaderProgram(context, R.raw.simple_vertex_shader_matrix, R.raw.simple_fragment_shader) {
    // Uniform locations
    // Retrieve uniform locations for the shader program.
    private val uMatrixLocation: Int = GLES20.glGetUniformLocation(program, U_MATRIX)
    private val uColorLocation: Int = GLES20.glGetUniformLocation(program, U_COLOR)

    // Attribute locations
    // Retrieve attribute locations for the shader program.
    val positionAttributeLocation: Int = GLES20.glGetAttribLocation(program, A_POSITION)
    val colorAttributeLocation: Int = GLES20.glGetAttribLocation(program, A_COLOR)

    fun setUniforms(matrix: FloatArray, r: Float, g: Float, b: Float) {
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
        GLES20.glUniform4f(uColorLocation, r, g, b, 1f);
    }
}
