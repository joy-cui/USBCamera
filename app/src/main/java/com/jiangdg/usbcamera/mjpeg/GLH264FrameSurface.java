package com.jiangdg.usbcamera.mjpeg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.suirui.srpaas.base.util.log.SRLog;

@SuppressLint("ViewConstructor")
public class GLH264FrameSurface extends GLSurfaceView {
    String TAG="VideoRender";
    private SRLog log = new SRLog(TAG,1);
    //    GLFrameH264Render  mRenderer;
    public GLH264FrameSurface(Context context,int size) {
        this(context, null,size);
    }

    public GLH264FrameSurface(Context context, AttributeSet attrs,int size) {
        super(context, attrs);
        setEGLContextClientVersion(2);
    }

    public void clearRender(){
        this.onPause();



    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        log.E("GLH264FrameSurface....SRSdkJni...onAttachedToWindow");
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

}
