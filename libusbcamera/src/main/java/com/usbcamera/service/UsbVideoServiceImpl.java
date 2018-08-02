package com.usbcamera.service;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.Surface;

import com.serenegiant.usb.widget.CameraViewInterface;
import com.suirui.srpaas.base.util.log.SRLog;
import com.usbcamera.capture.UsbVideoCapture;
import com.usbcamera.contant.UsbCameraEntry;
import com.usbcamera.listener.UsbCameraCaptureListener;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by cui on 2018/6/4.
 */

public class UsbVideoServiceImpl implements IUsbVideoServeice , UsbCameraCaptureListener.UsbCameraVideoListener{
SRLog log=new SRLog("UsbVideoServiceImpl",1);
    private UsbVideoServiceListener mListener;
    private UsbVideoCapture videoCapture;
    private static UsbVideoServiceImpl instance=null;
    public synchronized static UsbVideoServiceImpl getInstance(Context context) {
        if (instance == null) {
            instance = new UsbVideoServiceImpl(context);
        }
        return instance;
    }


    public UsbVideoServiceImpl(Context mContext) {
        log.E("UsbVideoServiceImpl......");
        if (videoCapture == null) {
            videoCapture = new UsbVideoCapture(mContext);
        }
        UsbCameraCaptureListener.getInstance().addCameraVideoListener(this);
    }


    public UsbVideoServiceImpl(Context mContext, CameraViewInterface cameraViewInterface) {
        log.E("UsbVideoServiceImpl....cameraViewInterface..");
        if (videoCapture == null) {
            videoCapture = new UsbVideoCapture(mContext,cameraViewInterface);
        }
        UsbCameraCaptureListener.getInstance().addCameraVideoListener(this);
    }


    @Override
    public void addVideoServiceListener(UsbVideoServiceListener listener) {
        this.mListener=listener;
    }

    @Override
    public void removeVideoServiceListener() {
        this.mListener=null;
    }



    @Override
    public UsbDevice getDeviceType() {
        if(videoCapture!=null) {
            return videoCapture.getSelectUsbDevice();
        }
        return null;
    }

    @Override
    public boolean startCapture() {
        if(videoCapture!=null) {
            return videoCapture.startCapture();
        }
        return false;
    }

    @Override
    public void stopCapture() {
        if(videoCapture!=null) {
            videoCapture.stopCapture();
        }
    }

    @Override
    public void switchCamera() {
        if(videoCapture!=null) {
            videoCapture.switchCamera();
        }
    }

    @Override
    public void setCaptureSize(int width, int height) {
        UsbCameraEntry.CaptureSize.width=width;
        UsbCameraEntry.CaptureSize.height=height;
        log.E("setCaptureSize...."+UsbCameraEntry.CaptureSize.width);
    }

    @Override
    public void setCaptureFps(int mFps) {
        UsbCameraEntry.CaptureParam.mFps=mFps;
    }

    @Override
    public List<UsbDevice> getUsbDeviceList() {
        return videoCapture.getUsbDeviceList();
    }

    @Override
    public int getUsbUsbDeviceCount() {
        if(videoCapture!=null) {
            List<UsbDevice> usbDeviceList = videoCapture.getUsbDeviceList();
            if (usbDeviceList != null) {
                return usbDeviceList.size();
            }
        }
        return 0;
    }

    @Override
    public void addSurface(int surfaceId, Surface surface, boolean isRecordable) {
        if(videoCapture!=null) {
            videoCapture.addSurface(surfaceId, surface, isRecordable);
        }
    }

    @Override
    public void removeSurface(int surfaceId) {
        if(videoCapture!=null) {
            videoCapture.removeSurface(surfaceId);
        }
    }

    @Override
    public void clearData() {
        if(videoCapture!=null) {
            videoCapture.clearData();
        }
    }


    @Override
    public void onPreviewCallback(byte[] data, int width, int height) {
        if (mListener != null)
            mListener.onPreviewCallback(data,width,height);
    }

    @Override
    public void onPreviewCallback(ByteBuffer data, int width, int height) {
//        log.E("onPreviewCallback....ByteBuffer");
        if (mListener != null)
            mListener.onPreviewCallback(data,width,height);
    }

    @Override
    public void onUsbCameraCaptureListener(boolean isOpen, int code) {
        if (mListener != null)
            mListener.onUsbCameraCaptureListener(isOpen,code);
    }

    @Override
    public void onPreviewCallback(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height) {
        if (mListener != null)
            mListener.onPreviewCallback(y,u,v,width,height);
    }
}
