package com.example.mixon.lagrangepolynomes;

import android.opengl.GLES20;

/**
 * Created by Mixon on 12.04.2015.
 */
public class Shader
{
    public Shader()
    {
        _handle = GLES20.glCreateProgram();
        if (_handle == 0)
        {
            throw new RuntimeException(GLES20.glGetProgramInfoLog(_handle));
        }
    }
    public void compile(final int type, final String source)
    {
        if (type != GLES20.GL_FRAGMENT_SHADER && type != GLES20.GL_VERTEX_SHADER)
        {
            throw new RuntimeException("Incorrect shader type!");
        }

        int shaderHandle = GLES20.glCreateShader(type);

        if (shaderHandle == 0)
        {
            throw new RuntimeException("Error creating shader!");
        }

        GLES20.glShaderSource(shaderHandle, source);
        GLES20.glCompileShader(shaderHandle);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0)
        {
            GLES20.glDeleteShader(shaderHandle);
            throw new RuntimeException(GLES20.glGetShaderInfoLog(shaderHandle));
        }

        GLES20.glAttachShader(_handle, shaderHandle);
        GLES20.glDeleteShader(shaderHandle);
    }
    public void link()
    {
        GLES20.glLinkProgram(_handle);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(_handle, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0)
        {
            GLES20.glDeleteProgram(_handle);
            _handle = 0;
            throw new RuntimeException(GLES20.glGetProgramInfoLog(_handle));
        }
    }
    public int getAttributeLocation(final String name)
    {
        int t = GLES20.glGetAttribLocation(_handle, name);
        if (t != -1)
            return t;
        throw new RuntimeException("The attribute \"" + name + "\" was not found!");
    }
    public int getUniformLocation(final String name)
    {
        int t = GLES20.glGetUniformLocation(_handle, name);
        if (t != -1)
            return t;
        throw new RuntimeException("The uniform variable \"" + name + "\" was not found!");
    }
    public void use()
    {
        GLES20.glUseProgram(_handle);
    }

    private int _handle;
}
