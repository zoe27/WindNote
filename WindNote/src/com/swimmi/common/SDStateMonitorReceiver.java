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
		// �洢����ж��
		if (Intent.ACTION_MEDIA_EJECT.equals(action) || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
			// ��SD�����
			BaseConfigure.isSdCard = false;
			if (BaseConfigure.isDebug()) {
				Tips.tipShort(context, "SD��������!");
			}
		} else {
			// �д洢�����
			if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				BaseConfigure.isSdCard = true;
				if (BaseConfigure.isDebug()) {
					Tips.tipShort(context, "SD�Ѽ���!");
				}
			}
		}

		if (BaseConfigure.isSdCard) {
			// ��SD��������
			BaseConfigure.ImagePath = new StringBuilder(context.getExternalCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
			BaseConfigure.DownloadPath = new StringBuilder(context.getExternalCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
		} else {
			// ��SD��������
			BaseConfigure.ImagePath = new StringBuilder(context.getCacheDir().getPath()).append(File.separator).append(APP_PATH_IMAGE).toString();
			BaseConfigure.DownloadPath = new StringBuilder(context.getCacheDir().getPath()).append(File.separator).append(APP_PATH_DOWNLOAD).toString();
		}
	}

}
