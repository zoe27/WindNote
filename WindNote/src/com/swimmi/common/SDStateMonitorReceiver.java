package com.swimmi.common;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.swimmi.common.BaseConfigure.APP_PATH_DOWNLOAD;
import static com.swimmi.common.BaseConfigure.APP_PATH_IMAGE;

public class SDStateMonitorReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		// 存储卡被卸载
		if (Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
			// 无SD卡情况
			BaseConfigure.isSdCard = false;
			if (BaseConfigure.isDebug()) {
				Tips.tipShort(context, "SD卡被弹出!");
			}
		} else {
			// 有存储卡情况
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				BaseConfigure.isSdCard = true;
				if (BaseConfigure.isDebug()) {
					Tips.tipShort(context, "SD已加载!");
				}
			}
		}

		if (BaseConfigure.isSdCard) {
			// 有SD卡的情形
			BaseConfigure.ImagePath = new StringBuilder(context.getExternalCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
			BaseConfigure.DownloadPath = new StringBuilder(context.getExternalCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
		} else {
			// 无SD卡的情形
			BaseConfigure.ImagePath = new StringBuilder(context.getCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
			BaseConfigure.DownloadPath = new StringBuilder(context.getCacheDir().getPath()).append(File.separator).append(APP_PATH_DOWNLOAD).toString();
		}
	}

}
