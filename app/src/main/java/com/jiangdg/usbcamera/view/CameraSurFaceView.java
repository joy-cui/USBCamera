package com.jiangdg.usbcamera.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.UVCCameraHandlerMultiSurface;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.srpass.usbcamera.R;


/**
 * @authordingna
 * @date2017-11-09
 **/
public class CameraSurFaceView extends View {
    private View view;
    private UVCCameraTextureView cameraGLSurfaceView;
//    private CameraRender cameraRender;
    public CameraSurFaceView(Context context) {
        super(context);
        this.init(context);
    }

    public CameraSurFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public CameraSurFaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    private void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.test_camera_view, null);
        FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.cameraView);
        cameraGLSurfaceView = new UVCCameraTextureView(context);
        cameraGLSurfaceView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        frameLayout.addView(cameraGLSurfaceView);
//        cameraRender = new CameraRender(cameraGLSurfaceView, context);
//        cameraGLSurfaceView.setRenderer(cameraRender);
    }

    public View getView() {
        return view;
    }

    public void addItemView(getItemView mGetItemView) {
        mGetItemView.onItemView(cameraGLSurfaceView);
    }

    public interface getItemView {
        void onItemView(UVCCameraTextureView cameraGLSurfaceView);
    }


}
