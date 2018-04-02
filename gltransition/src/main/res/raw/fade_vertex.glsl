attribute vec4 a_Position;
attribute vec2 a_TextureCoordinates;

uniform mat4 u_MMatrix;
uniform mat4 u_VMatrix;
uniform mat4 u_PMatrix;

varying vec2 v_TextureCoordinates;

void main()                    
{                            
    v_TextureCoordinates = a_TextureCoordinates;	  	  
    gl_Position = u_PMatrix * u_VMatrix * u_MMatrix * a_Position;
}          