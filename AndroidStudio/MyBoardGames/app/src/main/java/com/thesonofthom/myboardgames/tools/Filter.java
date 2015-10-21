package com.thesonofthom.myboardgames.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.Game.PropertyData;

/**
 * Class that stores all of the state and logic for filtering a list of games based on select criteria
 * @author Kevin Thomson
 *
 */
public class Filter implements Parcelable
{
	private static final String TAG = "Filter";
	
	public enum FilterOption
	{
		Number_of_Players,
		Playing_Time,
		Year_Published,
		Category;
		
		public String toString() 
		{
			return name().replace("_", " ");
		};
		
		public static CharSequence[] filterOptions;
		static
		{
			FilterOption[] values = FilterOption.values();
			filterOptions = new String[FilterOption.values().length];
			for(int i =0; i < values.length; i++)
			{
				filterOptions[i] = values[i].toString();
			}
		}
		
		public static FilterOption get(int position)
		{
			return FilterOption.values()[position];
		}
	}
	
	private int initialMinPlayingTime;
	private int initialMaxPlayingTime;
	private int initialMinYear;
	private int initialMaxYear;
	private ArrayList<String> allCategories;
	
	
	private Integer numberOfPlayers;
	private Integer minPlayingTime;
	private Integer maxPlayingTime;
	private Integer minYear;
	private Integer maxYear;
	private ArrayList<String> categories;
	
	public Filter(GameCache cache)
	{
		if(cache != null)
		{
			recalculateInitialValues(cache);
		}
		numberOfPlayers = null;
		minPlayingTime = null;
		maxPlayingTime = null;
		minYear = null;
		maxYear = null;
		categories = new ArrayList<String>(allCategories.size());
	}
	
	public void recalculateInitialValues(GameCache cache)
	{
		initialMinPlayingTime = cache.getMin(Property.playingtime);
		initialMaxPlayingTime = cache.getMax(Property.playingtime);
		initialMinYear = cache.getMin(Property.yearpublished);
		initialMaxYear = cache.getMax(Property.yearpublished);
		allCategories = new ArrayList<String>();
		for(Game game : cache.getList())
		{
			List<PropertyData> categoryList = game.getList(Property.boardgamecategory);
			if(categoryList != null)
			{
				for(PropertyData data : categoryList)
				{
					if(!allCategories.contains(data.value))
					{
						allCategories.add(data.value);
					}
				}
			}
		}
		Collections.sort(allCategories);
	}
	
	public boolean isFilterActive(FilterOption option)
	{
		switch(option)
		{
			case Number_of_Players:
				return numberOfPlayers != null;
			case Playing_Time:
				return minPlayingTime != null && maxPlayingTime != null;
			case Year_Published:
				return minYear != null && maxYear != null;
			case Category:
				return categories != null && !categories.isEmpty();
		}
		return false;
	}

	public void setNumberOfPlayers(int num)
	{
		numberOfPlayers = num;
	}
	
	public Integer getNumberOfPlayers()
	{
		return numberOfPlayers;
	}
	
	public void setPlayingTime(int min, int max)
	{
		minPlayingTime = min;
		maxPlayingTime = max;
	}
	
	public int getInitialMinPlayingTime()
	{
		return initialMinPlayingTime;
	}
	
	public int getInitialMaxPlayingTime()
	{
		return initialMaxPlayingTime;
	}
	
	public Integer getMinPlayingTime()
	{
		return minPlayingTime;
	}
	
	public Integer getMaxPlayingTime()
	{
		return maxPlayingTime;
	}
	
	public void setYear(int min, int max)
	{
		minYear = min;
		maxYear = max;
	}
	
	public int getInitialMinYear()
	{
		return initialMinYear;
	}
	
	public int getInitialMaxYear()
	{
		return initialMaxYear;
	}
	
	public Integer getMinYear()
	{
		return minYear;
	}
	
	public Integer getMaxYear()
	{
		return maxYear;
	}
	
	public void addCategory(String category)
	{
		categories.add(category);
	}
	
	public ArrayList<String> getCategories()
	{
		return categories;
	}
	
	public ArrayList<String> getAllCategories()
	{
		return allCategories;
	}
	
	public String getCategory(int position)
	{
		return allCategories.get(position);
	}
	
	public void clear(FilterOption option)
	{
		switch(option)
		{
			case Number_of_Players:
				numberOfPlayers = null;
				break;
			case Playing_Time:
				minPlayingTime = null;
				maxPlayingTime = null;
				break;
			case Year_Published:
				minYear = null;
				maxYear = null;
				break;
			case Category:
				categories.clear();
				break;
		}
	}
	
	public void clearAllFilters()
	{
		for(FilterOption option : FilterOption.values())
		{
			clear(option);
		}
	}
	
