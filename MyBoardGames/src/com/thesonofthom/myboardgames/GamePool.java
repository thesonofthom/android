package com.thesonofthom.myboardgames;

import java.util.ArrayList;
import java.util.HashMap;

import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GameSorter.SortOption;
import com.thesonofthom.myboardgames.GameSorter.SortOrder;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.asynctask.LoadGamesTask;
import com.thesonofthom.myboardgames.tools.Filter;

import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.util.Log;

public class GamePool
{
	public static enum GameGroup
	{
		AllSavedGames, //everything on disk
		AllGames, //all owned games (my games + games on loan)
		MyGames,
		GamesOnLoan;
	}
	
	private static String TAG = "GamePool";
	
	//file data
	
	private GameCache[] gameCaches;
	private HashMap<String, GameCache> onLoanCaches;
	//private HashMap<String, Filter> onLoanFilters;
	private GameCache searchCache;
	private String query;

	private Filter[] filters;

	private HashMap<Integer, Game> allGames;
	
	private LoadGamesTask loadGamesTask; //for getting the number of files
	
	//private HashMap<Integer, Game> oldSearchResults;
	
	private static GamePool instance = null;
	
	
	private GamePool()
	{
		allGames = new HashMap<Integer, Game>();
		gameCaches = new GameCache[GameGroup.values().length];
		filters = new Filter[GameGroup.values().length];
		for(int i = 0; i < gameCaches.length; i++)
		{
			GameGroup group = GameGroup.values()[i];
			gameCaches[i] = new GameCache(group + " cache");
			gameCaches[i].setSortOptions(SortOption.NAME, SortOrder.ASCENDING);
			gameCaches[i].useEntireList(true);
			filters[i] = new Filter(gameCaches[i]);
		}
		onLoanCaches = new HashMap<String, GameCache>();
		//onLoanFilters = new HashMap<String, Filter>();
	}
	
	public boolean verifyState(MainActivity activity)
	{
		if(loadGamesTask == null)
		{
			loadGamesTask = new LoadGamesTask(activity, null);
		}
		Log.i(TAG, "Verifying state...");
		int numberOfFiles = loadGamesTask.getFileDirectory().listFiles().length;
		int numberOfGames =  getCache(GameGroup.AllSavedGames).getSize();
		Log.i(TAG, "Number of files: " + numberOfFiles);
		Log.i(TAG, "Number of games: " + numberOfGames);
		return  numberOfFiles == numberOfGames;
	}
	
	public void add(Game game)
	{
		int objectid = game.getObjectId();
		Game oldGame = allGames.get(objectid);
		if(oldGame == null || oldGame.getStatus() < game.getStatus())
		{
			allGames.put(objectid, game);
		}
	}
	

	public void addLocalGame(Game game)
	{
		addGame(game, GameGroup.AllSavedGames);
		if(game.isOwned())
		{
			addGame(game, GameGroup.AllGames);
			if(game.isOnLoan())
			{
				addGame(game, GameGroup.GamesOnLoan);
			}
			else
			{
				addGame(game, GameGroup.MyGames);
			}
		}
	}
	
	private void addGame(Game game, GameGroup group)
	{
		getCache(group).cache(game);
		if(!lock)
		{
			getFilter(group).recalculateInitialValues(getCache(group));
		}
	}
	
	
	
	public void removeLocalGame(Game game)
	{
		for(GameGroup group : GameGroup.values())
		{
			removeGame(game, group);
		}
	}
	
	private void removeGame(Game game, GameGroup group)
	{
		if(getCache(group).remove(game) != null)
		{
			getFilter(group).recalculateInitialValues(getCache(group));
		}
	}
	
	public void loanGame(Game game)
	{
		removeGame(game, GameGroup.MyGames);
		addGame(game, GameGroup.GamesOnLoan);
	}
	
	public void returnLoanedGame(Game game)
	{
		removeGame(game, GameGroup.GamesOnLoan);
		addGame(game, GameGroup.MyGames);
	}
	
	public GameCache getCache(GameGroup option)
	{
		return gameCaches[option.ordinal()];

	}
	
