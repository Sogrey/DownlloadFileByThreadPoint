package com.zlwh.downlloadfilebythreadpoint;

/**
 * ���ػص�
 * @author Sogrey
 *
 */
public interface DownloadFileCallback {
	void downloadSuccess(Object obj);//���سɹ�
	void downloadError(Exception e,String msg);//����ʧ��
}
