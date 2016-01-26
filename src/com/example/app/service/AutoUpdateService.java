package com.example.app.service;

import com.example.app.activity.R;
import com.example.app.activity.WeatherActivity;
import com.example.app.receiver.AutoUpdateReceiver;
import com.example.app.util.HttpCallbackListener;
import com.example.app.util.HttpUtil;
import com.example.app.util.Utility;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		
		super.onCreate();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				updateWeather();
				
			}
		}).start();
		AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour = 8*60*60*1000;
		long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	private void updateWeather(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode = prefs.getString("weather_code", "");
		String address  = "http://www.weather.com.cn/data/cityinfo/" +weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
				String city = prefs.getString("city_name", "");
				String temp1 = prefs.getString("temp1", "");
				String temp2 = prefs.getString("temp2", "");
				Notification notification = new Notification(R.drawable.ic_launcher, "notification", System.currentTimeMillis());
				Intent notificationIntent = new Intent(AutoUpdateService.this, WeatherActivity.class);
				PendingIntent pendIntent = PendingIntent.getActivity(AutoUpdateService.this, 0, notificationIntent, Notification.FLAG_AUTO_CANCEL );
				notification.setLatestEventInfo(AutoUpdateService.this, "weather", city+temp1+"~"+temp2, pendIntent);
				startForeground(1, notification);
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	

}
