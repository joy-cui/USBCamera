package com.usbcamera.service;

import android.hardware.usb.UsbDevice;
import android.view.Surface;

import java.util.List;

/**
 * Created by cui on 2018/6/4.
 */

public interface IUsbVideoServeice {
    void addVideoServiceListener(UsbVideoServiceListener listener);

    void removeVideoServiceListener();

    UsbDevice getDeviceType();

    /**
     * 视频采集
     *
     * @return
     */
    boolean startCapture();

    /**
     * 关闭相机
     */
    void stopCapture();

    /**
     * 切换相机
     */
    void switchCamera();

    /**
     * 设置采集的分辨率
     * @param width
     * @param height
     */
    void setCaptureSize(int width, int height);

    /**
     * 设置帧率
     * @param mFps
     */
    void setCaptureFps(int mFps);

//    /**
//     * 测试
//     * @param isRender true 渲染 false 不渲染
//     * @param isCode  true 编码  false 不编码
//     */
//    void setCaptrueRenderCode(boolean isRender, boolean isCode);

    /**
     * 获取usb摄像头的个数
     * @return
     */
    List<UsbDevice> getUsbDeviceList();

    int getUsbUsbDeviceCount();

    void  addSurface(final int surfaceId, final Surface surface, final boolean isRecordable);
    void removeSurface(final int surfaceId);

    void clearData();

}
