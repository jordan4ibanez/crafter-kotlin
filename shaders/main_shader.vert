#version 410 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinate;
layout (location = 2) in vec4 color;

out vec2 outputTextureCoordinate;
out vec4 newColoring;

uniform mat4 cameraMatrix;
uniform mat4 objectMatrix;
uniform bool hasColor;
uniform bool hasBones;

void main() {
  // Position in world without camera matrix application
  vec4 objectPosition = vec4(position,1.0);

  // Position in world relative to camera
  vec4 cameraPosition = objectMatrix * objectPosition;

  // Output real coordinates into gpu
  gl_Position = cameraMatrix * cameraPosition;

  outputTextureCoordinate = textureCoordinate;

  if (hasColor) {
    newColoring = color;
  }
}