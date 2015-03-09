package com.zlwh.downlloadfilebythreadpoint;

/**
 * 下载回调
 * @author Sogrey
 *
 */
public interface DownloadFileCallback {
	void downloadSuccess(Object obj);//下载成功
	void downloadError(Exception e,String msg);//下载失败
}
