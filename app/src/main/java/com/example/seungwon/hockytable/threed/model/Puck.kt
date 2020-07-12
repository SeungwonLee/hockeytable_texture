package com.example.seungwon.hockytable.threed.model

import com.example.seungwon.hockytable.texture.VertexArray
import com.example.seungwon.hockytable.threed.ColorShaderProgram
import com.example.seungwon.hockytable.threed.model.ObjectBuilder.Companion.DrawCommand
import com.example.seungwon.hockytable.utils.Geometry
import com.example.seungwon.hockytable.utils.Geometry.Cylinder


class Puck(radius: Float, height: Float, numPointsAroundPuck: Int) {
    val height: Float

    val radius: Float
    private val vertexArray: VertexArray
    private val drawList: List<DrawCommand>

    init {
        val (vertexData, localDrawList) =
            ObjectBuilder.createPuck(
                Cylinder(
                    Geometry.Point(0f, 0f, 0f), radius, height
                ), numPointsAroundPuck
            )
        this.radius = radius
        this.height = height
        vertexArray = VertexArray(vertexData)
        drawList = localDrawList
    }

    fun bindData(colorProgram: ColorShaderProgram) {
        vertexArray.setVertexAttribPointer(
            0,
            colorProgram.positionAttributeLocation,
            POSITION_COMPONENT_COUNT, 0
        )
    }

    fun draw() {
        for (drawCommand in drawList) {
            drawCommand.draw()
        }
    }

    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }

}