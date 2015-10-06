package com.thesonofthom.myboardgames;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import android.util.Log;
import com.thesonofthom.myboardgames.GameSorter.SortOption;
import com.thesonofthom.myboardgames.GameSorter.SortOrder;
import com.thesonofthom.myboardgames.adapters.GameAdapter;

public class Game implements Comparable<Game>
{
	private static final String TAG = "Game";
	
	public static final int UNKNOWN_OBJECT_ID = -1;
	
	public static final int STATUS_NONE = 0; //not loaded at all
	public static final int STATUS_MINIMAL = 1; //at least has object id
	public static final int STATUS_LIST_INFO = 2; //has enough info to display as a list result
	public static final int STATUS_FULL = 3; //has all info
	public static final int STATUS_FULL_WITH_COMMENTS = 4; //has all info + comments
	
	public static enum Property
	{
		yearpublished("Year Published"),
		minplayers("Minimum Players"),
		maxplayers("Maximum Players"),
		playingtime("Playing Time (minutes)"),
		age("Minimum Age"),
		name("Name"),
		description("Description"),
		thumbnail("Thumbnail"),
		image("Image"),
		boardgamepublisher("Publishers", true), //list
		boardgamecategory("Categories", true), //list
		boardgamehonor("Honors", true), //list
		boardgameexpansion("Expansions", true), //list (don't include "fan expansion")
		baseboardgame("Expansion for Base Game", true),
		//boardgamesubdomain("Subdomain", true),
		boardgamedesigner("Designers", true),
		boardgameartist("Artists", true),
		boardgamemechanic("Mechanics", true),
		comment("Comment"),
		owned("Owned"),
		loanContact_lookup_key("Loan Contact Lookup Key");
		private String publicName;
		private boolean allowMultiple;

		Property(String name)
		{
			this(name, false);
		}
		
		Property(String name, boolean allowMultiple)
		{
			publicName = name;
			this.allowMultiple = allowMultiple;

		}
		
		public boolean allowMultiple()
		{
			return allowMultiple;
		}
		
		public String getPublicName()
		{
			return publicName;
		}
		
		public static Property getProperty(String name)
		{
			try
			{
				return (Property)Enum.valueOf(Property.class, name);
			}
			catch(Exception e)
			{
				return null;
			}
		}
	}
	
	private int status = STATUS_NONE;

	private int objectid;	
	
	private static boolean dontMarkAsDirtyLock;
	
	public static void enableDontMarkAsDirtyLock()
	{
		
		dontMarkAsDirtyLock = true;
	}
	
	public static void disabledontMarkAsDirtyLock()
	{
		dontMarkAsDirtyLock = false;
	}
	
	public Game(int objectid)
	{
		this.objectid = objectid;
		initializeCaches();
		GamePool.getInstance().add(this);
	}
	
	private void initializeCaches()
	{
		expansionCache = new GameCache(objectid + " Expansion Cache");
		expansionCache.setSortOptions(SortOption.YEAR, SortOrder.ASCENDING);
		baseGameCache = new GameCache(objectid + " Base Game Cache");
		baseGameCache.setSortOptions(SortOption.YEAR, SortOrder.ASCENDING);
	}

