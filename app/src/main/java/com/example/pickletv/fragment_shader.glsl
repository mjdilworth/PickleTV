// Fragment shader for video rendering
precision mediump float;

varying vec2 outTexCoord;

uniform sampler2D videoTexture;

void main() {
    gl_FragColor = texture2D(videoTexture, outTexCoord);
}

