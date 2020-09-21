package se.hig.dvg306.modul3app;

/*
    Modul3Renderer.java

    Exempelkod som visar hur transformationer och projektioner samt hur uniform-variabler
    och vertex-arrays kan användas i ett OpenGL ES program.

    Koden skapades utifrån filen 'HelloTriangleRenderer.java' licenserad
    enligt MIT-licens - se nedan.

    Peter Jenke, Peter.Jenke@hig.se
    2019-02-20
 */

// The MIT License (MIT)
//
// Copyright (c) 2013 Dan Ginsburg, Budirijanto Purnomo
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

//
// Book:      OpenGL(R) ES 3.0 Programming Guide, 2nd Edition
// Authors:   Dan Ginsburg, Budirijanto Purnomo, Dave Shreiner, Aaftab Munshi
// ISBN-10:   0-321-93388-5
// ISBN-13:   978-0-321-93388-1
// Publisher: Addison-Wesley Professional
// URLs:      http://www.opengles-book.com
//            http://my.safaribooksonline.com/book/animation-and-3d/9780133440133
//

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import se.hig.dvg306.modul3app.tools.DemModel;
import se.hig.dvg306.modul3app.tools.ResourceHandler;

public class Modul3Renderer implements GLSurfaceView.Renderer
{
    //
    // Constructor - loads model data from a res file and creates byte buffers for
    // vertex data and for normal data
    //



    DemModel rawModel;

    public Modul3Renderer (Context context)
    {

        appContext = context;

        List<String> mVerticesDataStringList = null;
        try {
            Log.e ( TAG, "--->>>      Start loading model from dem data set.");
            mVerticesDataStringList = ResourceHandler.readDataToList(context, R.raw.dem);
            Log.e ( TAG, "--->>>      Finished loading model from data set.");
        } catch (IOException e) {
            Log.e ( TAG, "--->>>      Could not load model from data set.");
        }

        //get the vertice data
        rawModel = new DemModel(mVerticesDataStringList);

        float[] mVerticesData; //= new float[0];
        mVerticesData = rawModel.getVerticeData();
        Log.e(TAG, "--->>>      ...finished.");

        // Process vertex data
        // 4: because of 4 elements per vertex position
        nbrOfVertices = mVerticesData.length / 4;

        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);

        Log.e(TAG, "--->>>      Starting with normals...");
        float[] mNormalData; //= new float[0];

        //get the normal data
        mNormalData = rawModel.getNormalData();

        Log.e(TAG, "--->>>      ...finished.");

        // Process normal data
        // 4: because of 4 elements per vertex position
        nbrOfNormals = mNormalData.length / 4;

