package com.dilworth.dilmap

enum class Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    fun next(): Corner = when (this) {
        TOP_LEFT -> TOP_RIGHT
        TOP_RIGHT -> BOTTOM_RIGHT
        BOTTOM_RIGHT -> BOTTOM_LEFT
        BOTTOM_LEFT -> TOP_LEFT
    }
}

