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

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.jiangdg.usbcamera.UVCCameraHelper;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.common.UVCCameraHandler;
import com.serenegiant.usb.common.UVCCameraHandlerMultiSurface;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.serenegiant.usb.widget.UVCCameraTextureView;
import com.srpass.usbcamera.R;

import java.io.File;
import java.util.List;

public final class MainActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent {
	private static final boolean DEBUG = true;	// TODO set false on release
	private static final String TAG = "MainActivity";

	private final Object mSync = new Object();
	/**
	 * for accessing USB
	 */
	private USBMonitor mUSBMonitor;
	/**
	 * Handler to execute camera releated methods sequentially on private thread
	 */
	private UVCCameraHandlerMultiSurface mCameraHandler;
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
	private UVCCameraTextureView bigCameraGLSurfaceView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:");
		setContentView(R.layout.activity_main);
		bigLayout = (FrameLayout) findViewById(R.id.bigLayout);
		mCameraButton = (ToggleButton)findViewById(R.id.camera_button);
		mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mCaptureButton = (ImageButton)findViewById(R.id.capture_button);
		mCaptureButton.setOnClickListener(mOnClickListener);
		mCaptureButton.setVisibility(View.INVISIBLE);

		mUVCCameraViewL = (CameraViewInterface)findViewById(R.id.camera_view_L);
//		mUVCCameraViewL.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
		mUVCCameraViewL.setCallback(mCallback);
//
		mUVCCameraViewR = (CameraViewInterface)findViewById(R.id.camera_view_R);
		mUVCCameraViewR.setAspectRatio(UVCCamera.DEFAULT_PREVIEW_WIDTH / (float) UVCCamera.DEFAULT_PREVIEW_HEIGHT);
		mUVCCameraViewR.setCallback(mCallback);


//
//		CameraSurFaceView bigView = new CameraSurFaceView(MainActivity.this);
//		bigView.addItemView(new CameraSurFaceView.getItemView() {
//			@Override
//			public void onItemView(UVCCameraTextureView cameraGLSurfaceView) {
//				bigCameraGLSurfaceView=cameraGLSurfaceView;
//			}
//		});
//		bigLayout.addView(bigView.getView());
//		bigCameraGLSurfaceView.setAspectRatio(1280 / (float) 720);
//		bigCameraGLSurfaceView.setCallback(mCallback);
        //Nv21  采集yuv 10帧  cpu:28%   ()
    	//yuv420采集yuv  10帧  cpu:22%