	public GameCache getLoanGameCache(String lookupKey)
	{
		GameCache cache = onLoanCaches.get(lookupKey);
		if(cache == null)
		{
			cache = new GameCache(lookupKey + " cache");
			cache.setSortOptions(SortOption.NAME, SortOrder.ASCENDING);
			cache.useEntireList(true);
			
			for(Game game : getCache(GameGroup.GamesOnLoan).getList())
			{
				if(lookupKey.equals(game.get(Property.loanContact_lookup_key)))
				{
					cache.cache(game);
				}
			}
			onLoanCaches.put(lookupKey, cache);
		}
		return cache;
	}
	
//	public Filter getLoanGameFilter(String lookupKey)
//	{
//		Filter filter = onLoanFilters.get(lookupKey);
//		if(filter == null)
//		{
//			filter = new Filter(getLoanGameCache(lookupKey));
//			onLoanFilters.put(lookupKey, filter);
//		}
//		return filter;
//	}
	
	public void refreshAllFilters()
	{
		for(GameGroup group : GameGroup.values())
		{
			getFilter(group).recalculateInitialValues(getCache(group));
		}
	}
	
	public GameCache createSearchCache(String query)
	{
		return new GameCache(query + " Search Cache");
	}
	
	public void setSearchResultsCache(GameCache cache)
	{
		searchCache = cache;
	}
	
	public GameCache getSearchCache()
	{
		return searchCache;
	}
	
	public void setQuery(String query)
	{
		this.query = query;
	}
	
	public String getQuery()
	{
		return query;
	}
	
	
	public Filter getFilter(GameGroup option)
	{
		return filters[option.ordinal()];
	}

	public Game get(int objectid)
	{
		return allGames.get(objectid);
	}
	

	public static GamePool getInstance()
	{
		if(instance == null)
		{
			instance = new GamePool();
		}
		return instance;
	}
	
	public HashMap<Integer, Game> getAllGamesHashMap()
	{
		return allGames;
	}
	
	public ArrayList<String> getListOfFriendLookupKeys()
	{
		ArrayList<String> uniqueIds = new ArrayList<String>();
		GameCache cache  = GamePool.getInstance().getCache(GameGroup.GamesOnLoan);
		for(Game game : cache.getList())
		{
			String lookupKey = game.get(Property.loanContact_lookup_key);
			if(lookupKey != null && !uniqueIds.contains(lookupKey))
			{
				uniqueIds.add(lookupKey);
			}
		}
		return uniqueIds;
	}
	
	private volatile boolean lock = false;
	
	public void lock()
	{
		lock = true;
	}
	
	public void unlock()
	{
		lock = false;
	}
	
	private static final String LOCAL_GAMES = "LOCAL_GAMES";
	private static final String FILTER(GameGroup group) {return "FILTER_"+group.name();}
	public void saveState(Bundle bundle)
	{
//		for(GameGroup group : GameGroup.values())
//		{
//			bundle.putParcelable(FILTER(group), getFilter(group));
//		}
//		
//		for(Game game : getCache(GameGroup.AllSavedGames).getList())
//		{
//			game.setParcelMode(ParcelMode.MODE_LIST);
//		}
//		bundle.putParcelable(LOCAL_GAMES, getCache(GameGroup.AllSavedGames));
//		Log.i(TAG, "BUNDLE: " + bundle);
	}
	
	public void restoreState(Bundle bundle)
	{
//		if (bundle != null)
//		{
//			restoreLock = true;
//			Log.i(TAG, "BUNDLE: " + bundle);
//			for (GameGroup group : GameGroup.values())
//			{
//				filters[group.ordinal()] = bundle.getParcelable(FILTER(group));
//			}
//			GameCache games = bundle.getParcelable(LOCAL_GAMES);
//			if (games != null)
//			{
//				for (Game game : games.getList())
//				{
//					addLocalGame(game);
//				}
//			}
//			
//			for (GameGroup group : GameGroup.values())
//			{
//				filters[group.ordinal()].recalculateInitialValues(getCache(group));
//			}
//			restoreLock = false;
//		}
	}
}