        mNormals = ByteBuffer.allocateDirect(mNormalData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mNormals.put(mNormalData).position(0);



        float[] mTextCoord;

        //get texture coordinates data
        mTextCoord = rawModel.getTextCoords();

        Log.e(TAG, "--->>>      ...finished.");


        mTextCoords = ByteBuffer.allocateDirect(mTextCoord.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextCoords.put(mTextCoord).position(0);


}

    ///
    // Create a shader object, load the shader source, and
    // compile the shader.
    //
    private int createShader(int type, String shaderSrc )
    {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES30.glCreateShader ( type );

        if ( shader == 0 )
        {
            return 0;
        }

        // Load the shader source
        GLES30.glShaderSource ( shader, shaderSrc );

        // Compile the shader
        GLES30.glCompileShader ( shader );

        // Check the compile status
        GLES30.glGetShaderiv ( shader, GLES30.GL_COMPILE_STATUS, compiled, 0 );

        if ( compiled[0] == 0 )
        {
            Log.e ( TAG, GLES30.glGetShaderInfoLog ( shader ) );
            GLES30.glDeleteShader ( shader );
            return 0;
        }

        return shader;
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated ( GL10 glUnused, EGLConfig config )
    {

        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];


        // Load the source code for the vertex shader program from a res file:
        try {
            vShaderStr = ResourceHandler.readTextData(appContext, R.raw.vertex_shader);
        } catch (IOException e) {
            Log.e ( TAG, "--->>>      Could not load source code for vertex shader.");
            throw new RuntimeException (e);
        }
        Log.e ( TAG, "--->>>      Loaded vertex shader: " + vShaderStr);

        // Load the source code for the fragment shader program from a res file:
        try {
            fShaderStr = ResourceHandler.readTextData(appContext, R.raw.fragment_shader);
        } catch (IOException e) {
            Log.e ( TAG, "--->>>      Could not load source code for fragment shader.");
            throw new RuntimeException (e);
        }
        Log.e ( TAG, "--->>>      Loaded fragment shader: " + fShaderStr);

        // Create the vertex/fragment shaders
        vertexShader = createShader( GLES30.GL_VERTEX_SHADER, vShaderStr );
        fragmentShader = createShader( GLES30.GL_FRAGMENT_SHADER, fShaderStr );

        // Create the program object
        programObject = GLES30.glCreateProgram();

        if ( programObject == 0 )
        {
            return;
        }

        GLES30.glAttachShader ( programObject, vertexShader );
        GLES30.glAttachShader ( programObject, fragmentShader );


        // Bind vPosition to attribute 0
        GLES30.glBindAttribLocation ( programObject, 0, "vPosition" );

        // Bind vNormal to attribute 1
        GLES30.glBindAttribLocation ( programObject, 1, "vNormal" );

        GLES30.glBindAttribLocation ( programObject, 2, "vTexCoord" );



        // Link the program
        GLES30.glLinkProgram ( programObject );

        // Check the link status
        GLES30.glGetProgramiv ( programObject, GLES30.GL_LINK_STATUS, linked, 0 );

        if ( linked[0] == 0 )
        {
            Log.e ( TAG, "Error linking program:" );
            Log.e ( TAG, GLES30.glGetProgramInfoLog ( programObject ) );
            GLES30.glDeleteProgram ( programObject );
            return;
        }

        // Store the program object
        mProgramObject = programObject;

        GLES30.glClearColor ( 0.5f, 0.5f, 0.5f, 1.0f );
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
    }


    float counter = 0;
    public void onDrawFrame ( GL10 glUnused )
    {
    counter += 0.1f;

    if(touchUp){
        scrollX = 0;
        scrollY = 0;
    }
    currentXpos = currentXpos - scrollX;
    currentYpos = currentYpos - scrollY;




        // Initiate the model-view matrix as identity matrix
        Matrix.setIdentityM(mViewMatrix, 0);
        // Modify the method call Matrix.translateM below
        // Define a translation transformation
        // Modify the distance in y-direction here - current value for step 1
        Matrix.translateM(mViewMatrix, 0, currentXpos  , currentYpos, zoom);
        // Add the method call Matrix.scaleM below
        // Define a scaling transformation
        // Necessary because of the objects original size
        Matrix.scaleM(mViewMatrix, 0, 0.015f, 0.015f, 0.015f);
        // Define a rotation transformation
        //Matrix.rotateM(mViewMatrix, 0, 1.0f, 0.0f, 0.0f, 1.0f);



        // Calculate the model-view and projection transformation as composite transformation
        Matrix.multiplyMM (mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Clear the color buffer
        GLES30.glClear ( GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT );

        // Use the program object
        GLES30.glUseProgram ( mProgramObject );

        //send the max height to the vertex shader
        maxHeightHandle = GLES30.glGetUniformLocation(mProgramObject,"maxHeight");
        GLES30.glUniform1f(maxHeightHandle,rawModel.getMaxHeight());

        // Make MVP matrix accessible in the vertex shader
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramObject, "uMVPMatrix");
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Light position:
        vLightPositionHandle = GLES30.glGetUniformLocation(mProgramObject, "vLightPosition");
        GLES30.glUniform4fv(vLightPositionHandle, 1, lightPosition, 0);

        // Light color:
        vLightColorDfHandle = GLES30.glGetUniformLocation(mProgramObject, "vLightColorDf");
        GLES30.glUniform4fv(vLightColorDfHandle, 1, lightColorDf, 0);

        // Material color:
        vMaterialColorDfHandle = GLES30.glGetUniformLocation(mProgramObject, "vMaterialColorDf");
        GLES30.glUniform4fv(vMaterialColorDfHandle, 1, materialColorDf, 0);

        // Load the vertex data from mVertices
        GLES30.glVertexAttribPointer ( 0, 4, GLES30.GL_FLOAT, false, 0, mVertices );
        // Assign vertex data to 'in' variable bound to attribute with index 0:
        GLES30.glEnableVertexAttribArray ( 0 );

        // Load the normal data from mNormals
        GLES30.glVertexAttribPointer ( 1, 4, GLES30.GL_FLOAT, false, 0, mNormals );
        // Assign normal data to 'in' variable bound to attribute with index 1:
        GLES30.glEnableVertexAttribArray ( 1 );

        // Load the normal data from mTextCoords
        GLES30.glVertexAttribPointer ( 2, 2, GLES30.GL_FLOAT, false, 0, mTextCoords );
        // Assign normal data to 'in' variable bound to attribute with index 1:
        GLES30.glEnableVertexAttribArray ( 2 );



        GLES30.glDrawArrays (GLES30.GL_TRIANGLES, 0, nbrOfVertices);

        GLES30.glDisableVertexAttribArray ( 1 );
        GLES30.glDisableVertexAttribArray ( 0 );
        GLES30.glDisableVertexAttribArray ( 2 );
    }

    //
    // Handle surface changes
    //
    public void onSurfaceChanged ( GL10 glUnused, int width, int height )
    {
        mWidth = width;
        mHeight = height;

        GLES30.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 0.5f, 100.0f);

        Matrix.setIdentityM(testScaleMatrix, 0);
        Matrix.scaleM(testScaleMatrix, 0, 1.0f, -1.0f, 1.0f);
        Matrix.multiplyMM (mProjectionMatrix, 0, mProjectionMatrix, 0, testScaleMatrix, 0);
       // Matrix.orthoM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, -1f, 100.0f);

    }

