package com.example.seungwon.hockytable.threed.model

import com.example.seungwon.hockytable.texture.VertexArray
import com.example.seungwon.hockytable.threed.ColorShaderProgram
import com.example.seungwon.hockytable.threed.model.ObjectBuilder.Companion.DrawCommand
import com.example.seungwon.hockytable.utils.Geometry


class Mallet(
    radius: Float,
    height: Float,
    numPointsAroundMallet: Int
) {
    val radius = 0f
    val height = 0f

    private var vertexArray: VertexArray? = null
    private var drawList: List<DrawCommand>? = null

    init {
        val (vertexData, localDrawList) = ObjectBuilder.createMallet(
            Geometry.Point(
                0f,
                0f, 0f
            ), radius, height, numPointsAroundMallet
        )
        vertexArray = VertexArray(vertexData)
        drawList = localDrawList
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray?.setVertexAttribPointer(
            0,
            colorProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT, 0
        )
    }

    fun draw() {
        for (drawCommand in drawList!!) {
            drawCommand.draw()
        }
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }
}