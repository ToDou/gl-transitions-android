precision mediump float; 
      	 				
uniform sampler2D u_TextureUnit0;
varying vec2 v_TextureCoordinates;

void main()                    		
{
    gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
}