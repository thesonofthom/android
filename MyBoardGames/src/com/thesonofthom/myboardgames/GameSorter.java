package com.thesonofthom.myboardgames;

import java.util.Collections;
import java.util.List;

import com.thesonofthom.myboardgames.Game.Property;

public class GameSorter 
{
	public static enum SortOption
	{
		NAME(Property.name, "Name", false),
		MIN_PLAYERS(Property.minplayers, "Min Players", true),
		MAX_PLAYERS(Property.maxplayers, "Max Players", true),
		PLAYING_TIME(Property.playingtime, "Playing Time", true),
		//MIN_AGE(Property.age, "Min Age", true),
		YEAR(Property.yearpublished, "Year", true);
		
		Property property;
		boolean compareDecimal;
		String name;
		SortOption(Property p, String n, boolean c)
		{
			property = p;
			compareDecimal = c;
			name = n;
		}
		
		@Override
		public String toString() 
		{
			return name;
		}
	}
	
	public static enum SortOrder
	{
		ASCENDING,
		DESCENDING
	}
	
	public static void sortByObjectId(List<Game> list)
	{
		sort(list, null);
	}
	
	public static void sort(List<Game> list, SortOption option)
	{
		sort(list, option, SortOrder.ASCENDING);
	}
	
	public static void sort(List<Game> list, SortOption option, SortOrder order)
	{
		Game.setSortOptions(option, order);
		Collections.sort(list);
	}

}
