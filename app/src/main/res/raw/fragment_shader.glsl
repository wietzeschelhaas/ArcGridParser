#version 300 es

precision mediump float;

in vec4 diffuseColorTerm;

out vec4 fragColor;

void main()
{
    //fragColor = vec4(0.0f,0.0f,0.0f,1.0f);
    fragColor = diffuseColorTerm;
}