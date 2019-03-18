package com.example.seungwon.hockytable.texture

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import com.example.seungwon.hockytable.R
import com.example.seungwon.hockytable.R.raw.simple_fragment_shader
import com.example.seungwon.hockytable.R.raw.simple_vertex_shader


class ColorShaderProgram(context: Context)
    : ShaderProgram(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader) {
    // Uniform locations
    private val uMatrixLocation: Int

    // Attribute locations
    val positionAttributeLocation: Int
    val colorAttributeLocation: Int

    init {
        // Retrieve uniform locations for the shader program.
        uMatrixLocation = glGetUniformLocation(program, U_MATRIX)
        // Retrieve attribute locations for the shader program.
        positionAttributeLocation = glGetAttribLocation(program, A_POSITION)
        colorAttributeLocation = glGetAttribLocation(program, A_COLOR)
    }

    fun setUniforms(matrix: FloatArray) {
        // Pass the matrix into the shader program.
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0)
    }
}