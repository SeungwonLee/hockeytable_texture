package com.example.seungwon.hockytable.texture

import android.opengl.GLES20
import com.example.seungwon.hockytable.texture.VertexArray.Companion.BYTES_PER_FLOAT

class Mallet {
    private val vertexArray: VertexArray

    init {
        vertexArray = VertexArray(VERTEX_DATA)
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
                0,
                colorProgram.positionAttributeLocation,
                POSITION_COMPONENT_COUNT,
                STRIDE)
        vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                colorProgram.colorAttributeLocation,
                COLOR_COMPONENT_COUNT,
                STRIDE)
    }

    fun draw() {
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 2)
    }

    companion object {
        private val POSITION_COMPONENT_COUNT = 2
        private val COLOR_COMPONENT_COUNT = 3
        private val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT
        private val VERTEX_DATA = floatArrayOf(
                // Order of coordinates: X, Y, R, G, B
                0f, -0.4f, 0f, 0f, 1f, 0f, 0.4f, 1f, 0f, 0f)
    }
}
