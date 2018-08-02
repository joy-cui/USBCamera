package com.usbcamera.capture;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.common.UVCCameraHandlerMultiSurface;
import com.serenegiant.usb.widget.CameraViewInterface;
import com.suirui.srpaas.base.util.log.SRLog;
import com.usbcamera.contant.UsbCameraEntry;
import com.usbcamera.listener.UsbCameraCaptureListener;
import com.usbcamera.util.UsbDeviceUtil;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by cui on 2018/6/4.
 */

public class UsbVideoCapture  implements AbstractUVCCameraHandler.OnPreViewResultListener{
    SRLog log=new SRLog(UsbVideoCapture.class.getName(),1);

    private static final String TAG = "UsbVideoCapture";

    private final Object mSync = new Object();
    private USBMonitor mUSBMonitor;
    /**
     * Handler to execute camera releated methods sequentially on private thread
     */
    private UVCCameraHandlerMultiSurface mCameraHandler;

    private Context mContext;
    private UsbDevice selectUsbDevice;

    private CameraViewInterface surfaceTexture=null;

    public UsbVideoCapture(Context context){
        log.E("UsbVideoCapture....11.");
        //采集MJPEG  20帧  cpu:47%
        //采集yuv 20帧cpu:22%
        if(context!=null) {
            this.mContext = context;
        }
        initCamera();



    }



    public UsbVideoCapture(Context context, CameraViewInterface surface){
        log.E("UsbVideoCapture....22.");
        //采集MJPEG  20帧  cpu:47%
        //采集yuv 20帧cpu:22%
        if(context!=null) {
            this.mContext = context;
        }
        this.surfaceTexture=surface;
        initCamera();

    }

    /**
     * 获取usb接口
     * @return
     */
    public List<UsbDevice> getUsbDeviceList(){
        if(mUSBMonitor!=null){
          return   UsbDeviceUtil.getUsbDeviceList(mUSBMonitor,mContext);
        }else{
            return null;
        }

    }


        public void initCamera(){
        try {
            synchronized (mSync) {
                if (mUSBMonitor == null) {
                    log.E("mUSBMonitor....regiser.width.."+ UsbCameraEntry.CaptureSize.width);
                    mUSBMonitor = new USBMonitor(mContext, mOnDeviceConnectListener);
                    mCameraHandler = UVCCameraHandlerMultiSurface.createHandler((Activity) mContext, surfaceTexture, 1,
                            UsbCameraEntry.CaptureSize.width, UsbCameraEntry.CaptureSize.height, UsbCameraEntry.Image_Format, UsbCameraEntry.CaptureParam.mFps);
                    mUSBMonitor.register();
                    log.E("mUSBMonitor....regiser");
                    requestPermission();
                    log.E("requestPermission。。。。");
                    mCameraHandler.setOnPreViewResultListener(this);

                }

            }
        }catch (Exception e){
            e.printStackTrace();
            UsbCameraCaptureListener.getInstance().onUsbCameraCaptureListener(true, UsbCameraEntry.USBCaptureError.ERROR_000);
        }
    }


    public void requestPermission(){
            if(mUSBMonitor!=null) {
                List<UsbDevice> usbDeviceList = UsbDeviceUtil.getUsbDeviceList(mUSBMonitor, mContext);
                if (usbDeviceList != null && usbDeviceList.size() > 0) {
                    log.E("requestPermission。。。。size:" + usbDeviceList.size());
                    int selectCount = usbDeviceList.size() - 1;
                    this.selectUsbDevice = usbDeviceList.get(selectCount);
                    mUSBMonitor.requestPermission(this.selectUsbDevice);

                }
            }
    }
    /**
     * 开始采集
     * @return
     */
    public boolean startCapture(){
        try {
            synchronized (mSync) {
                Log.e("", "mOnDeviceConnectListener...startCapture");
                if (mUSBMonitor == null) {
                    initCamera();
                }
                if (mCameraHandler != null) {
                    mCameraHandler.startPreview();
                    Log.e("", "mOnDeviceConnectListener...startCapture...22");
                }
            }



        }catch (Exception e){
            e.printStackTrace();
            UsbCameraCaptureListener.getInstance().onUsbCameraCaptureListener(true, UsbCameraEntry.USBCaptureError.ERROR_001);

        }
        return false;
    }

