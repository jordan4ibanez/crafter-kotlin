#version 410 core

in vec2 outputTextureCoordinate;
in vec4 newColoring;

out vec4 fragColor;

uniform sampler2D textureSampler;

void main() {
  vec4 textureColor = texture(textureSampler, outputTextureCoordinate);

  fragColor = textureColor;// * newColoring;

}