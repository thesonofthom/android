package com.thesonofthom.myboardgames.adapters;

import java.util.List;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Settings;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.activities.MainActivity;
import com.thesonofthom.myboardgames.tools.Filter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  Adapter for displaying a list of Game objects
 *  
 * @author Kevin Thomson
 *
 */
public class GameListAdapter extends BaseAdapter implements GameAdapter
{
	
	private static final String TAG = "GameListAdapter";
	private MainActivity context;
    private LayoutInflater inflater;
    private GameCache cache;
    
    private Settings settings;
    
    private List<Game> list;
    private boolean fullDescription;
    
    private Filter filter;
    
    //private View list_row_game_result;
    
    public GameListAdapter(MainActivity a, GameCache cache, Filter f)
    {
    	this(a, cache, f, true);
    }
    
	public GameListAdapter(MainActivity context, GameCache cache, Filter f, boolean fullDescription)
	{
		this.context = context;
		this.cache = cache;
		list = cache.getList();
		filter = f;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        settings = new Settings(context);
        this.fullDescription = fullDescription;
       // list_row_game_result = inflater.inflate(R.layout.list_row_game_result, null);
	}
		
	@Override
	public int getCount() 
	{
		if(filter == null || !filter.isActive())
		{
			//Log.i(TAG, "Count: " + cache.getPosition());
			return cache.getPosition();
		}
		else
		{
			return filter.getCount(cache);
		}
	}
	
	public boolean isCompleteList()
	{
		Log.i(TAG, "isCompleteList: getCount(): " + getCount() + " getFullCount(): " + getFullCount());
		return getCount() == getFullCount();
	}
	
	public int getFullCount()
	{
		return cache.getList().size();
	}

	@Override
	public Object getItem(int position) 
	{
		if(filter != null && filter.isActive())
		{
			position = filter.getOriginalPositionFromFilteredPosition(cache, position);
		}
		
		if(position < list.size())
		{
			return list.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) 
	{
		if(filter != null && filter.isActive())
		{
			position = filter.getOriginalPositionFromFilteredPosition(cache, position);
		}
		
		if(position >= 0 && position < list.size())
		{
			return list.get(position).getObjectId();
		}
		return Game.UNKNOWN_OBJECT_ID;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if(convertView == null || convertView.getId() != R.id.list_row_game_result_id)
		{
			convertView = inflater.inflate(R.layout.list_row_game_result, null);
		}
		 
		 TextView gameName = (TextView)convertView.findViewById(R.id.game_name);
		 TextView gameDescription = (TextView)convertView.findViewById(R.id.game_description);

		 ImageView thumb_image = (ImageView)convertView.findViewById(R.id.list_image); 
		 
		 View ownershipIndicator = convertView.findViewById(R.id.game_owned_indicator);
	        
		 Game  game = (Game)getItem(position);
		 if(game != null)
		 {
			 game.addAdapter(this);
			 String name = game.get(Property.name);
			 if(name != null && gameName != null)
			 {
				 gameName.setText(game.get(Property.name));
			 }

			 if(gameDescription != null)
			 {
				 gameDescription.setText(game.getDescription(fullDescription));
			 }
			 
	         if(settings.detailedSearchResultsEnabled())
	         {
	        	 if( thumb_image != null)
	        	 {
		        	 thumb_image.setVisibility(View.VISIBLE);
		        	 String thumbnailUrl = game.get(Property.thumbnail);
		        	 if(thumbnailUrl != null)
		        	 {
		        		 context.getImageLoader().DisplayImage(game.get(Property.thumbnail), thumb_image);
		        	 }
		        	 else
		        	 {
		        		 thumb_image.setImageDrawable(context.getResources().getDrawable(R.drawable.no_image));
		        	 }
	        	 }
	         }
	         else
	         {
	        	 thumb_image.setVisibility(View.GONE);
	         }
	         
	         if(game.isOwned())
	         {
	        	 if(game.isOnLoan())
	        	 {
	        		 ownershipIndicator.setBackgroundResource(R.color.yellow);
	        	 }
	        	 else
	        	 {
	        		 ownershipIndicator.setBackgroundResource(R.color.green);
	        	 }
	         }
	         else
	         {
	        	 ownershipIndicator.setBackgroundResource(android.R.color.transparent);
	         }
		 }
		 return convertView;
	}

	@Override
	public void refresh()
	{
		notifyDataSetChanged();
	}
}
