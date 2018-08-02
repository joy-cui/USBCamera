package com.usbcamera.listener;

import java.nio.ByteBuffer;

/**
 * Created by cui.li on 2016/11/2.
 */

public class UsbCameraCaptureListener {
    private static UsbCameraCaptureListener cameraListener;
    private UsbCameraVideoListener cameraVideoListener;

    public UsbCameraCaptureListener() {

    }

    public synchronized static UsbCameraCaptureListener getInstance() {

        if (cameraListener == null) {
            cameraListener = new UsbCameraCaptureListener();
        }
        return cameraListener;

    }

    public void addCameraVideoListener(UsbCameraVideoListener listener) {
        this.cameraVideoListener = listener;
    }

    public void onPreviewCallback(byte[] data, int width, int height) {
        if (cameraVideoListener != null) {
            cameraVideoListener.onPreviewCallback(data, width, height);
        }
    }


    public void onPreviewCallback(ByteBuffer data, int width, int height) {
        if (cameraVideoListener != null) {
            cameraVideoListener.onPreviewCallback(data, width, height);
        }
    }

    public void onUsbCameraCaptureListener(boolean isOpen,int code){
        if (cameraVideoListener != null) {
            cameraVideoListener.onUsbCameraCaptureListener(isOpen, code);
        }
    }
    public  void onPreviewCallback(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height){
        if (cameraVideoListener != null) {
            cameraVideoListener.onPreviewCallback(y,u,v, width,height);
        }
    }

    public interface UsbCameraVideoListener {
        void onPreviewCallback(byte[] data, int width, int height);
        void onPreviewCallback(ByteBuffer data, int width, int height);
        void onUsbCameraCaptureListener(boolean isOpen, int code);
        void onPreviewCallback(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height);
    }

}
