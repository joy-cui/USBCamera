/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.jiangdg.usbcamera.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jiangdg.usbcamera.mjpeg.GLFrameH264Render;
import com.jiangdg.usbcamera.mjpeg.GLH264FrameSurface;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.UVCCameraHandlerMultiSurface;
import com.serenegiant.usb.encoder.IVideoEncoder;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.srpass.usbcamera.R;
import com.suirui.srpaas.base.util.log.SRLog;
import com.usbcamera.service.IUsbVideoServeice;
import com.usbcamera.service.UsbVideoServiceImpl;
import com.usbcamera.service.UsbVideoServiceListener;


import java.nio.ByteBuffer;
import java.util.List;

public final class CaptureActivity extends AppCompatActivity implements UsbVideoServiceListener {
	private static final boolean DEBUG = true;	// TODO set false on release
	private static final String TAG = "MainActivity";
	SRLog log=new SRLog(TAG,1);

	/**
	 * for camera preview display
	 */
	private CameraViewInterface mUVCCameraViewL;
	private CameraViewInterface mUVCCameraViewR;
	/**
	 * for open&start / stop&close camera preview
	 */
	private ToggleButton mCameraButton;
	/**
	 * button for start/stop recording
	 */
	private ImageButton mCaptureButton;
	private FrameLayout bigLayout;
//	private UVCCameraTextureView bigCameraGLSurfaceView;


	IUsbVideoServeice usbVideoServeice=null;
	ByteBuffer yBuf=null;
	ByteBuffer uBuf=null;
	ByteBuffer vBuf=null;
	byte[] callBackData=null;

//	private UVCCameraTextureView bigCameraGLSurfaceView=null;
	Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what){
				case 100:
					createSurfaceViewTest();
					break;
				case 101:
					if(bigLayout!=null){
						bigLayout.removeAllViews();
					}
//					bigCameraGLSurfaceView=null;
					break;
			}
		}
	};

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:");
		setContentView(R.layout.activity_main);
		bigLayout = (FrameLayout) findViewById(R.id.bigLayout);
		mCameraButton = (ToggleButton)findViewById(R.id.camera_button);
		mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCaptureButton = (ImageButton)findViewById(R.id.capture_button);
		Button testBtn= (Button) findViewById(R.id.test_btn);
		testBtn.setOnClickListener(mOnClickListener);

		mCaptureButton.setOnClickListener(mOnClickListener);
		mCaptureButton.setVisibility(View.INVISIBLE);
		CameraViewInterface textsurface=new CameraViewInterface() {
			@Override
			public void onPause() {

			}

			@Override
			public void onResume() {

			}

			@Override
			public void setCallback(Callback callback) {

			}

			@Override
			public SurfaceTexture getSurfaceTexture() {
				return null;
			}

			@Override
			public Surface getSurface() {
				return null;
			}

			@Override
			public boolean hasSurface() {
				return false;
			}

			@Override
			public void setVideoEncoder(IVideoEncoder encoder) {

			}

			@Override
			public Bitmap captureStillImage(int width, int height) {
				return null;
			}

			@Override
			public void setAspectRatio(double v) {

			}

			@Override
			public void setAspectRatio(int i, int i1) {

			}

			@Override
			public double getAspectRatio() {
				return 0;
			}
		};




//		log.E("captureActivity...setCallBack..end");




		log.E("captureActivity...new .UsbVideoServiceImpl.");
		usbVideoServeice=new UsbVideoServiceImpl(this,null);
//		usbVideoServeice.setCaptureSize(1280,720);
//		usbVideoServeice.setCaptureFps(25);

		usbVideoServeice.addVideoServiceListener(this);
		int size=usbVideoServeice.getUsbUsbDeviceCount();
		Toast.makeText(this,"usbDeviceCamera:"+size,Toast.LENGTH_LONG).show();

		mUVCCameraViewL = (CameraViewInterface)findViewById(R.id.camera_view_L);
		mUVCCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
		mUVCCameraViewL.setCallback(mCallback);

		mUVCCameraViewR = (CameraViewInterface)findViewById(R.id.camera_view_R);
		mUVCCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);



	}
	private GLFrameH264Render h264Render=null;
	private GLH264FrameSurface glh264FrameSurface=null;
	private void createSurfaceViewTest(){
		if(glh264FrameSurface==null) {
			CameraSurFaceMjpegView bigView = new CameraSurFaceMjpegView(CaptureActivity.this);
			bigView.addItemView(new CameraSurFaceMjpegView.getItemView() {

                @Override
                public void onItemView(GLH264FrameSurface cameraGLSurfaceView, GLFrameH264Render render) {
                    glh264FrameSurface=cameraGLSurfaceView;
                    h264Render=render;
                }
            });
			bigLayout.addView(bigView.getView());

		}
	}

