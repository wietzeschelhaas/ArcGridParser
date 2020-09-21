#version 300 es

in vec4 vPosition;
in vec4 vNormal;
in vec2 vTexCoord;

uniform float maxHeight;


uniform mat4 uMVPMatrix;
uniform vec4 vLightPosition;
uniform vec4 vLightColorDf;
uniform vec4 vMaterialColorDf;

out vec4 diffuseColorTerm;

vec4 diffuseTerm (vec4 Ld, vec4 Kd, vec4 nv, vec4 lv)
{
    return Ld * Kd * max(0.0f, dot(nv, lv));
}

void main()
{
    vec4 transformedPos = uMVPMatrix * vPosition;
    vec4 transformedNormal =normalize (uMVPMatrix * vNormal);
    vec4 transformedLightPos = uMVPMatrix * vLightPosition;
    vec4 lVec = normalize (transformedLightPos - transformedPos);





    //divide the height value of this vertex with the maxHeight found in the raster file.
    //this will create a color that will be whiter if the height is higher.
    diffuseColorTerm = vec4(vPosition.z/maxHeight,vPosition.z/maxHeight,vPosition.z/maxHeight,1.0);
    //diffuseColorTerm = diffuseTerm (vLightColorDf, vMaterialColorDf, transformedNormal, lVec);

   gl_Position = transformedPos;
}