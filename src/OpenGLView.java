package com.example.mixon.lagrangepolynomes;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Mixon on 31.03.2015.
 */
public class OpenGLView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private int mBytesPerFloat = 4;

    private int mGridCoordsHandle;
    private int mPointCoordsHandle;

    private float pointRadius = 0.02f;
    private int countCircleNodes = 16;

    private ArrayList<Integer> mGraphicCoordsHandle = new ArrayList<>();

    private float wmax;
    private float hmax;
    private int countNodes;

    private int mPositionHandle;
    private int mColorHandle;

    private int mMVPMatrixHandle;

    private int countInterval = 5;

    private float[] mProjectionMatrix = new float[16];
    private float[] mGridModelMatrix = new float[16];
    private float[] mGraphicModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mHelpMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private boolean changeMatrix = true;

    private OpenGLViewListener listener = null;

    public OpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
    }

    public int getCountInterval() {
        return countInterval;
    }

    public void setListener(OpenGLViewListener lstnr)
    {
        listener = lstnr;
    }

    private void setShader()
    {
        String vertexShaderCode =
                        "uniform mat4 uMVPMatrix;" +
                        "uniform vec4 uColor;" +
                        "attribute vec4 aPosition;" +
                        "varying   vec4 vColor;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * aPosition;" +
                        "  vColor = uColor;" +
                        "}";
        String fragmentShaderCode =
                        "precision mediump float;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        Shader program = new Shader();
        program.compile(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        program.compile(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        program.link();

        mMVPMatrixHandle = program.getUniformLocation("uMVPMatrix");
        mPositionHandle = program.getAttributeLocation("aPosition");
        mColorHandle = program.getUniformLocation("uColor");

        program.use();
    }

    private int arrayToVBO(float[] array)
    {
        int[] v = new int[1];

        ByteBuffer bb = ByteBuffer.allocateDirect(array.length * mBytesPerFloat);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();

        fb.put(array);
        fb.position(0);

        GLES20.glGenBuffers(1, v, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, v[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mBytesPerFloat * array.length, fb, GLES20.GL_STATIC_DRAW);

        return v[0];
    }

    private void setGridVbo()
    {
        int i;
        float[] gridCoords = new float[8 * (countInterval - 1)];

        for (i = 0; i < countInterval - 1; ++i)
        {
            gridCoords[8 * i] = gridCoords[8 * i + 5] = 0.0f;
            gridCoords[8 * i + 2] = gridCoords[8 * i + 7] = 1.0f;
            gridCoords[8 * i + 1] = gridCoords[8 * i + 3] = gridCoords[8 * i + 4] = gridCoords[8 * i + 6] = (float)(i + 1) / countInterval;
        }

        mGridCoordsHandle = arrayToVBO(gridCoords);
    }

    private void setPointVBO()
    {
        int i;
        float[] circleCoords = new float[2 * (countCircleNodes + 2)];

        circleCoords[0] = 0.0f;
        circleCoords[1] = 0.0f;

        for (i = 1; i < countCircleNodes + 2; ++i)
        {
            circleCoords[2 * i] = (float) Math.cos(2 * Math.PI * i / countCircleNodes);
            circleCoords[2 * i + 1] = (float) Math.sin(2 * Math.PI * i / countCircleNodes);
        }

        mPointCoordsHandle = arrayToVBO(circleCoords);
    }

    public void setGraphic(ICalculable f, float left, float right)
    {
        int num = countNodes;
        int i;

        float[] coords = new float[2 * (num + 1)];

        for (i = 0; i <= num; ++i)
        {
            coords[2 * i] = left + (float) i * (right - left) / (float) num;
            coords[2 * i + 1] = (float) f.calculate(coords[2 * i]);

            if (coords[2 * i + 1] > Model.Max) Model.Max = coords[2 * i + 1];
            if (coords[2 * i + 1] < Model.Min) Model.Min = coords[2 * i + 1];
        }

        mGraphicCoordsHandle.add(arrayToVBO(coords));
    }

    private void freeSource()
    {
        int[] buf = new int[mGraphicCoordsHandle.size()];
        int i;
        for (i = 0; i < mGraphicCoordsHandle.size(); ++i)
        {
            buf[i] = mGraphicCoordsHandle.get(i);
        }
        GLES20.glDeleteBuffers(mGraphicCoordsHandle.size(), buf, 0);
        mGraphicCoordsHandle.clear();
    }

    private void setCamera()
    {
        float eyeX = 0.0f, eyeY = 0.0f, eyeZ = 1.0625f,
              lookX = 0.0f, lookY = 0.0f, lookZ = -5.0f,
              upX = 0.0f, upY = 1.0f, upZ = 0.0f;
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
    }

    private void makeMVPMatrix(float[] modelMatrix)
    {
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
    }

    private void sendMVPMatrix()
    {
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
    }

    private void drawVbo(int coordsHandle, int count, int type)
    {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, coordsHandle);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glDrawArrays(type, 0, count);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private void makeModelMatrix()
    {
        Matrix.setIdentityM(mGraphicModelMatrix, 0);
        mGraphicModelMatrix[0] = 2 * wmax / (Model.Right - Model.Left);
        mGraphicModelMatrix[5] = 2 * hmax / (Model.Max - Model.Min);
        mGraphicModelMatrix[12] = wmax - mGraphicModelMatrix[0] * Model.Right;
        mGraphicModelMatrix[13] = hmax - mGraphicModelMatrix[5] * Model.Max;
        if (listener != null)
        {
            listener.rescalingEvent(this);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        setShader();
        setGridVbo();
        setPointVBO();
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        setCamera();
    }



    @Override
    public void onDrawFrame(GL10 gl)
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        makeMVPMatrix(mGridModelMatrix);
        sendMVPMatrix();
        GLES20.glUniform4f(mColorHandle, 0.8f, 0.8f, 0.8f, 1.0f);
        drawVbo(mGridCoordsHandle, 4 * (countInterval - 1), GLES20.GL_LINES);

        if (Model.state == Model.State.ADD)
        {
            Graphic g = Model.graphicList.get(Model.graphicList.size() - 1);
            setGraphic(g.f, g.leftBorder, g.rightBorder);
            changeMatrix = true;
            Model.state = Model.State.STATIC;
        }
        else if (Model.state == Model.State.REMOVE)
        {
            try {
                freeSource();
                Model.state = Model.State.NONE;
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
            }
        }

        if (changeMatrix)
        {
            makeModelMatrix();
        }

        int i, j, n = Model.graphicList.size();
        if (n > 0)
        {
            makeMVPMatrix(mGraphicModelMatrix);
            sendMVPMatrix();
            for (i = 0; i < n; ++i)
            {
                ArrayList<Float> color = Model.graphicList.get(i).color;
                GLES20.glUniform4f(mColorHandle, color.get(0), color.get(1), color.get(2), 1.0f);
                drawVbo(mGraphicCoordsHandle.get(i), countNodes + 1, GLES20.GL_LINE_STRIP);
            }
        }

        n = Model.pointCollectionList.size();
        if (n > 0)
        {
            for (i = 0; i < n; ++i)
            {
                for (j = 0; j < Model.pointCollectionList.get(i).absciss.size(); ++j)
                {
                    Matrix.setIdentityM(mHelpMatrix, 0);
                    mHelpMatrix[0] = pointRadius / mGraphicModelMatrix[0];
                    mHelpMatrix[5] = pointRadius / mGraphicModelMatrix[5];
                    mHelpMatrix[12] = Model.pointCollectionList.get(i).absciss.get(j);
                    mHelpMatrix[13] = Model.pointCollectionList.get(i).ordinat.get(j);
                    Matrix.multiplyMM(mHelpMatrix, 0, mGraphicModelMatrix, 0, mHelpMatrix, 0);
                    makeMVPMatrix(mHelpMatrix);
                    sendMVPMatrix();
                    GLES20.glUniform4f(mColorHandle, Model.pointCollectionList.get(i).color.get(0), Model.pointCollectionList.get(i).color.get(1), Model.pointCollectionList.get(i).color.get(2), 1.0f);
                    drawVbo(mPointCoordsHandle, countCircleNodes + 2, GLES20.GL_TRIANGLE_FAN);
                }
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {

        GLES20.glViewport(0, 0, width, height);

        float near = 1.0f, far = 10.0f;

        if (width > height)
        {
            wmax = (float) width / height;
            hmax = 1.0f;
        }
        else
        {
            wmax = 1.0f;
            hmax = (float) height / width;
        }

        Matrix.frustumM(mProjectionMatrix, 0, -wmax, wmax, -hmax, hmax, near, far);
        /*************************************************
         Set Model Matrix
         *************************************************/
        Matrix.setIdentityM(mGridModelMatrix, 0);
        mGridModelMatrix[0] = 2.0f * wmax;
        mGridModelMatrix[12] = -wmax;
        mGridModelMatrix[5] = 2.0f * hmax;
        mGridModelMatrix[13] = -hmax;

        countNodes = width;
        freeSource();
            if (Model.state != Model.State.NONE) {
                int i, n = Model.graphicList.size();
                for (i = 0; i < n; ++i) {
                    Graphic g = Model.graphicList.get(i);
                    setGraphic(g.f, g.leftBorder, g.rightBorder);
                }
                changeMatrix = true;
            }
    }

    //protected
}
