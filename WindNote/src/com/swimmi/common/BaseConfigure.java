package com.swimmi.common;

import java.io.File;

import android.text.TextUtils;

public class BaseConfigure {
	/**
	 * �Ƿ���SD����ʶ��HY �㲥�ı���״̬
	 */
	public static boolean isSdCard = false;
	/**
	 * �Ƿ��������ʶ
	 */
	public static boolean isConnected = false;
	/**
	 * �Ƿ�WiFi
	 */
	public static boolean isWiFi = false;
	/**
	 * ͼƬ����Ŀ¼
	 */
	public static String ImagePath = "mnt/sdcard/WindNote/";
	/**
	 * ��ʱ����Ŀ¼
	 */
	public static String DownloadPath = "mnt/sdcard/WindNote/download/";
	/**
	 * ͼƬ����Ŀ¼ imageCache/
	 */
	public static final String APP_PATH_IMAGE = "imageCache" + File.separator;
	/**
	 * ��ʱ����Ŀ¼ download/
	 */
	public static final String APP_PATH_DOWNLOAD = "download" + File.separator;
	
	/**
	 * ���ظ���ͼƬ�ı�ʶ
	 */
	public static boolean Hi_Q_LOAD = false;
	/**
	 * �Ƿ�ʼdebug
	 */
	protected static boolean DEBUG = false;

	/**
	 * �Ƿ����ģʽ(�����������ã�������ȫ���Σ�
	 * 
	 * @return
	 */
	public static boolean isDebug() {
		return DEBUG;
	}

	/**
	 * �Ƴ���������
	 */
	public static void removeProxy() {
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");
	}

	/**
	 * ���ô���
	 */
	@SuppressWarnings("deprecation")
	public static void setProxy() {
		if (!TextUtils.isEmpty(android.net.Proxy.getDefaultHost())) {
			// ����HTTP����Ҫʹ�õĴ���������ĵ�ַ
			System.setProperty("http.proxyHost", android.net.Proxy.getDefaultHost());
		}
		if (android.net.Proxy.getDefaultPort() != -1) {
			// ����HTTP����Ҫʹ�õĴ���������Ķ˿�
			System.setProperty("http.proxyPort", String.valueOf(android.net.Proxy.getDefaultPort()));
		}
	}
	
	public static boolean getSdCard(){
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			isSdCard = true;
		}
		return isSdCard;
	}
}
