precision mediump float; 
      	 				
uniform sampler2D u_TextureUnit0;
uniform sampler2D u_TextureUnit1;
varying vec2 v_TextureCoordinates;

uniform float u_Progress;

void main()                    		
{
    vec4 col;
    float p;

    if(u_Progress < 0.5) {
        col = texture2D(u_TextureUnit0, v_TextureCoordinates);
        p = (0.5 - u_Progress) / 0.5;
    } else {
        col =  texture2D(u_TextureUnit1, v_TextureCoordinates);
        p = (u_Progress - 0.5) / 0.5;
    }
    gl_FragColor = vec4(col.rgb, p);
}