    public void setScrollX(float x){
        this.scrollX = x;
    }
    public void setScrollY(float y){
        this.scrollY = y;
    }
    public void inCreaseZoom(){
        this.zoom +=10;
    }
    public void deCreasezoom(){
        this.zoom -=10;
    }



    // Member variables

    boolean touchUp = true;

    private Context appContext;

    private float scrollX = 0;
    private float scrollY = 0;


    private float zoom = -50;

    float currentXpos = -110;
    float currentYpos = -80;

    private int mWidth;
    private int mHeight;

    private int nbrOfVertices;
    private FloatBuffer mVertices;

    private int nbrOfNormals;
    private FloatBuffer mNormals;

    private FloatBuffer mTextCoords;

    private int mProgramObject;
    private int mMVPMatrixHandle;

    private int maxHeightHandle;

    // Transformation data:
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private final float[] testScaleMatrix = new float[16];

    // Light position and color (only diffuse term now):
    private int vLightPositionHandle;
    private final float lightPosition [] = {175.0f, 75.0f, 125.0f, 0.0f};
    // Light color (only diffuse term now):
    private int vLightColorDfHandle;
    private final float lightColorDf [] = {0.98f, 0.98f, 0.98f, 1.0f};
    // Material color (only diffuse term now):
    private int vMaterialColorDfHandle;
    private final float materialColorDf [] = {0.62f, 0.773f, 0.843f, 1.0f};

    // To be read when creating the instance:
    private String vShaderStr;
    private String fShaderStr;

    private static String TAG = "Modul3Renderer";




}