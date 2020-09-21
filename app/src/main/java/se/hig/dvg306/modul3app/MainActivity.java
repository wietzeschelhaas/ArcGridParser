package se.hig.dvg306.modul3app;

/*
   MainActivity.java

    Exempelkod som visar hur transformationer och projektioner kan användas
    i ett OpenGL ES program.

    Koden skapades utifrån filen 'HelloTriangle.java' licenserad
    enligt MIT-licens - se nedan.

    Peter Jenke, Peter.Jenke@hig.se
    2018-12-06
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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import java.io.IOException;

public class MainActivity extends Activity implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener
{
    private final int CONTEXT_CLIENT_VERSION = 3;
    private GestureDetectorCompat mDetector;
    Modul3Renderer renderer;

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate ( savedInstanceState );

        final Context thisActivity = this;

        if (detectOpenGLES30()) {
           // Tell the surface view we want to create an OpenGL ES 3.0-compatible
           // context, and set an OpenGL ES 3.0-compatible renderer.
           try {
               Log.e(TAG, "--->>>      Creating new GLSurfaceView...");
               mGLSurfaceView = new GLSurfaceView(thisActivity);
               mGLSurfaceView.setEGLContextClientVersion(CONTEXT_CLIENT_VERSION);
               Log.e(TAG, "--->>>      ...finished.");
               Log.e(TAG, "--->>>      Creating new renderer...");
               renderer  = new Modul3Renderer(thisActivity);
               Log.e(TAG, "--->>>      ...finished.");
               Log.e(TAG, "--->>>      Creating and assigning renderer to view...");
               mGLSurfaceView.setRenderer(renderer);
               Log.e(TAG, "--->>>      ...finished.");
           } catch (RuntimeException e) {
               Log.e(TAG, "--->>>      Error while loading resources:\n" + e.getMessage() + "\n\nExiting...");
               finish();
           }
        } else {
           Log.e(TAG, "--->>>      OpenGL ES 3.0 not supported on device.  Exiting...");
           finish();

        }

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        setContentView(mGLSurfaceView);
    }

    private boolean detectOpenGLES30()
    {
        ActivityManager am =
                ( ActivityManager ) getSystemService ( Context.ACTIVITY_SERVICE );
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return ( info.reqGlEsVersion >= 0x30000 );
    }

    @Override
    protected void onResume()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();

        synchronized (mGLSurfaceView) {
            mGLSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause()
    {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();

        synchronized (mGLSurfaceView) {
            mGLSurfaceView.onPause();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction() == MotionEvent.ACTION_UP){
            //tell renderer that the user had stopped touching the screen
            renderer.touchUp = true;
        }


        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {


        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {


    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {


        //tell renderer that the user is touching the screen
        renderer.touchUp = false;


        renderer.setScrollX(v/10);
        renderer.setScrollY(v1/10);


        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
        renderer.deCreasezoom();
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        renderer.inCreaseZoom();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }

    private GLSurfaceView mGLSurfaceView;
    private static String TAG = "DVG306_Modul3App MainActivity";
}