#version 450

layout(location = 0) out vec4 pixelColor;

in vec2 vTextureCoordinates;
in vec3 vNormal;

uniform sampler2D uTexture;

void main()
{
	vec3 textureColor = texture(uTexture, vTextureCoordinates).xyz;
	pixelColor = vec4(textureColor, 1.0);
}