		//采集MJPEG  20帧  cpu:47%
		synchronized (mSync) {
			mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
			mCameraHandler = UVCCameraHandlerMultiSurface.createHandler(this, mUVCCameraViewL, 1,
					1280, 720,UVCCamera.FRAME_FORMAT_MJPEG,24);

		}
//		mCameraHandler.setOnPreViewResultListener(new AbstractUVCCameraHandler.OnPreViewResultListener() {
//			@Override
//			public void onPreviewResult(byte[] data) {
//				Log.e("","onPreviewResult...");
//			}
//		});
		CameraDialog.showDialog(MainActivity.this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.v(TAG, "onStart:");
		synchronized (mSync) {
			mUSBMonitor.register();
		}
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
		synchronized (mSync) {
//			mCameraHandler.stopRecording();
//			mCameraHandler.stopPreview();
    		mCameraHandler.close();	// #close include #stopRecording and #stopPreview
			mUSBMonitor.unregister();
		}
		if (mUVCCameraViewL != null) {
			mUVCCameraViewL.onPause();
		}
		if (mUVCCameraViewR != null) {
			mUVCCameraViewR.onPause();
		}
		setCameraButton(false);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		synchronized (mSync) {
			if (mCameraHandler != null) {
				mCameraHandler.release();
				mCameraHandler = null;
			}
			if (mUSBMonitor != null) {
				mUSBMonitor.destroy();
				mUSBMonitor = null;
			}
		}
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
				synchronized (mSync) {
					if ((mCameraHandler != null) && mCameraHandler.isOpened()) {
//						if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
//							if (!mCameraHandler.isRecording()) {
//								mCaptureButton.setColorFilter(0xffff0000);	// turn red
//								mCameraHandler.startRecording();
//							} else {
//								mCaptureButton.setColorFilter(0);	// return to default color
//								mCameraHandler.stopRecording();
//							}
//						}
					}
				}
				break;
			}
		}
	};

	private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener= new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
			switch (compoundButton.getId()) {
			case R.id.camera_button:
				synchronized (mSync) {
					if (isChecked && (mCameraHandler != null) && !mCameraHandler.isOpened()) {
						CameraDialog.showDialog(MainActivity.this);
					} else {
						mCameraHandler.close();
						setCameraButton(false);
					}
				}
				break;
			}
		}
	};



	private void setCameraButton(final boolean isOn) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (mCameraButton != null) {
//					try {
//						mCameraButton.setOnCheckedChangeListener(null);
//						mCameraButton.setChecked(isOn);
//					} finally {
//						mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
//					}
//				}
//				if (!isOn && (mCaptureButton != null)) {
//					mCaptureButton.setVisibility(View.INVISIBLE);
//				}
//			}
//		}, 0);
	}

	private void startPreview() {
		synchronized (mSync) {
			if (mCameraHandler != null) {
				mCameraHandler.startPreview();
			}
			Toast.makeText(MainActivity.this, "onConnect...startPreview", Toast.LENGTH_SHORT).show();
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCaptureButton.setVisibility(View.VISIBLE);
			}
		});
	}
	public List<UsbDevice> getUsbDeviceList() {
		List<DeviceFilter> deviceFilters = DeviceFilter
				.getDeviceFilters(this, com.jiangdg.libusbcamera.R.xml.device_filter);
		if (mUSBMonitor == null || deviceFilters == null)
			return null;
		return mUSBMonitor.getDeviceList(deviceFilters.get(0));
	}


	public void requestPermission(int index) {
		List<UsbDevice> devList = getUsbDeviceList();
		if (devList == null || devList.size() == 0) {
			return;
		}
		int count = devList.size();
		if (index >= count)
			new IllegalArgumentException("index illegal,should be < devList.size()");
		if (mUSBMonitor != null) {
			mUSBMonitor.requestPermission(getUsbDeviceList().get(index));
		}
	}

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
			Log.e("","mOnDeviceConnectListener.....onAttach");

		}

		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "onConnect:");
			Log.e("","mOnDeviceConnectListener.....onConnect");
			synchronized (mSync) {
				if (mCameraHandler != null) {
					Log.e("","mOnDeviceConnectListener.....onConnect...open");
					mCameraHandler.open(ctrlBlock);
					startPreview();
					Toast.makeText(MainActivity.this, "onConnect...预览", Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "onDisconnect:");
			Log.e("","mOnDeviceConnectListener.....onDisconnect");

			synchronized (mSync) {
				if (mCameraHandler != null) {
//					queueEvent(new Runnable() {
//						@Override
//						public void run() {
//							synchronized (mSync) {
//								if (mCameraHandler != null) {
//									mCameraHandler.close();
//								}
//							}
//						}
//					}, 0);
				}
			}
			setCameraButton(false);
		}

		@Override
		public void onDettach(final UsbDevice device) {
			Log.e("","mOnDeviceConnectListener.....onDettach");
			Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onCancel(final UsbDevice device) {
			setCameraButton(false);
		}
	};

	/**
	 * to access from CameraDialog
	 * @return
	 */

	private final CameraViewInterface.Callback
		mCallback = new CameraViewInterface.Callback() {
		@Override
		public void onSurfaceCreated(final CameraViewInterface view, final Surface surface) {
			mCameraHandler.addSurface(surface.hashCode(), surface, false);
		}

		@Override
		public void onSurfaceChanged(final CameraViewInterface view, final Surface surface, final int width, final int height) {

		}

		@Override
		public void onSurfaceDestroy(final CameraViewInterface view, final Surface surface) {
			synchronized (mSync) {
				if (mCameraHandler != null) {
					mCameraHandler.removeSurface(surface.hashCode());
				}
			}
		}
	};

	@Override
	public USBMonitor getUSBMonitor() {
		synchronized (mSync) {
			return mUSBMonitor;
		}
	}

	@Override
	public void onDialogResult(boolean canceled) {
		if (canceled) {
			setCameraButton(false);
		}
	}

//	@Override
//	public void onSelectDevice(UsbDevice usbDevice) {
//
//	}
}
