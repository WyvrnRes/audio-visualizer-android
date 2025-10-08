/*
        Copyright 2018 Gaurav Kumar

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.gauravk.audiovisualizer.utils

import android.graphics.PointF

class BezierSpline(size: Int) {

    private val nSize: Int = size - 1
    val firstControlPoints: Array<PointF> = Array(nSize) { PointF() }
    val secondControlPoints: Array<PointF> = Array(nSize) { PointF() }

    /**
     * Get open-ended bezier spline control points.
     *
     * @param knots bezier spline points
     * @throws IllegalArgumentException if less than two knots are passed.
     */
    fun updateCurveControlPoints(knots: Array<PointF>) {
        require(knots.size >= 2) { "At least two knot points are required" }

        val n = knots.size - 1

        // Special case: bezier curve should be a straight line
        if (n == 1) {
            // 3P1 = 2P0 + P3
            var x = (2 * knots[0].x + knots[1].x) / 3
            var y = (2 * knots[0].y + knots[1].y) / 3

            firstControlPoints[0].x = x
            firstControlPoints[0].y = y

            // P2 = 2P1 - P0
            x = 2 * firstControlPoints[0].x - knots[0].x
            y = 2 * firstControlPoints[0].y - knots[0].y

            secondControlPoints[0].x = x
            secondControlPoints[0].y = y

        } else {

            // Calculate first bezier control points
            // Right hand side vector
            val rhs = FloatArray(n)

            // Set right hand side X values
            for (i in 1 until n - 1) {
                rhs[i] = 4 * knots[i].x + 2 * knots[i + 1].x
            }
            rhs[0] = knots[0].x + 2 * knots[1].x
            rhs[n - 1] = (8 * knots[n - 1].x + knots[n].x) / 2f

            // Get first control points X-values
            val x = getFirstControlPoints(rhs)

            // Set right hand side Y values
            for (i in 1 until n - 1) {
                rhs[i] = 4 * knots[i].y + 2 * knots[i + 1].y
            }
            rhs[0] = knots[0].y + 2 * knots[1].y
            rhs[n - 1] = (8 * knots[n - 1].y + knots[n].y) / 2f

            // Get first control points Y-values
            val y = getFirstControlPoints(rhs)

            for (i in 0 until n) {
                // First control point
                firstControlPoints[i].x = x[i]
                firstControlPoints[i].y = y[i]

                // Second control point
                if (i < n - 1) {
                    val xx = 2 * knots[i + 1].x - x[i + 1]
                    val yy = 2 * knots[i + 1].y - y[i + 1]
                    secondControlPoints[i].x = xx
                    secondControlPoints[i].y = yy
                } else {
                    val xx = (knots[n].x + x[n - 1]) / 2
                    val yy = (knots[n].y + y[n - 1]) / 2
                    secondControlPoints[i].x = xx
                    secondControlPoints[i].y = yy
                }
            }
        }
    }

    /**
     * Solves a tridiagonal system for one of coordinates (x or y) of first
     * bezier control points.
     *
     * @param rhs right hand side vector.
     * @return Solution vector.
     */
    private fun getFirstControlPoints(rhs: FloatArray): FloatArray {
        val n = rhs.size
        val x = FloatArray(n) // Solution vector
        val tmp = FloatArray(n) // Temp workspace

        var b = 2.0f
        x[0] = rhs[0] / b

        // Decomposition and forward substitution
        for (i in 1 until n) {
            tmp[i] = 1 / b
            b = (if (i < n - 1) 4.0f else 3.5f) - tmp[i]
            x[i] = (rhs[i] - x[i - 1]) / b
        }

        // Backsubstitution
        for (i in 1 until n) {
            x[n - i - 1] -= tmp[n - i] * x[n - i]
        }

        return x
    }
}
