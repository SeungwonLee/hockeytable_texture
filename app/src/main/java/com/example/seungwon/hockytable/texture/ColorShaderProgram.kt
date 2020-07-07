package com.example.seungwon.hockytable.texture

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import com.example.seungwon.hockytable.R


class ColorShaderProgram(context: Context) :
    ShaderProgram(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader) {
    // Uniform locations
    // Retrieve uniform locations for the shader program.
    private val uMatrixLocation: Int = glGetUniformLocation(program, U_MATRIX)

    // Attribute locations
    // Retrieve attribute locations for the shader program.
    val positionAttributeLocation: Int = glGetAttribLocation(program, A_POSITION)
    val colorAttributeLocation: Int = glGetAttribLocation(program, A_COLOR)

    fun setUniforms(matrix: FloatArray) {
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}