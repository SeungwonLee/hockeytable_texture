package com.example.seungwon.hockytable.texture

import android.content.Context
import android.opengl.GLES20.glUseProgram
import com.example.seungwon.hockytable.utils.ShaderHelper
import com.example.seungwon.hockytable.utils.TextResourceReader

open class ShaderProgram(context: Context, vertexShaderResourceId: Int,
                         fragmentShaderResourceId: Int) {
    // Uniform constants
    protected val U_MATRIX = "u_Matrix"
    protected val U_COLOR = "u_Color"
    protected val U_TEXTURE_UNIT = "u_TextureUnit"

    // Attribute constants
    protected val A_POSITION = "a_Position"
    protected val A_COLOR = "a_Color"
    protected val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

    // Shader program
    protected var program: Int = 0

    init {
        // Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId))
    }

    fun useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program)
    }
}