#version 450

layout(location = 0) out vec4 pixelColor;

in vec2 vTextureCoordinates;
in vec3 vNormal;

uniform sampler2D uTexture;
uniform vec3 uColor;

void main()
{
	vec3 textureColor = texture(uTexture, vTextureCoordinates).xyz * uColor;
	pixelColor = vec4(textureColor, 1.0);
}