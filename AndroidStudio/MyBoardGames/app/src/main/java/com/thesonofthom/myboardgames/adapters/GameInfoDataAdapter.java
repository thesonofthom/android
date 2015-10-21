package com.thesonofthom.myboardgames.adapters;



import java.util.ArrayList;
import java.util.List;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.Game.PropertyData;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.asynctask.RetrieveAdditionalResultsTask;
import com.thesonofthom.myboardgames.asynctask.DialogAsyncTask;
import com.thesonofthom.myboardgames.asynctask.SaveGamesTask;
import com.thesonofthom.myboardgames.fragments.GameInfoFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Adapter for displaying detailed information for a single game
 * 
 * @author Kevin Thomson
 *
 */
public class GameInfoDataAdapter extends BaseExpandableListAdapter implements GameAdapter
{
	public static final String TAG = "GameInfoDataAdapter";
	public static enum Section
	{
		Information(null, true),
		BaseGame(Property.baseboardgame, true),
		Description(Property.description),
		Expansions(Property.boardgameexpansion),
		Categories(Property.boardgamecategory),
		Mechanics(Property.boardgamemechanic),
		Publishers(Property.boardgamepublisher),
		Designers(Property.boardgamedesigner),
		Artists(Property.boardgameartist),
		Honors(Property.boardgamehonor);
		
		
		Section(Property property)
		{
			this(property, false);
		}
		
		Section(Property property, boolean startExpanded)
		{
			this.property = property;
			this.startExpanded = startExpanded;
		}
		
		public String toString()
		{
			if(property != null)
			{
				return property.getPublicName();
			}
			else
			{
				return name();
			}
		}
		
		
		Property property;
		public boolean startExpanded;
	}
	
	private static Property[] informationArray = {Property.yearpublished, Property.minplayers, Property.maxplayers, Property.playingtime, Property.age};
	
	private ArrayList<Property> validInformationArray;
	
	private ArrayList<Section> groups;
	
	private Game game;
	private MainActivity context;
	
	private GameListAdapter expansionsAdapter;
	private GameListAdapter baseGamesAdapter;
	
	private Button expansionLoadMoreButton;
	private Button baseGameLoadMoreButton;
	
	private LayoutInflater inflater=null;

