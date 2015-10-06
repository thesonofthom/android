package com.thesonofthom.myboardgames;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GameSorter.SortOption;
import com.thesonofthom.myboardgames.GameSorter.SortOrder;
import com.thesonofthom.myboardgames.tools.ParcelTools;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class GameCache //implements Parcelable
{
	private static final String TAG = "GameList";
	private HashMap<Integer, Game> cache;
	
	private List<Game> list;
	private boolean useEntireList;
	int currentPosition = 0;
	
	private String name;

	private SortOption sortOption;
	private SortOrder sortOrder;
	
	public GameCache(String name)
	{
		initialize(name);
	}
	
	private void initialize(String name)
	{
		cache = new HashMap<Integer, Game>();
		list = new LinkedList<Game>();
		currentPosition = 0;
		useEntireList = false;
		this.name = name;
		sortOption = null;
		sortOrder = SortOrder.ASCENDING;
	}
	
	public boolean setSortOptions(SortOption option, SortOrder order)
	{
		if(sortOption != option || sortOrder != order)
		{
			//Log.i(TAG, "Setting sort options to: " + option + ", " + order);
			sortOption = option;
			sortOrder = order;
			return true;
		}
		return false;
	}
	
	public void sort()
	{
		GameSorter.sort(list, sortOption, sortOrder);
	}
	
	public void clear()
	{
		list.clear();
		cache.clear();
	}
	

	public void cache(Game game)
	{
		int objectid = game.getObjectId();
		
		Game oldGame = cache.get(objectid);

		if(oldGame == null || oldGame.getStatus() < game.getStatus())
		{
			cache.put(game.getObjectId(), game);
			
			if(oldGame != null)
			{
				list.remove(oldGame);
			}
			
			int targetPosition;

			for (targetPosition = 0; targetPosition < list.size(); targetPosition++)
			{
				if (game.compareTo(list.get(targetPosition), sortOption,
						sortOrder) <= 0)
				// if (objectid <= list.get(targetPosition).getObjectId())
				{
					break;
				}
			}
			list.add(targetPosition, game);

		}
	}
	
	public Game remove(Game game)
	{
		Game removedGame = cache.remove(game.getObjectId());
		for (int position = 0; position < list.size(); position++)
		{
			if(game.getObjectId() == list.get(position).getObjectId())
			{
				list.remove(position);
				if(currentPosition >= position && currentPosition > 0)
				{
					currentPosition--;
				}
				break;
			}
		}
		return removedGame;
	}
	
	public List<Game> getList()
	{
		return list;
	}
	
	public int getSize()
	{
		return list.size();
	}


	public int getPosition()
	{
		if(useEntireList)
		{
			return list.size();
		}
		return currentPosition;
	}
	
	public void updatePosition(int number)
	{
		int newPosition = currentPosition + number;
		//limit the position to the size of the list
		if(newPosition > list.size())
		{
			newPosition = list.size();
		}
		currentPosition = newPosition;
	}
	
	public void setPosition(int position)
	{
		//limit the position to the size of the list
		if(position > list.size())
		{
			position = list.size();
		}
		currentPosition = position;
	}
	
	public void useEntireList(boolean enable)
	{
		useEntireList = enable;
	}
	
	public boolean isUseEntireList()
	{
		return useEntireList;
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder();
		s.append(name).append(" [Size: ").append(list.size()).append(" Position: ").append(currentPosition).append("]: ");
		for(Game game : list)
		{
			s.append(game.getObjectId()).append("(").append(game.getStatus()).append("),");
		}
		return s.toString();
	}
	
	
	public int getMax(Property property)
	{
		int max = 0;
		for(Game game : list)
		{
			int val = game.getInt(property);
			if(val > max)
			{
				max = val;
			}
		}
		return max;
	}
	
	public int getMin(Property property)
	{
		int min = Integer.MAX_VALUE;
		for(Game game : list)
		{
			int val = game.getInt(property);
			if(val < min)
			{
				min = val;
			}
		}
		if(min == Integer.MAX_VALUE)
		{
			min = 0;
		}
		return min;
	}
	
	//Parcel Stuff
//	
//	public GameCache(Parcel parcel)
//	{
//		readFromParcel(parcel);
//	}
//
//	@Override
//	public int describeContents()
//	{
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags)
//	{
//		dest.writeString(name);
//		dest.writeInt(currentPosition);
//		ParcelTools.writeBoolean(dest, useEntireList);
//		ParcelTools.writeEnum(dest, sortOption);
//		ParcelTools.writeEnum(dest, sortOrder);
//		Game[] gameArray = list.toArray(new Game[list.size()]);
//		dest.writeTypedArray(gameArray, flags);
//	}
//	
//	
//	public void readFromParcel(Parcel p)
//	{
//		//Log.i(TAG, "readFromParcel");
//		String name = p.readString();
//		//Log.i(TAG, "name = " + name);
//		initialize(name);
//		currentPosition = p.readInt();
//		//Log.i(TAG, "currentPosition = " + currentPosition);
//		useEntireList = ParcelTools.readBoolean(p);
//		//Log.i(TAG, "useEntireList = " + useEntireList);
//		sortOption = ParcelTools.readEnum(p, SortOption.class);
//		sortOrder = ParcelTools.readEnum(p, SortOrder.class);
//		
//		Game[] gameArray = p.createTypedArray(Game.CREATOR);
//		for(Game game : gameArray)
//		{
//			Log.i(TAG, "game = " + game);
//			Game actualGame = GamePool.getInstance().get(game.getObjectId()); //ensure only one copy of games
//			if(actualGame == null)
//			{
//				GamePool.getInstance().add(game);
//				actualGame = GamePool.getInstance().get(game.getObjectId());
//			}
//			cache(actualGame);
//		}
//		
////		while(p.dataAvail() > 0)
////		{
////			Game game = (Game)p.readParcelable(Game.class.getClassLoader());
////			Log.i(TAG, "game = " + game);
////			cache(game);
////		}
//	}
//	
//	public static final Creator<GameCache> CREATOR = new Creator<GameCache>()
//	{
//		@Override
//		public GameCache createFromParcel(Parcel source)
//		{
//			return new GameCache(source);
//		}
//
//		@Override
//		public GameCache[] newArray(int size)
//		{
//			return new GameCache[size];
//		}
//
//	};
//	
}
