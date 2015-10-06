package com.thesonofthom.myboardgames.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.Game.PropertyData;
import com.thesonofthom.myboardgames.asynctask.AsyncTaskFixed;
import com.thesonofthom.myboardgames.asynctask.LoadGamesTask;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;
import com.thesonofthom.myboardgames.tools.FileTools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
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
		// TODO Auto-generated method stub
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
