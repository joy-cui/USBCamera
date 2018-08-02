package com.usbcamera.contant;

import com.serenegiant.usb.UVCCamera;

public class UsbCameraEntry {
//    public static boolean isSwitch = false;//解决前后相机切换时，最后一帧图像倒置问题
//    public static boolean isRotate = false;//解决相机旋转时，最后一帧图像显示不对问题
//    public static int deviceType=UsbCameraEntry.DeviceType.mobile;

    public static int Image_Format= UVCCamera.FRAME_FORMAT_MJPEG;
    public class DeviceType {
        public static final int mobile = 0;
        public static final int box = 1;
    }
    public static class CaptureSize{
        public static int width = 1280;
        public static int height = 720;

    }
    public static class CaptureParam{//采集参数设置
        public static int  mFps=25;//默认15帧
    }
    public static class CaptrueRenderCode{
        public static boolean isRender=true;//是否本地渲染
        public static boolean isCode=true;//是否发送给sdk编码

    }

    public static class USBCaptureError{
        public static int ERROR_001=100;//采集失败
        public static int ERROR_002=200;//停止采集失败
        public static int ERROR_000=-1;//初始化失败
    }

}
