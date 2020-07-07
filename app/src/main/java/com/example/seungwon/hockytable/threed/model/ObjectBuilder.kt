package com.example.seungwon.hockytable.threed.model

import android.opengl.GLES20
import com.example.seungwon.hockytable.utils.Geometry
import com.example.seungwon.hockytable.utils.Geometry.Circle
import com.example.seungwon.hockytable.utils.Geometry.Cylinder
import kotlin.math.cos
import kotlin.math.sin


class ObjectBuilder(sizeInVertices: Int) {
    private val vertexData: FloatArray = FloatArray(sizeInVertices * FLOATS_PER_VERTEX)
    private var offset = 0
    private val drawList: ArrayList<DrawCommand> = ArrayList()

    private fun appendCircle(circle: Circle, numPoints: Int) {
        val startVertex = offset / FLOATS_PER_VERTEX
        val numVertices = sizeOfCircleInVertices(numPoints)

        // Center point of fan
        vertexData[offset++] = circle.center.x
        vertexData[offset++] = circle.center.y
        vertexData[offset++] = circle.center.z

        // Fan around center point. <= is used because we want to generate
        // the point at the starting angle twice to complete the fan.
        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()
                    * (Math.PI.toFloat() * 2f))

            vertexData[offset++] = (circle.center.x
                    + circle.radius * cos(angleInRadians))
            vertexData[offset++] = circle.center.y
            vertexData[offset++] = (circle.center.z
                    + circle.radius * sin(angleInRadians))
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, startVertex, numVertices)
            }
        })
    }

    private fun appendOpenCylinder(cylinder: Cylinder, numPoints: Int) {
        val startVertex = offset / FLOATS_PER_VERTEX
        val numVertices = sizeOfOpenCylinderInVertices(numPoints)
        val yStart = cylinder.center.y - cylinder.height / 2f
        val yEnd = cylinder.center.y + cylinder.height / 2f

        // Generate strip around center point. <= is used because we want to
        // generate the points at the starting angle twice, to complete the
        // strip.
        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()
                    * (Math.PI.toFloat() * 2f)).toDouble()
            val xPosition: Float = ((cylinder.center.x
                    + cylinder.radius * cos(angleInRadians)).toFloat())
            val zPosition: Float = ((cylinder.center.z
                    + cylinder.radius * sin(angleInRadians)).toFloat())
            vertexData[offset++] = xPosition
            vertexData[offset++] = yStart
            vertexData[offset++] = zPosition
            vertexData[offset++] = xPosition
            vertexData[offset++] = yEnd
            vertexData[offset++] = zPosition
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices)
            }
        })
    }

    private fun appendOpenCylinder2(cylinder: Cylinder, numPoints: Int) {
        val startVertex = offset / FLOATS_PER_VERTEX
        val numVertices = sizeOfOpenCylinderInVertices(numPoints)
        val yStart = cylinder.center.y - cylinder.height / 2f
        val yEnd = cylinder.center.y + cylinder.height / 2f

        // Generate strip around center point. <= is used because we want to
        // generate the points at the starting angle twice, to complete the
        // strip.
        for (i in 0..numPoints) {
            val angleInRadians = (i.toFloat() / numPoints.toFloat()
                    * (Math.PI.toFloat() * 2f)).toDouble()
            val xPosition: Float = ((cylinder.center.x
                    + cylinder.radius * cos(angleInRadians)).toFloat())
            val zPosition: Float = ((cylinder.center.z
                    + cylinder.radius * sin(angleInRadians)).toFloat())
            vertexData[offset++] = xPosition
            vertexData[offset++] = yStart
            vertexData[offset++] = zPosition
            vertexData[offset++] = xPosition
            vertexData[offset++] = yEnd
            vertexData[offset++] = zPosition
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertices)
            }
        })
    }

    private fun build(): GeneratedData = GeneratedData(vertexData, drawList)

    data class GeneratedData(
        val vertexData: FloatArray,
        val drawList: List<DrawCommand>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as GeneratedData

            if (!vertexData.contentEquals(other.vertexData)) return false
            if (drawList != other.drawList) return false

            return true
        }

        override fun hashCode(): Int {
            var result = vertexData.contentHashCode()
            result = 31 * result + drawList.hashCode()
            return result
        }
    }

    companion object {
        private const val FLOATS_PER_VERTEX: Int = 3

        fun sizeOfCircleInVertices(numPoints: Int): Int = 1 + (numPoints + 1)

        fun sizeOfOpenCylinderInVertices(numPoints: Int): Int = (numPoints + 1) * 2

        fun createPuck(puck: Geometry.Cylinder, numPoints: Int): GeneratedData { // cylinder
            val size: Int = (sizeOfCircleInVertices(numPoints)
                    + sizeOfOpenCylinderInVertices(numPoints))
            val builder = ObjectBuilder(size)
            val puckTop = Circle(puck.center.translateY(puck.height / 2f), puck.radius)
            builder.appendCircle(puckTop, numPoints)
            builder.appendOpenCylinder2(puck, numPoints)
            return builder.build()
        }

        fun createMallet(
            center: Geometry.Point,
            radius: Float,
            height: Float,
            numPoints: Int
        ): GeneratedData {
            val size = (sizeOfCircleInVertices(numPoints) * 2
                    + sizeOfOpenCylinderInVertices(numPoints) * 2)

            val builder = ObjectBuilder(size)

            // First, generate the mallet base.
            val baseHeight: Float = height * 0.25f

            val baseCircle = Circle(
                center.translateY(-baseHeight),
                radius.toFloat()
            )
            val baseCylinder = Cylinder(
                baseCircle.center.translateY(-baseHeight / 2f),
                radius.toFloat(), baseHeight
            )

            builder.appendCircle(baseCircle, numPoints)
            builder.appendOpenCylinder(baseCylinder, numPoints)

            // Now generate the mallet handle.

            // Now generate the mallet handle.
            val handleHeight: Float = height * 0.75f
            val handleRadius = radius / 3f

            val handleCircle = Circle(
                center.translateY(height * 0.5f),
                handleRadius
            )
            val handleCylinder = Cylinder(
                handleCircle.center.translateY(-handleHeight / 2f),
                handleRadius, handleHeight
            )

            builder.appendCircle(handleCircle, numPoints)
            builder.appendOpenCylinder(handleCylinder, numPoints)

            return builder.build()
        }

        interface DrawCommand {
            fun draw()
        }
    }
}