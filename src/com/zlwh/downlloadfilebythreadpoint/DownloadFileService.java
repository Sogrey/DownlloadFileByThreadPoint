package com.zlwh.downlloadfilebythreadpoint;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * �ļ����ص�service
 * 
 * @author Sogrey
 * 
 */
public class DownloadFileService extends Service {

	private DownloadFileUtils downloadFileUtils;// �ļ����ع�����
	private String filePath;// �����ڱ��ص�·��
	private NotificationManager notificationManager;// ״̬��֪ͨ������
	private Notification notification;// ״̬��֪ͨ
	private RemoteViews remoteViews;// ״̬��֪ͨ��ʾ��view
	private final int notificationID = 1;// ֪ͨ��id
	private final int updateProgress = 1;// ����״̬�������ؽ���
	private final int downloadSuccess = 2;// ���سɹ�
	private final int downloadError = 3;// ����ʧ��
	private final String TAG = "DownloadFileService";
	private Timer timer;// ��ʱ�������ڸ������ؽ���
	private TimerTask task;// ��ʱ��ִ�е�����
	protected String url;
	private GotFJReceiver mGotFJReceiver;

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onCreate() {
		regReceiver();
		init();
	}

	private void regReceiver() {
		mGotFJReceiver = new GotFJReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.action.download_content");
		registerReceiver(mGotFJReceiver, filter);// ע������������չ㲥
	}

	@Override
	public void onDestroy() {
		if (mGotFJReceiver != null) {
			unregisterReceiver(mGotFJReceiver);
		}
		Log.i(TAG, TAG + " is onDestory...");
		super.onDestroy();
	}

	private void init() {
		
		Intent intent=new Intent(this,MainActivity.class);//������������������
		  PendingIntent pIntent=PendingIntent.getActivity(this, 0, intent, 0);
		  
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;// ����֪ͨ��Ϣ��ͼ��
		notification.tickerText = "�������ء�����";// ����֪ͨ��Ϣ�ı���
		notification.contentIntent=pIntent;
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.down_notification);
		remoteViews.setImageViewResource(R.id.IconIV, R.drawable.ic_launcher);
		timer = new Timer();
		task = new TimerTask() {

			@Override
			public void run() {
				handler.sendEmptyMessage(updateProgress);
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	public void download() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				downloadFileUtils = new DownloadFileUtils(url, filePath,
						url.split("/")[url.split("/").length-1], 1, callback);
				downloadFileUtils.downloadFile();
			}
		}).start();
		timer.schedule(task, 500, 500);
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == updateProgress) {// �������ؽ���
				long fileSize = downloadFileUtils.getFileSize();
				long totalReadSize = downloadFileUtils.getTotalReadSize();
				if (totalReadSize > 0) {
					float size = (float) totalReadSize * 100 / (float) fileSize;
					DecimalFormat format = new DecimalFormat("0.00");
					String progress = format.format(size);
					remoteViews.setTextViewText(R.id.progressTv, "������"
							+ progress + "%");
					remoteViews.setProgressBar(R.id.progressBar, 100,
							(int) size, false);
					notification.contentView = remoteViews;
					notificationManager.notify(notificationID, notification);
				}
			} else if (msg.what == downloadSuccess) {// �������
				remoteViews.setTextViewText(R.id.progressTv, "�������");
				remoteViews.setProgressBar(R.id.progressBar, 100, 100, false);
				notification.contentView = remoteViews;
				notificationManager.notify(notificationID, notification);
				if (timer != null && task != null) {
					timer.cancel();
					task.cancel();
					timer = null;
					task = null;
				}
				
				// �����װPendingIntent
				Uri uri = Uri.fromFile(new File(filePath+url.split("/")[url.split("/").length-1]));
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(uri,
						"application/vnd.android.package-archive");

				PendingIntent updatePendingIntent = PendingIntent.getActivity(
						DownloadFileService.this, 0, installIntent, 0);

				notification.defaults = Notification.DEFAULT_SOUND;// ��������
				notification.setLatestEventInfo(DownloadFileService.this,
						"QQ", "�������,�����װ��", updatePendingIntent);
				notificationManager.notify(0, notification);
				
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			} else if (msg.what == downloadError) {// ����ʧ��
				if (timer != null && task != null) {
					timer.cancel();
					task.cancel();
					timer = null;
					task = null;
				}
				notificationManager.cancel(notificationID);
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			}
		}

	};
	/**
	 * ���ػص�
	 */
	DownloadFileCallback callback = new DownloadFileCallback() {

		@Override
		public void downloadSuccess(Object obj) {
			handler.sendEmptyMessage(downloadSuccess);
		}

		@Override
		public void downloadError(Exception e, String msg) {
			handler.sendEmptyMessage(downloadError);
		}
	};

	/** ��ȡ������Ƶ�������㲥���� */
	public class GotFJReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// intent.putExtra("url",
			// "http://125.76.237.48:8120/ftpfile/20150309101807.png");
			// intent.putExtra("path", Environment.getExternalStorageDirectory()
			// + "/aaa");
			url = intent.getStringExtra("url");
			filePath = intent.getStringExtra("path");
			download();
		}
	}
}
