package com.example.pickletv

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Represents a trapezoid warp shape for keystone correction
 */
data class WarpShape(
    val topLeft: Float = 0f,      // Top-left corner offset (horizontal)
    val topRight: Float = 0f,     // Top-right corner offset (horizontal)
    val bottomLeft: Float = 0f,   // Bottom-left corner offset (horizontal)
    val bottomRight: Float = 0f   // Bottom-right corner offset (horizontal)
)

/**
 * Manages saving and loading warp shape preferences
 */
class WarpShapeManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("warp_shape_prefs", Context.MODE_PRIVATE)

    fun saveWarpShape(warpShape: WarpShape) {
        preferences.edit().apply {
            putFloat("topLeft", warpShape.topLeft)
            putFloat("topRight", warpShape.topRight)
            putFloat("bottomLeft", warpShape.bottomLeft)
            putFloat("bottomRight", warpShape.bottomRight)
            apply()
        }
        Log.d("WarpShapeManager", "Saved warp shape: $warpShape")
    }

    fun loadWarpShape(): WarpShape {
        return WarpShape(
            topLeft = preferences.getFloat("topLeft", 0f),
            topRight = preferences.getFloat("topRight", 0f),
            bottomLeft = preferences.getFloat("bottomLeft", 0f),
            bottomRight = preferences.getFloat("bottomRight", 0f)
        ).also {
            Log.d("WarpShapeManager", "Loaded warp shape: $it")
        }
    }

    fun resetWarpShape() {
        preferences.edit().clear().apply()
    }
}