private void createSurfaceView(){
//		if(bigCameraGLSurfaceView==null) {
//			CameraSurFaceView bigView = new CameraSurFaceView(CaptureActivity.this);
//			bigView.addItemView(new CameraSurFaceView.getItemView() {
//				@Override
//				public void onItemView(UVCCameraTextureView cameraGLSurfaceView) {
//					bigCameraGLSurfaceView = cameraGLSurfaceView;
//				}
//			});
//			bigLayout.addView(bigView.getView());
//			bigCameraGLSurfaceView.setAspectRatio(1280 / (float) 720);
//			bigCameraGLSurfaceView.setCallback(mCallback);
//		}
}
	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.v(TAG, "onStart:");

		if (mUVCCameraViewL != null) {
			mUVCCameraViewL.onResume();
		}
		if (mUVCCameraViewR != null) {
			mUVCCameraViewR.onResume();
		}
	}

	@Override
	protected void onStop() {
		if (DEBUG) Log.v(TAG, "onStop:");

		if (mUVCCameraViewL != null) {
			mUVCCameraViewL.onPause();
		}
		if (mUVCCameraViewR != null) {
			mUVCCameraViewR.onPause();
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");

        mUVCCameraViewL = null;
        mUVCCameraViewR = null;
        mCameraButton = null;
        mCaptureButton = null;
		super.onDestroy();
	}

	/**
	 * event handler when click camera / capture button
	 */
	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.capture_button:
   				log.E("mOnClickListener...");


				break;
				case R.id.test_btn:
					startActivity(new Intent(CaptureActivity.this,USBCameraActivity.class));
					break;
			}
		}
	};
boolean isStart=false;
boolean isTest=false;
	private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener= new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
			switch (compoundButton.getId()) {
			case R.id.camera_button:
				log.E("OnCheckedChangeListener....."+isChecked);
				if(isStart){
					usbVideoServeice.stopCapture();
//					Toast.makeText(CaptureActivity.this,"stopCapture: ",Toast.LENGTH_LONG).show();
					isStart=false;
					mHandler.sendEmptyMessageDelayed(101,0);
				}else {

					usbVideoServeice.startCapture();
//					Toast.makeText(CaptureActivity.this,"startCapture: ",Toast.LENGTH_LONG).show();
					isStart=true;
					mHandler.sendEmptyMessageDelayed(100,0);
				}
				break;
			}
		}
	};


	@Override
	public void onUsbCameraCaptureListener(boolean isOpen, int code) {

	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onPreviewCallback(byte[] data, int width, int height) {
		log.E("onPreviewCallback.mIFrameCallback..CaptureActivity.."+width+" : "+height);
//		glH264Render.onFrame(data,0,data.length,width,height);
//		glH264Render.update(width,height,false);

	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
	public void onPreviewCallback(ByteBuffer data, int width, int height) {
		log.E("onPreviewCallback..ByteBuffer.1.mIFrameCallback.CaptureActivity.."+width+" : "+height);
//		Toast.makeText(this,"onPreviewCallback: "+width,Toast.LENGTH_LONG).show();
        if(h264Render!=null){
            h264Render.onFrame(data,width,height);
        }
	}

	@Override
	public void onPreviewCallback(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height) {
		log.E("onPreviewCallback..ByteBuffer...2.CaptureActivity.."+width+" : "+height);
//		Toast.makeText(this,"usbDeviceCamera:..2222",Toast.LENGTH_LONG).show();
	}

	private final CameraViewInterface.Callback
			mCallback = new CameraViewInterface.Callback() {
		@Override
		public void onSurfaceCreated(final CameraViewInterface view, final Surface surface) {
//			mCameraHandler.addSurface(surface.hashCode(), surface, false);
			log.E("captureActivity...onSurfaceCreated....");
			Toast.makeText(CaptureActivity.this,"onSurfaceCreated:..2222",Toast.LENGTH_LONG).show();
			if(usbVideoServeice!=null) {
				usbVideoServeice.addSurface(surface.hashCode(), surface, false);
			}
		}

		@Override
		public void onSurfaceChanged(final CameraViewInterface view, final Surface surface, final int width, final int height) {

		}

		@Override
		public void onSurfaceDestroy(final CameraViewInterface view, final Surface surface) {
//			synchronized (mSync) {
//				if (mCameraHandler != null) {
//					mCameraHandler.removeSurface(surface.hashCode());
//				}
//			}
			log.E("captureActivity...onSurfaceDestroy....");
			Toast.makeText(CaptureActivity.this,"onSurfaceDestroy:..2222",Toast.LENGTH_LONG).show();
			if(usbVideoServeice!=null) {
				usbVideoServeice.removeSurface(surface.hashCode());
			}
		}
	};
}
