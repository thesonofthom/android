package com.thesonofthom.myboardgames.activities;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.asynctask.LoadGamesTask;
import com.thesonofthom.myboardgames.tools.FileTools;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SplashScreenActivity extends BaseActivity
{
	private static final String TAG = "SplashScreenActivity";

	
	private ProgressBar progressBar;

	public SplashScreenActivity()
	{
		super(TAG);
	}
	
	public ProgressBar getProgressBar()
	{
		return progressBar;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		progressBar = (ProgressBar)findViewById(R.id.progressBar1);
		getActionBar().hide();
		if(FileTools.isExternalStorageReadable())
		{
			LoadGamesTask task = new LoadGamesTask(this);
			task.execute();
		}
		else
		{
			Toast.makeText(this, "SD Card is not mounted!", Toast.LENGTH_LONG).show();
			launchMainActivity(5000);
		}
	}
	
	public void launchMainActivity(int delayInMs)
	{
		 new Handler().postDelayed(new Runnable() {
             @Override
             public void run() 
             {

         		Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
        		startActivity(i);
        		// close this activity
        		finish();
             }
         }, delayInMs);

	}

}
