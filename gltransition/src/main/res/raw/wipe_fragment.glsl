precision mediump float; 
      	 				
uniform sampler2D u_TextureUnit0;
uniform sampler2D u_TextureUnit1;
varying vec2 v_TextureCoordinates;

uniform float u_Progress;
uniform int u_Direction;

void main()                    		
{
    if(u_Direction == 0)
    {
       	if(v_TextureCoordinates.y < (1.0 - u_Progress))
       	    gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
       	else
           	gl_FragColor = texture2D(u_TextureUnit1, v_TextureCoordinates);
    }
    else if(u_Direction == 1)
    {
       	if(v_TextureCoordinates.y < u_Progress)
       	    gl_FragColor = texture2D(u_TextureUnit1, v_TextureCoordinates);
       	else
           	gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
       }
    else if(u_Direction == 2)
    {
       	if(v_TextureCoordinates.x < (1.0 - u_Progress))
       	    gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
       	else
           	gl_FragColor = texture2D(u_TextureUnit1, v_TextureCoordinates);
    }
    else if(u_Direction == 3)
    {
       	if(v_TextureCoordinates.x < u_Progress)
       	    gl_FragColor = texture2D(u_TextureUnit1, v_TextureCoordinates);
       	else
           	gl_FragColor = texture2D(u_TextureUnit0, v_TextureCoordinates);
    }
}