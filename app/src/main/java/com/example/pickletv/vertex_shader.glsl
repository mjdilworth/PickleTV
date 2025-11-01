// Vertex shader for trapezoid warp correction
attribute vec4 position;
attribute vec2 texCoord;

varying vec2 outTexCoord;

uniform mat4 projection;
uniform float topLeft;
uniform float topRight;
uniform float bottomLeft;
uniform float bottomRight;

void main() {
    // Apply trapezoid warp by adjusting vertex positions
    vec4 warpedPos = position;

    // Normalize coordinates to -1..1 range
    float normalizedX = position.x;
    float normalizedY = position.y;

    // Interpolate warp offset based on Y position
    float normalizedY01 = (normalizedY + 1.0) / 2.0; // Convert to 0..1

    // Interpolate between top and bottom offsets
    float leftOffset = mix(topLeft, bottomLeft, normalizedY01);
    float rightOffset = mix(topRight, bottomRight, normalizedY01);

    // Interpolate between left and right offsets based on X position
    float normalizedX01 = (normalizedX + 1.0) / 2.0; // Convert to 0..1
    float xOffset = mix(leftOffset, rightOffset, normalizedX01);

    warpedPos.x = position.x + xOffset;

    gl_Position = projection * warpedPos;
    outTexCoord = texCoord;
}

