package com.zlwh.downlloadfilebythreadpoint;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;

public class MainActivity extends Activity {

	private Button downloadBtn;
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Intent intent  = new Intent();
		intent.setClass(getApplicationContext(), DownloadFileService.class);
		startService(intent);
		downloadBtn = (Button) this.findViewById(R.id.downloadBtn);
		downloadBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("url", "http://gdown.baidu.com/data/wisegame/4f9b25fb0e093ac6/QQ_220.apk");
				intent.putExtra("path", Environment.getExternalStorageDirectory() + "/aaa");
				intent.setAction("android.action.download_content");
				sendBroadcast(intent);
				downloadBtn.setEnabled(false);
			}
		});
		
	}

}
