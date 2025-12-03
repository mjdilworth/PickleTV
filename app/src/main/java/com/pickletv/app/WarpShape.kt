package com.pickletv.app

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Represents a trapezoid warp shape for keystone correction
 * Stores X,Y coordinates for each of the 4 corners
 */
data class WarpShape(
    val topLeftX: Float = 0f,       // Top-left X offset
    val topLeftY: Float = 0f,       // Top-left Y offset
    val topRightX: Float = 0f,      // Top-right X offset
    val topRightY: Float = 0f,      // Top-right Y offset
    val bottomLeftX: Float = 0f,    // Bottom-left X offset
    val bottomLeftY: Float = 0f,    // Bottom-left Y offset
    val bottomRightX: Float = 0f,   // Bottom-right X offset
    val bottomRightY: Float = 0f    // Bottom-right Y offset
)

/**
 * Manages saving and loading warp shape preferences
 */
class WarpShapeManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("warp_shape_prefs", Context.MODE_PRIVATE)

    fun saveWarpShape(warpShape: WarpShape) {
        preferences.edit().apply {
            putFloat("topLeftX", warpShape.topLeftX)
            putFloat("topLeftY", warpShape.topLeftY)
            putFloat("topRightX", warpShape.topRightX)
            putFloat("topRightY", warpShape.topRightY)
            putFloat("bottomLeftX", warpShape.bottomLeftX)
            putFloat("bottomLeftY", warpShape.bottomLeftY)
            putFloat("bottomRightX", warpShape.bottomRightX)
            putFloat("bottomRightY", warpShape.bottomRightY)
            apply()
        }
        Log.d("WarpShapeManager", "Saved warp shape: $warpShape")
    }

    fun loadWarpShape(): WarpShape {
        return WarpShape(
            topLeftX = preferences.getFloat("topLeftX", 0f),
            topLeftY = preferences.getFloat("topLeftY", 0f),
            topRightX = preferences.getFloat("topRightX", 0f),
            topRightY = preferences.getFloat("topRightY", 0f),
            bottomLeftX = preferences.getFloat("bottomLeftX", 0f),
            bottomLeftY = preferences.getFloat("bottomLeftY", 0f),
            bottomRightX = preferences.getFloat("bottomRightX", 0f),
            bottomRightY = preferences.getFloat("bottomRightY", 0f)
        ).also {
            Log.d("WarpShapeManager", "Loaded warp shape: $it")
        }
    }

    fun resetWarpShape() {
        preferences.edit().clear().apply()
    }
}

