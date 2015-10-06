package com.thesonofthom.myboardgames.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.asynctask.DialogAsyncTask;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser;
import com.thesonofthom.myboardgames.bgg.BGGXMLParser.XMLType;

public class InternetTools
{
	public static final String TAG = "InternetTools";
	private Context context;

	private Resources r;
	private Settings settings;
	DialogAsyncTask task;
	
	public InternetTools(Context context, DialogAsyncTask task)
	{
		this.context = context;

		r = context.getResources();
		settings = new Settings(context);
		this.task = task;
	}
	
	public String getXmlFromInternet(String url) throws ClientProtocolException, IOException
	{
		if (DialogAsyncTask.isDone()) { return null; }
		Log.i(TAG, "getXmlFromInternet: " + url);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		HttpResponse httpResponse = httpClient.execute(httpPost);
		if (DialogAsyncTask.isDone()) { return null; }
		HttpEntity httpEntity = httpResponse.getEntity();
		String text = EntityUtils.toString(httpEntity);
		Log.i(TAG, "Done");

		// if(text != null)
		// {
		// //fix the "only one root element allowed error
		// int endOfHeader = text.indexOf("?>") + 2;
		// String header = text.substring(0, endOfHeader);
		// String xmlBody = text.substring(endOfHeader);
		// text = header + "<root>" + xmlBody + "</root>";
		// Log.i(TAG, text.substring(0, endOfHeader + 10));
		// Log.i(TAG, text.substring(text.length() - 20));
		// }
		//
		return text;
	}
	
	public boolean retrieveExpandedGameInfo(String objectIdList, BGGXMLParser parser) 
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException
	{

		String nextUrl = getGamesUrl(objectIdList, false);
		
		String xml = getXmlFromInternet(nextUrl);
		if(DialogAsyncTask.isDone())
		{
			return false;
		}
		task.updateProgress("Parsing results...");
		return parser.parseGameList(xml, XMLType.GAME_INFO);
	}

	public String getGamesUrl(String gameIdList)
	{
		return getGamesUrl(gameIdList, false);
	}
	public String getGamesUrl(String gameIdList, boolean loadComments)
	{
		String url = r.getString(R.string.bgg_url) + r.getString(R.string.bgg_url_game) + gameIdList;
		if(loadComments)
		{
			url += "&comments=1";
		}
		return url;
	}
	
	public String getSearchUrl(String query)
	{
		String searchTerm = null;
		try
		{
			searchTerm = URLEncoder.encode(query, "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			Log.e("Error", e.getMessage());
		}
		return r.getString(R.string.bgg_url) + r.getString(R.string.bgg_url_search) + searchTerm;
	}
	
	public String getUserCollectionUrl(String user)
	{
		return r.getString(R.string.bgg_url) + r.getString(R.string.bgg_url_user) + user + "&own=1";
	}
	
	public String getObjectIdList(GameCache cache, int limit)
	{
		String objectIds = "";
		int currentPosition = cache.getPosition();
		if(cache.isUseEntireList())
		{
			currentPosition = 0;
		}
		Log.i(TAG, "Current List position: " + currentPosition);
		Log.i(TAG, "Cached list: " + cache);
		boolean firstEntry = true;
		for(int i = 0; i < limit && currentPosition < cache.getList().size(); i++)
		{
			Game game = cache.getList().get(currentPosition);
			if(game.getStatus() < Game.STATUS_FULL)
			{
				if(!firstEntry)
				{
					objectIds += ",";
				}
				firstEntry = false;
				objectIds += game.getObjectId();
			}
			else
			{
				Log.i(TAG, "Already fetched " + game.getObjectId());
			}
			//if the status is at least full, we have already retrieved the full data on the game
			currentPosition++;
		}
		//cache.setPosition(currentPosition);

		return objectIds;
	}
}