	public boolean isActive()
	{
		for(FilterOption option : FilterOption.values())
		{
			if(isFilterActive(option))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getOriginalPositionFromFilteredPosition(GameCache cache, int position)
	{
		int currentPosition = -1;
		int i = 0;
		for(; i < cache.getSize(); i++)
		{
			Game game = cache.getList().get(i);
			if(include(game))
			{
				currentPosition++;
				if(currentPosition == position)
				{
					break;
				}
			}
		}
		return i;
	}
	
	public int getCount(GameCache cache)
	{
		int count = 0;
		for(int i = 0; i < cache.getSize(); i++)
		{
			if(include(cache.getList().get(i)))
			{
				count++;
			}
		}
		return count;
	}
	
	public boolean include(Game game)
	{
		if(isFilterActive(FilterOption.Number_of_Players))
		{
			int minNum = game.getInt(Property.minplayers);
			int maxNum = game.getInt(Property.maxplayers);
			if(numberOfPlayers < minNum || numberOfPlayers > maxNum)
			{
				return false;
			}
		}
		
		if(isFilterActive(FilterOption.Playing_Time))
		{
			int gameTime = game.getInt(Property.playingtime);
			if(gameTime < minPlayingTime || gameTime > maxPlayingTime)
			{
				return false;
			}
		}
		
		if(isFilterActive(FilterOption.Year_Published))
		{
			int year = game.getInt(Property.yearpublished);
			if(year < minYear || year > maxYear)
			{
				return false;
			}
		}
		
		if(isFilterActive(FilterOption.Category))
		{
			boolean matchesCategory = false;
			String categoryString = game.get(Property.boardgamecategory);
			if(categoryString == null)
			{
				return false;
			}
			for(String category : categories)
			{
				if(categoryString.contains(category))
				{
					matchesCategory = true;
					break;
				}
			}
			if(!matchesCategory)
			{
				return false;
			}
		}
		
		//matches all filters, return true;
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder s = new StringBuilder("<B>Active Filters</B>");
		if(isFilterActive(FilterOption.Number_of_Players))
		{
			s.append("<BR>Number of Players: ").append(numberOfPlayers);
		}
		if(isFilterActive(FilterOption.Playing_Time))
		{
			s.append(String.format("<BR>Playing Time: %d - %d minutes", minPlayingTime, maxPlayingTime));
		}
		if(isFilterActive(FilterOption.Year_Published))
		{
			s.append(String.format("<BR>Year Published: %d - %d", minYear, maxYear));
		}
		if(isFilterActive(FilterOption.Category))
		{
			s.append("<BR>Categories: ");
			for(int i = 0; i < categories.size(); i++)
			{
				if(i > 0)
				{
					s.append(", ");
				}
				s.append(categories.get(i));
			}
		}
		return s.toString();
	}
	
	//Parcel stuff
	
	@Override
	public int describeContents()
	{
		return 0;
	}
	@Override
	public void writeToParcel(Parcel p, int flags)
	{
		boolean filterNumberOfPlayers = isFilterActive(FilterOption.Number_of_Players);
		ParcelTools.writeBoolean(p, filterNumberOfPlayers);
		if(filterNumberOfPlayers)
		{
			p.writeInt(numberOfPlayers);
		}	
		
		boolean filterPlayingTime = isFilterActive(FilterOption.Playing_Time);
		ParcelTools.writeBoolean(p, filterPlayingTime);
		if(filterPlayingTime)
		{
			p.writeInt(minPlayingTime);
			p.writeInt(maxPlayingTime);
		}
		
		boolean filterYear = isFilterActive(FilterOption.Year_Published);
		ParcelTools.writeBoolean(p, filterYear);
		if(filterYear)
		{
			p.writeInt(minYear);
			p.writeInt(maxYear);
		}
		
		p.writeStringList(categories);
	}
	
	public void readFromParcel(Parcel p)
	{
		boolean filterNumberOfPlayers = ParcelTools.readBoolean(p);
		if(filterNumberOfPlayers)
		{
			numberOfPlayers = p.readInt();
		}
		
		boolean filterPlayingTime = ParcelTools.readBoolean(p);
		if(filterPlayingTime)
		{
			minPlayingTime = p.readInt();
			maxPlayingTime = p.readInt();
		}
		
		boolean  filterYear = ParcelTools.readBoolean(p);
		if(filterYear)
		{
			minYear = p.readInt();
			maxYear = p.readInt();
		}
		
		categories = p.createStringArrayList();
	}

	public Filter(Parcel p)
	{
		readFromParcel(p);
	}
	
	public static final Creator<Filter> CREATOR = new Creator<Filter>()
	{
		@Override
		public Filter createFromParcel(Parcel source)
		{
			return new Filter(source);
		}

		@Override
		public Filter[] newArray(int size)
		{
			return new Filter[size];
		}

	};
}