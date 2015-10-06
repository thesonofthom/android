package com.thesonofthom.myboardgames;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.GameSorter.SortOption;
import com.thesonofthom.myboardgames.GameSorter.SortOrder;

public class GameCache
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
}