	public GameInfoDataAdapter(final GameInfoFragment fragment, final DialogAsyncTask<?> task, final Game game)
	{
		this.context = fragment.getMainActivity();
		this.game = game;
		game.getExpansionCache().sort(); //force results to be in correct order
		game.getBaseGameCache().sort(); 
		game.addAdapter(this);
		groups = new ArrayList<Section>();
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		validInformationArray = new ArrayList<Property>(informationArray.length);
		
		expansionsAdapter = new GameListAdapter(context, game.getExpansionCache(), null, false);
		expansionLoadMoreButton = new Button(context);
		expansionLoadMoreButton.setText("Load More");
		expansionLoadMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				RetrieveAdditionalResultsTask retriveMoreExpansionsTask = new RetrieveAdditionalResultsTask(context, game.getExpansionCache(), task);
				retriveMoreExpansionsTask.execute();
				if(game.isOwned())
				{
					SaveGamesTask saveTask = new SaveGamesTask(fragment);
					saveTask.showToast(false); //silently save off additional info for game
					saveTask.execute(game); //this will only save new information about the game
				}
			}
		});

		baseGamesAdapter = new GameListAdapter(context, game.getBaseGameCache(), null, false);
		baseGameLoadMoreButton = new Button(context);
		baseGameLoadMoreButton.setText("Load More");
		baseGameLoadMoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				RetrieveAdditionalResultsTask retriveMoreBaseGamesTask = new RetrieveAdditionalResultsTask(context, game.getBaseGameCache(), task);
				retriveMoreBaseGamesTask.execute();
				//now that we've loaded everything, save off anything we haven't already saved about the game,
				//since we have loaded more information about it
				if(game.isOwned())
				{
					SaveGamesTask saveTask = new SaveGamesTask(fragment);
					saveTask.showToast(false); //silently save off additional info for game
					saveTask.execute(game); //this will only save new information about the game
				}
			}
		});
		
		for(Section data : Section.values())
		{
			boolean include = false;
			if(data == Section.Information)
			{
				for(Property info : informationArray)
				{
					if(game.getList(info) != null)
					{
						include = true;
						validInformationArray.add(info);
					}
				}
			}
			else
			{
				if(game.getList(data.property) != null)
				{
					include = true; 
				}
			}
			if(include)
			{
				groups.add(data);
			}
		}
	}
	
	public void refresh()
	{
		notifyDataSetChanged();
	}
	
	public Section groupPositionToSection(int groupPosition)
	{
		return groups.get(groupPosition);
	}
	
	
	public int getExpansionsLoadMoreButtonPosition()
	{
		return expansionsAdapter.getCount();
	}
	public int getBaseGamesLoadMoreButtonPosition()
	{
		return baseGamesAdapter.getCount();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition)
	{
		Section group = groups.get(groupPosition);
		switch(group)
		{
			case Information:
				return game.get(validInformationArray.get(childPosition));
			case Expansions:
			{
				if(childPosition == getExpansionsLoadMoreButtonPosition())
				{
					return expansionLoadMoreButton;
				}
				return expansionsAdapter.getItem(childPosition);
			}
			case BaseGame:
			{
				if(childPosition == getBaseGamesLoadMoreButtonPosition())
				{
					return baseGameLoadMoreButton;
				}
				return baseGamesAdapter.getItem(childPosition);
			}
			default:
				return game.getList(group.property).get(childPosition).value;

		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition)
	{
		Section group = groups.get(groupPosition);
		switch(group)
		{
			case Expansions:
				return expansionsAdapter.getItemId(childPosition);
			case BaseGame:
				return baseGamesAdapter.getItemId(childPosition);
			default:
				return Game.UNKNOWN_OBJECT_ID;
		}
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		Section group = groups.get(groupPosition);
		//Log.i(TAG, "Getting child " + childPosition + " for group" + group);
		
		if(group == Section.Expansions)
		{
			if(childPosition == getExpansionsLoadMoreButtonPosition())
			{
				return expansionLoadMoreButton;
			}
			return expansionsAdapter.getView(childPosition, convertView, null);
		}
		else if(group == Section.BaseGame)
		{
			if(childPosition == getBaseGamesLoadMoreButtonPosition())
			{
				return baseGameLoadMoreButton;
			}
			return baseGamesAdapter.getView(childPosition, convertView, null);
		}
		

		if(convertView == null || convertView.getId() != R.id.list_row_game_data_id)
		{
			convertView = inflater.inflate(R.layout.list_row_game_data, null);
		}
	
		String childText = (String) getChild(groupPosition, childPosition);

		if (group == Section.Information)
		{
			String header = "<b>" + validInformationArray.get(childPosition).getPublicName()+ ": </b>";
			childText = header + childText;
		}
		TextView txtListChild = (TextView) convertView.findViewById(R.id.game_data_list_item);
		if(txtListChild != null)
		{
			txtListChild.setText(Html.fromHtml(childText));
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition)
	{
		Section group = groups.get(groupPosition);
		switch(group)
		{
			case Information:
				return validInformationArray.size();//informationArray.length;
			case Expansions:
			{
				int count = expansionsAdapter.getCount();
				if(!expansionsAdapter.isCompleteList())
				{
					count++; //add loadMoreButton
				}
				return count;
			}
			case BaseGame:
			{
				int count = baseGamesAdapter.getCount();
				if(!baseGamesAdapter.isCompleteList())
				{
					count++; //add loadMoreButton
				}
				return count;
			}
			default:
			{
				List<PropertyData> list = game.getList(group.property);
				if(list != null)
				{
					return list.size();
				}
				return 0;
			}
		}
	}
	
	public int getFullChildrenCount(int groupPosition)
	{
		Section group = groups.get(groupPosition);
		switch(group)
		{
			case Expansions:
				return expansionsAdapter.getFullCount();
			case BaseGame:
				return baseGamesAdapter.getFullCount();

			default:
				return getChildrenCount(groupPosition);
					
		}
	}

	@Override
	public Object getGroup(int groupPosition)
	{
		Section group = groups.get(groupPosition);
		return group;
	}

	@Override
	public int getGroupCount()
	{
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition)
	{
		return groups.get(groupPosition).ordinal();
	}
	
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{
		Section group = groups.get(groupPosition);
		if (convertView == null || convertView.getId() != R.id.list_group_game_data_id)
		{
			convertView = inflater.inflate(R.layout.list_group_game_data, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.game_info_section);
		title.setTypeface(null, Typeface.BOLD);
		String name = group.toString();
		switch(group)
		{
			case Expansions:
			case Categories:
			case Mechanics:
			case Publishers:
			case Designers:
			case Artists:
			case Honors:
			{
				int count = getFullChildrenCount(groupPosition);
				if(count > 0)
				{
					name += " (" + getFullChildrenCount(groupPosition) + ")";
				}
				break;
			}

			default:
				break;
		}
		title.setText(name);

		return convertView;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		Section group = groups.get(groupPosition);
		switch(group)
		{
			case Expansions:
			case BaseGame:
				return true;
			default:
				return false;
		}
		
	}
}
