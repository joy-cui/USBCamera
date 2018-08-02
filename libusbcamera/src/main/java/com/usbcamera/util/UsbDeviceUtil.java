package com.usbcamera.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;
import android.widget.Toast;

import com.jiangdg.libusbcamera.R;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;

import java.util.List;

/**
 * Created by cui on 2018/6/4.
 */

public class UsbDeviceUtil {

    public  static List<UsbDevice> getUsbDeviceList(USBMonitor mUSBMonitor, Context context) {
        try {
            List<DeviceFilter> deviceFilters = DeviceFilter
                    .getDeviceFilters(context, R.xml.device_filter);
            if (mUSBMonitor == null || deviceFilters == null)
                return null;
            Log.e("", "getUsbDeviceList...." + mUSBMonitor);
            return mUSBMonitor.getDeviceList(deviceFilters.get(0));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void requestPermission(Context context, USBMonitor mUSBMonitor, int index) {
        try {
            List<UsbDevice> devList = getUsbDeviceList(mUSBMonitor, context);
            if (devList == null || devList.size() == 0) {
                return;
            }
            int count = devList.size();
            if (index >= count)
                new IllegalArgumentException("index illegal,should be < devList.size()");
            if (mUSBMonitor != null) {
                mUSBMonitor.requestPermission(getUsbDeviceList(mUSBMonitor, context).get(index));
                Toast.makeText(context,"requestPermission.."+count,Toast.LENGTH_LONG).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