    /**
     * 关闭采集
     */
    public void stopCapture(){
        log.E("UsbVideoCapture...mOnDeviceConnectListener....stopCapture");
        try {
            synchronized (mSync) {
                if (mCameraHandler != null) {
                    mCameraHandler.stopPreview();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            UsbCameraCaptureListener.getInstance().onUsbCameraCaptureListener(true, UsbCameraEntry.USBCaptureError.ERROR_002);
        }
    }
    /**
     * 切换相机
     */
    public void switchCamera(){

    }

public void clearData(){
    if (mCameraHandler != null) {
        mCameraHandler.close();
        mCameraHandler.release();
        mCameraHandler = null;
    }
    if (mUSBMonitor != null) {
        mUSBMonitor.unregister();
        mUSBMonitor.destroy();
        mUSBMonitor = null;
    }
}

  public  void  addSurface(final int surfaceId, final Surface surface, final boolean isRecordable){
        if(mCameraHandler!=null) {
            log.E("UsbVideoCapture..addSurface....usbCamera");
            mCameraHandler.addSurface(surfaceId, surface, isRecordable);
        }
    }

    public void removeSurface(final int surfaceId){
        if(mCameraHandler!=null) {
            mCameraHandler.removeSurface(surfaceId);
        }
    }

    private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {

        }

        @Override
        public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
            log.E("UsbVideoCapture..mOnDeviceConnectListener.usbCamera....onConnect");
            try {
                synchronized (mSync) {
                    if (mCameraHandler != null) {
                        Log.e("","mOnDeviceConnectListener.....onConnect...open");
                        mCameraHandler.open(ctrlBlock);
                        Log.e("","mOnDeviceConnectListener.....onConnect...open.."+mCameraHandler.isOpened());
//                        mCameraHandler.startPreview();

                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                UsbCameraCaptureListener.getInstance().onUsbCameraCaptureListener(true, UsbCameraEntry.USBCaptureError.ERROR_001);
            }
        }

        @Override
        public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
            try {
                log.E("mOnDeviceConnectListener.....onDisconnect");
                synchronized (mSync) {
                    if (mCameraHandler != null) {
                        mCameraHandler.stopPreview();
                        mCameraHandler.close();
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                UsbCameraCaptureListener.getInstance().onUsbCameraCaptureListener(true, UsbCameraEntry.USBCaptureError.ERROR_002);
            }
        }

        @Override
        public void onDettach(final UsbDevice device) {
            log.E("mOnDeviceConnectListener.....onDettach");


        }

        @Override
        public void onCancel(final UsbDevice device) {
            log.E("mOnDeviceConnectListener....onCancel");

        }
    };



    /**
     * 获取当前采集的相机
     * @return
     */
    public UsbDevice getSelectUsbDevice(){
        return this.selectUsbDevice;
    }



//    @Override
//    public void onPreviewResult(byte[] data, int width, int height) {
//        UsbCameraCaptureListener.getInstance().onPreviewCallback(data,width,height);
//    }

    @Override
    public void onPreviewResult(ByteBuffer data, int width, int height) {
        UsbCameraCaptureListener.getInstance().onPreviewCallback(data,width,height);
    }

    @Override
    public void onPreviewResult(ByteBuffer y, ByteBuffer u, ByteBuffer v, int width, int height) {
        UsbCameraCaptureListener.getInstance().onPreviewCallback(y,u,v,width,height);
    }

    @Override
    public void onPreviewResult(byte[] data,int width,int height) {
        UsbCameraCaptureListener.getInstance().onPreviewCallback(data,width,height);

    }
}