	public int getObjectId()
	{
		return objectid;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String getFileName()
	{
		return objectid + ".xml";
	}
	
	public static Game getGameFromFileName(String fileName)
	{
		String objectIdString = fileName.substring(0, fileName.indexOf(".xml"));
		int objectid = Integer.parseInt(objectIdString);
		return GamePool.getInstance().get(objectid);
	}
	
	public boolean isOwned()
	{
		String value = get(Property.owned);
		return value != null;
	}
	
	public void loanToContact(long id, String lookupKey)
	{
		add(Property.loanContact_lookup_key, lookupKey);
		GamePool.getInstance().loanGame(this);
		GameCache cache = GamePool.getInstance().getLoanGameCache(lookupKey);
		cache.cache(this);
		//GamePool.getInstance().getLoanGameFilter(lookupKey).recalculateInitialValues(cache);
	}
	
	public boolean isOnLoan()
	{
		String value = get(Property.loanContact_lookup_key);
		return value != null;
	}
	
	public void returnLoanedGame()
	{
		String lookupKey = get(Property.loanContact_lookup_key);
		if(lookupKey != null)
		{
			GameCache cache = GamePool.getInstance().getLoanGameCache(lookupKey);
			cache.remove(this);
			//GamePool.getInstance().getLoanGameFilter(lookupKey).recalculateInitialValues(cache);
			properties.remove(Property.loanContact_lookup_key);
			
			GamePool.getInstance().returnLoanedGame(this);
		}
	}
	
	public void setOwned(boolean owned)
	{
		if(owned)
		{
			add(Property.owned, "true");
			GamePool.getInstance().addLocalGame(this);
		}
		else
		{
			String lookupKey = get(Property.loanContact_lookup_key);
			properties.remove(Property.owned);
			properties.remove(Property.loanContact_lookup_key);
			GamePool.getInstance().removeLocalGame(this);
			if(lookupKey != null)
			{
				GamePool.getInstance().getLoanGameCache(lookupKey).remove(this);
			}
		}
	}
	
	private boolean dirty;
	public boolean isDirty()
	{
		return dirty;
	}
	
	public void clearDirty()
	{
		if(dirty)
		{
			Log.i(TAG, toString() + ": Clearing dirty flag...");
			dirty = false;
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("%d %s (status: %d, properties: %d)", objectid, get(Property.name), status, properties.size());
	}
	
	
	
	public boolean contains(Property p, int objectid)
	{
		LinkedList<PropertyData> data = getList(p);
		if(data != null)
		{
			Iterator<PropertyData> iterator = data.iterator();
			while(iterator.hasNext())
			{
				PropertyData pData = iterator.next();
				if(pData.objectid == objectid)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public class PropertyData
	{
		public Property name;
		public int objectid;
		public String value;
			
		PropertyData(Property n, int o, String v)
		{
			name = n;
			objectid = o;
			value = v;
		}
		

		public boolean equals(Object o)
		{
			if(o instanceof PropertyData)
			{
				PropertyData other = (PropertyData)o;
				return name == other.name && value != null && value.equals(other.value);
			}
			return super.equals(o);
		}
	}
	
	private HashMap<Property, LinkedList<PropertyData>> properties = new HashMap<Property, LinkedList<PropertyData>>();
	
	public HashMap<Property, LinkedList<PropertyData>> getProperties()
	{
		return properties;
	}

	
	public void add(Property key, int objectid, String value)
	{
		PropertyData property = new PropertyData(key, objectid, value);
		add(key, property);
	}
	
	public void add(Property key, String value)
	{
		add(key, UNKNOWN_OBJECT_ID, value);
	}
	
	private void add(Property key, PropertyData property)
	{
		LinkedList<PropertyData> list = getList(key);
		if(list == null)
		{
			list = new LinkedList<PropertyData>();
			properties.put(key, list);
		}
		
		if(!key.allowMultiple && !list.isEmpty())
		{
			return; //already have this property
		}
		
		if(property.objectid != UNKNOWN_OBJECT_ID)
		{
			for(PropertyData listProperty : list)
			{
				if(listProperty.objectid == property.objectid)
				{
					//we already have this property. don't add it again
					return;
				}
			}
		}
		
		if(!list.contains(property))
		{
			list.add(property);
		}

		int pobjectid = property.objectid;
		String propertyValue = property.value;
		if(key == Property.boardgameexpansion && pobjectid != UNKNOWN_OBJECT_ID)
		{
			Game expansion = GamePool.getInstance().get(pobjectid);
			if(expansion == null)
			{
				//if the expansion isn't in our cache yet, create an object for it
				//Log.i(TAG, "BGG CACHE: " + cache);
				//Log.i(TAG, "Expansion " + pobjectid + " " + propertyValue + " doesn't exist yet.");
				expansion = new Game(pobjectid);
				expansion.add(Property.name, propertyValue);
			}
			getExpansionCache().cache(expansion);
			expansion.getBaseGameCache().cache(this);

		}
		else if(key == Property.baseboardgame && pobjectid != UNKNOWN_OBJECT_ID)
		{
			Game baseGame = GamePool.getInstance().get(pobjectid);
			if(baseGame == null)
			{
				//if the game isn't in our cache yet, create an object for it
				//Log.i(TAG, "BGG CACHE: " + cache);
				//Log.i(TAG, "Base game " + pobjectid + " " + propertyValue + " doesn't exist yet.");
				baseGame = new Game(pobjectid);
				baseGame.add(Property.name, propertyValue);
			}
			getBaseGameCache().cache(baseGame);
			baseGame.getExpansionCache().cache(this);
		}
		if(!dirty && !dontMarkAsDirtyLock)
		{
			Log.i(TAG, "   Marking game as dirty because property " + property.name + ": " + propertyValue + " was added");
			dirty = true;
		}
	}
	
	public LinkedList<PropertyData> getList(Property key)
	{
		return properties.get(key);
	}
	
	public int getInt(Property key)
	{
		try
		{
			return Integer.parseInt(get(key));
		}
		catch(Exception e)
		{
			return 0;
		}
	}
	
	public long getLong(Property key)
	{
		try
		{
			return Long.parseLong(get(key));
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	public String get(Property key)
	{
		//compute name and return
		LinkedList<PropertyData> list = getList(key);
		if(list != null && !list.isEmpty())
		{
			if(key.allowMultiple)
			{
				String value = "";
				Iterator<PropertyData> iterator = list.iterator();
				while (iterator.hasNext())
				{
					if (!value.isEmpty())
					{
						value += ", ";
					}
					value += iterator.next().value;
				}
				return value;
			}
			else
			{
				return list.get(0).value;
			}
		}
		return null;
	}
	
	private GameCache expansionCache;
	private GameCache baseGameCache;
	
	public GameCache getExpansionCache()
	{
		return expansionCache;
	}
	
	public GameCache getBaseGameCache()
	{
		return baseGameCache;
	}
	
	public String toString(Property property)
	{
		return String.format("%s: %s", property.getPublicName(), get(property));
	}
	
	public String yearToString()
	{
		String year = get(Property.yearpublished);
		if(year != null)
		{
			try
			{
				int yearInt = Integer.parseInt(year);
				if(yearInt > 0)
				{
					return year; 
				}
			}
			catch(Exception e)
			{
				return null;
			}
		}
		return null;
		
	}
	
	public String playersToString()
	{
		String players = null;
		try
		{
			int minPlayers = Integer.parseInt(get(Property.minplayers));
			int maxPlayers = Integer.parseInt(get(Property.maxplayers));
			
			if(minPlayers == 0|| maxPlayers == 0)
			{
				return null;
			}
			if (minPlayers < maxPlayers)
			{
				players = String.format("%d - %d players", minPlayers, maxPlayers);
			}
			else if (minPlayers == maxPlayers)
			{
				players = Integer.toString(minPlayers);
				if(minPlayers == 1)
				{
					players += " player";
				}
				else
				{
					players += " players";
				}
			}

			return players;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public String playingTimeToString()
	{
		try
		{
			int playingTime = Integer.parseInt(get(Property.playingtime));
			if(playingTime > 0)
			{
				return String.format("%d minutes", playingTime);
			}
		}
		catch(Exception e)
		{
			
		}
		return null;
	}
	
	public String getDescription()
	{
		return getDescription(true);
	}
	
	public String getDescription(boolean fullDescription)
	{
		StringBuilder s = new StringBuilder();
		String year = yearToString();
		if(year != null)
		{
			s.append(year);
		}
		
		String players = playersToString();
		if(players != null)
		{
			if(s.length() != 0)
			{
				s.append(", ");
			}
			s.append(players);
		}
		
		String duration = playingTimeToString();
		if(duration != null)
		{
			if(s.length() != 0)
			{
				s.append(", ");
			}
			s.append(duration);
		}
		
		if (fullDescription)
		{
			String category = get(Property.boardgamecategory);
			if (category != null)
			{
				s.append("\n").append(category);
			}

			String mechanic = get(Property.boardgamemechanic);
			if (mechanic != null)
			{
				s.append("\n").append(mechanic);
			}
		}
		return s.toString();
	}
	
	private static SortOption sortOption = SortOption.NAME;
	private static SortOrder sortOrder = SortOrder.ASCENDING;
	
	public static void setSortOptions(SortOption option, SortOrder order)
	{
		sortOption = option;
		sortOrder = order;
	}
	
	private static SortOption savedSortOption = SortOption.NAME;
	private static SortOrder savedSortOrder = SortOrder.ASCENDING;
	public static void saveSortSettings()
	{
		savedSortOption = sortOption;
		savedSortOrder = sortOrder;
	}
	
	public static void restoreSortSettings()
	{
		sortOption = savedSortOption;
		sortOrder = savedSortOrder;
	}
	
	
	public int compareTo(Game other, SortOption option, SortOrder order)
	{
		//Log.i(TAG, "Comparing " + this + " and " + other + " by " + option);
		int result;
		if(option != null)
		{
			String thisProp = get(option.property);
			String otherProp = other.get(option.property);

			if(thisProp != null && otherProp != null)
			{
				if(option.compareDecimal)
				{
					Integer thisPropInt = Integer.parseInt(thisProp);
					Integer otherPropInt = Integer.parseInt(otherProp);
					result = thisPropInt.compareTo(otherPropInt);
				}
				else
				{
					if(option == SortOption.NAME)
					{
						if(thisProp.startsWith("The "))
						{
							thisProp = thisProp.substring(4); //remove "The" from the comparison
						}
						if(otherProp.startsWith("The "))
						{
							otherProp = otherProp.substring(4);
						}
					}
					
					result = thisProp.compareTo(otherProp);

				}
				
				if(result == 0)
				{
					if( option != SortOption.NAME)
					{
					//if the result is equal, then sort them by name
					result = compareTo(other, SortOption.NAME, order);
					}
					else //if we were sorting by name, and both names are somehow exactly the same, then sort by object id to get a consistent order
					{
						result = compareTo(other, null, SortOrder.ASCENDING);
					}
				}
				
			}
			else if(thisProp != null)
			{
				result = -1;
			}
			else if(otherProp != null)
			{
				result = 1;
			}
			else
			{
				result = 0;
			}
		}
		else //if null, sort by the object ID
		{
			result = objectid - other.objectid;
		}
		
		if(order == SortOrder.DESCENDING) //reverse the order of the result
		{
			result *= -1;
		}
		return result;
	}
	
	public int compareTo(Game other) 
	{	
		return compareTo(other, sortOption, sortOrder);
	}
	
	private LinkedList<GameAdapter> adapters = new LinkedList<GameAdapter>();
	
	public void addAdapter(GameAdapter gameInfoDataAdapter)
	{
		if(!adapters.contains(gameInfoDataAdapter))
		{
			adapters.add(gameInfoDataAdapter);
		}
	}
	
	public void updateAllAdapters()
	{
		updateAllAdapters(this);
		for(Game baseGame : baseGameCache.getList())
		{
			updateAllAdapters(baseGame); //to prevent infinite loops
		}
		
		for(Game expansionGame : expansionCache.getList())
		{
			updateAllAdapters(expansionGame);
		}
	}
	
	private void updateAllAdapters(Game gameToUpdate)
	{
		for(GameAdapter adapter : gameToUpdate.adapters)
		{
			if(adapter != null)
			{
				adapter.refresh();
			}
		}
	}
//
//	public class Comment
//	{
//		public final String username;
//		public final String rating;
//		public final String comment;
//		
//		private Comment(String u, String r, String c)
//		{
//			username = u;
//			rating = r;
//			comment = c;
//		}
//	}
//	
//
//	private ArrayList<Comment> comments = new ArrayList<Comment>();
//	
//	public void addComment(String u, String r, String c)
//	{
//		Comment comment = new Comment(u, r, c);
//		comments.add(comment);
//	}
//	
//	
//	public ArrayList<Comment> getComments()
//	{
//		return comments;
//	}
//	
	//Parcel stuff
	
//	public static enum ParcelMode
//	{
//		MODE_LIST_MINIMAL,
//		MODE_LIST,
//		MODE_FULL;
//	}
//
//	private ParcelMode parcelMode = ParcelMode.MODE_LIST;
//	
//	public void setParcelMode(ParcelMode mode)
//	{
//		//allow overriding of the parcel mode if the ordinal is higher
//		if(parcelMode == null || parcelMode.ordinal() < mode.ordinal())
//		{
//			Log.i(TAG, "Setting parcel mode for " + toString() + " to " + mode);
//			parcelMode = mode;
//		}
//	}
//	
//	public void clearParcelMode()
//	{
//		parcelMode = null;
//	}
//	
//	public Game(Parcel in)
//	{
//		readFromParcel(in);
//	}
//
//	@Override
//	public void writeToParcel(Parcel p, int flags)
//	{
//		Log.i(TAG, "Writing " + toString() + " to parcel, mode: " + parcelMode);
//		ParcelTools.writeEnum(p, parcelMode);
//		if(parcelMode == null)
//		{
//			return;
//		}
//		switch(parcelMode)
//		{
//		case MODE_LIST:
//		case MODE_LIST_MINIMAL:
//			writeListInfoToParcel(p, flags);
//			break;
//		case MODE_FULL:
//			writeAllToParcel(p, flags);
//			break;
//		default:
//			throw new RuntimeException("ERROR! " + toString() + ": setParcelMode() must be called before writing to parcel!");
//		}
//		clearParcelMode();
//	}
//	
//	public void readFromParcel(Parcel p)
//	{
//		initializeCaches();
//		parcelMode = ParcelTools.readEnum(p, ParcelMode.class);
//		Log.i(TAG, "Reading from parcel, mode: " + parcelMode);
//		if(parcelMode == null)
//		{
//			return;
//		}
//		
//		enableDontMarkAsDirtyLock();
//		switch(parcelMode)
//		{
//		case MODE_LIST:
//		case MODE_LIST_MINIMAL:
//			readListInfoFromParcel(p);
//			break;
//		case MODE_FULL:
//			readAllFromParcel(p);
//			break;
//		default:
//			throw new RuntimeException("ERROR! " + toString() + ": setParcelMode() must be called before reading from parcel!");
//		}
//		GamePool.getInstance().add(this);
//		disabledontMarkAsDirtyLock();
//		clearParcelMode();
//	}
//	
//	private void writeListInfoToParcel(Parcel p, int flags)
//	{
//		p.writeInt(objectid);
//		p.writeInt(STATUS_LIST_INFO);
//		p.writeString(get(Property.name));
//		p.writeString(get(Property.yearpublished));
//		p.writeString(get(Property.minplayers));
//		p.writeString(get(Property.maxplayers));
//		p.writeString(get(Property.playingtime));
//		
//		List<PropertyData> categoryList = getList(Property.boardgamecategory);
//		boolean categoriesAvailable = categoryList != null && !categoryList.isEmpty();
//		
//		List<PropertyData> mechanicsList = getList(Property.boardgamemechanic);
//		boolean mechanicsAvailable = mechanicsList != null && !mechanicsList.isEmpty();
//		if(parcelMode == ParcelMode.MODE_LIST && categoriesAvailable && mechanicsAvailable)
//		{
//
//			p.writeInt(categoryList.size());
//			for(PropertyData data : categoryList)
//			{
//				p.writeString(data.value);
//			};
//			
//			p.writeInt(mechanicsList.size());
//			for(PropertyData data : mechanicsList)
//			{
//				p.writeString(data.value);
//			};
//		}
//		else
//		{
//			p.writeInt(0);
//			p.writeInt(0);
//		}
//		
//		p.writeString(get(Property.thumbnail));
//		ParcelTools.writeBoolean(p, isOwned());
//		ParcelTools.writeBoolean(p, isOnLoan());
//		if(isOnLoan())
//		{
//			p.writeLong(getLong(Property.loanContact_id));
//			p.writeString(get(Property.loanContact_lookup_key));
//		}
//	}
//	
//	private void readListInfoFromParcel(Parcel p)
//	{
//		objectid = p.readInt();
//		status = p.readInt();
//		add(Property.name, p.readString());
//		add(Property.yearpublished, p.readString());
//		add(Property.minplayers, p.readString());
//		add(Property.maxplayers, p.readString());
//		add(Property.playingtime, p.readString());
//		
//		int numberOfCategories = p.readInt();
//		for(int i = 0; i < numberOfCategories; i++)
//		{
//			add(Property.boardgamecategory, p.readString());
//		}
//		
//		int numberOfMechanics = p.readInt();
//		for(int i = 0; i < numberOfMechanics; i++)
//		{
//			add(Property.boardgamemechanic, p.readString());
//		}
//
//		add(Property.thumbnail, p.readString());
//		setOwned(ParcelTools.readBoolean(p));
//		boolean isOnLoan = ParcelTools.readBoolean(p);
//		if(isOnLoan)
//		{
//			loanToContact(p.readLong(), p.readString());
//		}
//	}
//
//	private void writeAllToParcel(Parcel p, int flags)
//	{
//		p.writeInt(objectid);
//		p.writeInt(status);
//		ParcelTools.writeBoolean(p, dirty);
//		int numberOfProperties = 0;
//		for (Map.Entry<Property, LinkedList<PropertyData>> entry : properties.entrySet()) 
//		{
//			numberOfProperties += entry.getValue().size();
//		}
//		
//		p.writeInt(numberOfProperties);
//		
//		for (Map.Entry<Property, LinkedList<PropertyData>> entry : properties.entrySet()) 
//		{
//			//flatten each property into a separate line
//			Iterator<PropertyData> iterator = entry.getValue().iterator();
//			while(iterator.hasNext())
//			{
//				PropertyData property = iterator.next();
//				p.writeString(entry.getKey().name());
//				p.writeInt(property.objectid);
//				p.writeString(property.value);
//			}
//			
////			p.writeInt(comments.size());
////			for(Comment comment : comments)
////			{
////				p.writeString(comment.username);
////				p.writeString(comment.rating);
////				p.writeString(comment.comment);
////			}
//		}
//		
//		ParcelMode currentMode = parcelMode;
//		setParcelMode(ParcelMode.MODE_LIST_MINIMAL);
//		p.writeParcelable(baseGameCache, flags);
//		p.writeParcelable(expansionCache, flags);
//		setParcelMode(currentMode);
//		
//	}
//	
//	private void readAllFromParcel(Parcel p)
//	{
//		objectid = p.readInt();
//		Log.i(TAG, "readFromParcel: " + objectid);
//		status = p.readInt();
//		
//		dirty = ParcelTools.readBoolean(p);
//		
//		int numberOfProperties = p.readInt();
//		
//		for(int i = 0; i < numberOfProperties; i++)
//		{
//			Property propertyEnum = Property.getProperty(p.readString());
//			int propertyId = p.readInt();
//			String data = p.readString();
//			if(propertyEnum != null)
//			{
//				add(propertyEnum, propertyId, data);
//			}
//		}
//		
//		ParcelMode currentMode = parcelMode;
//		setParcelMode(ParcelMode.MODE_LIST_MINIMAL);
//		baseGameCache = p.readParcelable(GameCache.class.getClassLoader());
//		expansionCache = p.readParcelable(GameCache.class.getClassLoader());
//		setParcelMode(currentMode);
//		
//		Log.i(TAG, baseGameCache.toString());
//		Log.i(TAG, expansionCache.toString());
//		
//		
////		int numberOfComments = p.readInt();
////		for(int i = 0; i < numberOfComments; i++)
////		{
////			String username = p.readString();
////			String rating = p.readString();
////			String commentText = p.readString();
////			addComment(username, rating, commentText);
////		}
//		
//	}
//	
//
//	
//	public static final Creator<Game> CREATOR = new Creator<Game>()
//	{
//		@Override
//		public Game createFromParcel(Parcel source)
//		{
//			return new Game(source);
//		}
//
//		@Override
//		public Game[] newArray(int size)
//		{
//			return new Game[size];
//		}
//
//	};
//	
//	
//	@Override
//	public int describeContents()
//	{
//		return 0;
//	}
//	
}
