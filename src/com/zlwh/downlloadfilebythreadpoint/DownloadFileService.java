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
 * 文件下载的service
 * 
 * @author Sogrey
 * 
 */
public class DownloadFileService extends Service {

	private DownloadFileUtils downloadFileUtils;// 文件下载工具类
	private String filePath;// 保存在本地的路径
	private NotificationManager notificationManager;// 状态栏通知管理类
	private Notification notification;// 状态栏通知
	private RemoteViews remoteViews;// 状态栏通知显示的view
	private final int notificationID = 1;// 通知的id
	private final int updateProgress = 1;// 更新状态栏的下载进度
	private final int downloadSuccess = 2;// 下载成功
	private final int downloadError = 3;// 下载失败
	private final String TAG = "DownloadFileService";
	private Timer timer;// 定时器，用于更新下载进度
	private TimerTask task;// 定时器执行的任务
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
		registerReceiver(mGotFJReceiver, filter);// 注册接收器，接收广播
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
		
		Intent intent=new Intent(this,MainActivity.class);//点击进度条，进入程序
		  PendingIntent pIntent=PendingIntent.getActivity(this, 0, intent, 0);
		  
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		notification.icon = R.drawable.ic_launcher;// 设置通知消息的图标
		notification.tickerText = "正在下载。。。";// 设置通知消息的标题
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
			if (msg.what == updateProgress) {// 更新下载进度
				long fileSize = downloadFileUtils.getFileSize();
				long totalReadSize = downloadFileUtils.getTotalReadSize();
				if (totalReadSize > 0) {
					float size = (float) totalReadSize * 100 / (float) fileSize;
					DecimalFormat format = new DecimalFormat("0.00");
					String progress = format.format(size);
					remoteViews.setTextViewText(R.id.progressTv, "已下载"
							+ progress + "%");
					remoteViews.setProgressBar(R.id.progressBar, 100,
							(int) size, false);
					notification.contentView = remoteViews;
					notificationManager.notify(notificationID, notification);
				}
			} else if (msg.what == downloadSuccess) {// 下载完成
				remoteViews.setTextViewText(R.id.progressTv, "下载完成");
				remoteViews.setProgressBar(R.id.progressBar, 100, 100, false);
				notification.contentView = remoteViews;
				notificationManager.notify(notificationID, notification);
				if (timer != null && task != null) {
					timer.cancel();
					task.cancel();
					timer = null;
					task = null;
				}
				
				// 点击安装PendingIntent
				Uri uri = Uri.fromFile(new File(filePath+url.split("/")[url.split("/").length-1]));
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setDataAndType(uri,
						"application/vnd.android.package-archive");

				PendingIntent updatePendingIntent = PendingIntent.getActivity(
						DownloadFileService.this, 0, installIntent, 0);

				notification.defaults = Notification.DEFAULT_SOUND;// 铃声提醒
				notification.setLatestEventInfo(DownloadFileService.this,
						"QQ", "下载完成,点击安装。", updatePendingIntent);
				notificationManager.notify(0, notification);
				
				stopService(new Intent(getApplicationContext(),
						DownloadFileService.class));// stop service
			} else if (msg.what == downloadError) {// 下载失败
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
	 * 下载回调
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

	/** 获取到（视频）付件广播监听 */
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
