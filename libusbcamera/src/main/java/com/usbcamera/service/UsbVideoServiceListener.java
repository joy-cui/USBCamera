package com.usbcamera.service;

import java.nio.ByteBuffer;

/**
 * Created by cui on 2018/6/4.
 */

public interface UsbVideoServiceListener {
//    /**
//     * 相机失败
//     */
//    void onstartCaptureFailListener();
//
//    /**
//     * 关闭相机失败
//     */
    void onUsbCameraCaptureListener(boolean isOpen, int code);


    /**
     * 相机采集回来的数据
     *
     * @param data
     * @param width
     * @param height
     */
    void onPreviewCallback(byte[] data, int width, int height);

    void onPreviewCallback(ByteBuffer data, int width, int height);

    void onPreviewCallback(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height);
}
