package com.thesonofthom.myboardgames.asynctask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.GamePool.GameGroup;
import com.thesonofthom.myboardgames.activities.SplashScreenActivity;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;
import com.thesonofthom.myboardgames.tools.FileTools;

public class LoadGamesTask extends AsyncTaskFixed<Void, Integer, Boolean>
{
	private static final String TAG = "LoadGamesTask";
	
	private File gameDirectory;
	private File[] gameList;
	private BGGXMLParser parser;
	
	private SplashScreenActivity splashScreenActivity;
	
	public LoadGamesTask(Activity activity, BGGXMLParser existingParser)
	{
		super(TAG);
		gameDirectory = activity.getExternalFilesDir(FileTools.GAME_STORAGE_DIRECTORY);
		parser = existingParser;
		if(parser == null)
		{
			parser = new BGGXMLParser(activity, null);
		}
		
		if(gameDirectory != null)
		{
			gameList = gameDirectory.listFiles();
		}
		else
		{
			gameList = new File[0];
			Toast.makeText(activity, "Unable to read SD Card!", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	public LoadGamesTask(SplashScreenActivity splash)
	{
		this(splash, null);
		splashScreenActivity = splash;
	}
	
	public File getFileFromGame(Game game)
	{
		return new File(gameDirectory, game.getFileName());
	}
	
	public File getFileDirectory()
	{
		return gameDirectory;
	}

	@Override
	public void peformOnPreExecute()
	{
		int size = gameList.length;
		if(size > 0)
		{
			if(splashScreenActivity != null)
			{
				splashScreenActivity.getProgressBar().setIndeterminate(false);
				splashScreenActivity.getProgressBar().setMax(size);
			}

		}
	}

	@Override
	public Boolean performDoInBackground(Void... params)
	{
		Game.enableDontMarkAsDirtyLock();
		GamePool.getInstance().lock();
		//to clear out any potential infinite loops, clear the local game cache
		GamePool.getInstance().getCache(GameGroup.AllSavedGames).clear();
		for(int i = 0; i < gameList.length; i++)
		{
			try
			{
				File gameFile = gameList[i];
				Game game = Game.getGameFromFileName(gameFile.getName());
				if(game == null || game.getStatus() < Game.STATUS_FULL)
				{
					loadGame(gameFile);
				}
				else
				{
					GamePool.getInstance().addLocalGame(game); //re-add game just in case
				}
				
				publishProgress(i);
			}
			catch(Exception e)
			{
				Log.e(TAG, Log.getStackTraceString(e));
			}
		}
		Game.disabledontMarkAsDirtyLock();
		GamePool.getInstance().unlock();
		GamePool.getInstance().refreshAllFilters();
		return true;
	}
	
	public boolean loadGame(File fileName) throws ParserConfigurationException, SAXException, IOException
	{
		FileInputStream fin = new FileInputStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) 
	    {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    
		boolean success = parser.parseGameList(sb.toString(), XMLType.GAME_INFO);
		if(success)
		{
			Game game = Game.getGameFromFileName(fileName.getName());
			GamePool.getInstance().addLocalGame(game);
		}
		return success;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values)
	{
		super.onProgressUpdate(values);
		if(values.length > 0)
		{
			if(splashScreenActivity != null)
			{
				splashScreenActivity.getProgressBar().setProgress(values[0]);
			}
			
		}
	}

	@Override
	protected void performOnPostExecute(Boolean result)
	{
		for (Map.Entry<Integer, Game> entry : GamePool.getInstance().getAllGamesHashMap().entrySet()) 
		{
			entry.getValue().clearDirty();
		}
		if(splashScreenActivity != null)
		{
			splashScreenActivity.launchMainActivity(0);
		}
		
	}

	@Override
	public void performOnCancelled(Boolean result)
	{
		//nothing to do
	}
}