package com.swimmi.common;

import java.io.File;

import android.text.TextUtils;

public class BaseConfigure {
	/**
	 * 是否有SD卡标识，HY 广播改变其状态
	 */
	public static boolean isSdCard = false;
	/**
	 * 是否有网络标识
	 */
	public static boolean isConnected = false;
	/**
	 * 是否WiFi
	 */
	public static boolean isWiFi = false;
	/**
	 * 图片缓存目录
	 */
	public static String ImagePath = "mnt/sdcard/WindNote/";
	/**
	 * 临时下载目录
	 */
	public static String DownloadPath = "mnt/sdcard/WindNote/download/";
	/**
	 * 图片缓存目录 imageCache/
	 */
	public static final String APP_PATH_IMAGE = "imageCache" + File.separator;
	/**
	 * 临时下载目录 download/
	 */
	public static final String APP_PATH_DOWNLOAD = "download" + File.separator;
	
	/**
	 * 加载高清图片的标识
	 */
	public static boolean Hi_Q_LOAD = false;
	/**
	 * 是否开始debug
	 */
	protected static boolean DEBUG = false;

	/**
	 * 是否调试模式(程序入口需调用，否则不能全屏蔽）
	 * 
	 * @return
	 */
	public static boolean isDebug() {
		return DEBUG;
	}

	/**
	 * 移除代码配置
	 */
	public static void removeProxy() {
		System.setProperty("http.proxyHost", "");
		System.setProperty("http.proxyPort", "");
	}

	/**
	 * 设置代理
	 */
	@SuppressWarnings("deprecation")
	public static void setProxy() {
		if (!TextUtils.isEmpty(android.net.Proxy.getDefaultHost())) {
			// 设置HTTP访问要使用的代理服务器的地址
			System.setProperty("http.proxyHost", android.net.Proxy.getDefaultHost());
		}
		if (android.net.Proxy.getDefaultPort() != -1) {
			// 设置HTTP访问要使用的代理服务器的端口
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
