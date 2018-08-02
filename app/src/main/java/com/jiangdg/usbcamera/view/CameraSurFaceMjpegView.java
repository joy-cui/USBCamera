package com.jiangdg.usbcamera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.jiangdg.usbcamera.mjpeg.GLFrameH264Render;
import com.jiangdg.usbcamera.mjpeg.GLH264FrameSurface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.srpass.usbcamera.R;


/**
 * @authordingna
 * @date2017-11-09
 **/
public class CameraSurFaceMjpegView extends View {
    private View view;
    private GLH264FrameSurface glh264FrameSurface;
    private GLFrameH264Render glFrameH264Render;
//    private CameraRender cameraRender;
    public CameraSurFaceMjpegView(Context context) {
        super(context);
        this.init(context);
    }

    public CameraSurFaceMjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public CameraSurFaceMjpegView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    private void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.test_camera_view, null);
        FrameLayout remoteLayout = (FrameLayout) view.findViewById(R.id.cameraView);
        glh264FrameSurface = new GLH264FrameSurface(context, 360);
        remoteLayout.addView(glh264FrameSurface);
        glh264FrameSurface.setBackgroundColor(getResources().getColor(R.color.sr_transparents));
        glh264FrameSurface.setZOrderMediaOverlay(true);
        glFrameH264Render = new GLFrameH264Render(context, glh264FrameSurface);
        glh264FrameSurface.setRenderer(glFrameH264Render);
    }

    public View getView() {
        return view;
    }

    public void addItemView(getItemView mGetItemView) {
        mGetItemView.onItemView(glh264FrameSurface,glFrameH264Render);
    }

    public interface getItemView {
        void onItemView(GLH264FrameSurface cameraGLSurfaceView,GLFrameH264Render render);
    }


}
