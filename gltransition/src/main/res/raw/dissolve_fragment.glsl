precision mediump float; 
      	 				
uniform sampler2D u_TextureUnit0;
uniform sampler2D u_TextureUnit1;
varying vec2 v_TextureCoordinates;

uniform float u_Progress;

void main()                    		
{
    vec4 fadeOutColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
    vec4 fadeInColor = texture2D(u_TextureUnit1, v_TextureCoordinates);
    gl_FragColor = fadeOutColor+(fadeInColor-fadeOutColor) * u_Progress;
}