package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.adapters.GameListAdapter;
import com.thesonofthom.myboardgames.asynctask.RetrieveAdditionalResultsTask;
import com.thesonofthom.myboardgames.asynctask.RetrieveSearchResultsTask;
import com.thesonofthom.myboardgames.asynctask.TaskResult;
import com.thesonofthom.myboardgames.tools.FragmentTools;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Fragment to display the list of search results
 * @author Kevin Thomson
 *
 */
public class GameSearchFragment extends BaseFragment  implements SearchView.OnQueryTextListener 
{
	private static final String TAG = "GameSearchFragment";
	public GameSearchFragment()
	{
		super(TAG, Navigation.GameSearch);
	}

	
	private MenuItem searchItem;
	private ListView list;
	private TextView searchQuery;
	public Button moreResultsButton;
	
	private RetrieveSearchResultsTask searchResultsTask;
	
	private GameCache cache;
	
	public TextView getSearchQuery()
	{
		return searchQuery;
	}
	
	private String query = null;
	private View mainView;
	
	public GameListAdapter getAdapter()
	{
		if(searchResultsTask != null)
		{
			return searchResultsTask.getAdapter();
		}
		return null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		searchResultsTask = null;
		cache = GamePool.getInstance().getSearchCache();
		query = GamePool.getInstance().getQuery();
		if(savedInstanceState != null)
		{
			restoreInstanceState(savedInstanceState);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		if (mainView == null)
		{
			Log.i(TAG, "Inflating view...");
			mainView = inflater.inflate(R.layout.activity_search_results_list, container, false);
			list=(ListView)mainView.findViewById(R.id.game_list);
			
			// LoadMore button
			moreResultsButton = new Button(a);
			moreResultsButton.setText("Load More");
			moreResultsButton.setOnClickListener(new View.OnClickListener()
			{
	
				@Override
				public void onClick(View arg0)
				{
					RetrieveAdditionalResultsTask task = new RetrieveAdditionalResultsTask(a, cache, searchResultsTask);
					task.execute();
				}
			});
	
			// Adding Load More button to listview at bottom
			list.addFooterView(moreResultsButton);
	
	        // Click event for single list row
	        list.setOnItemClickListener(new OnItemClickListener() {
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) 
				{
					Log.i(TAG, "onChildClick: position: " + position + " id: " + id);
					Game game = (Game)list.getAdapter().getItem(position);
					GameInfoFragment.setGame(game);
					Fragment gameFragment = new GameInfoFragment();
					FragmentTools.transitionToFragment(a, gameFragment, null);
					
				}
			});		
	        searchQuery=(TextView)mainView.findViewById(R.id.search_query);
	        searchQuery.setVisibility(View.VISIBLE);
	        
			if(savedInstanceState != null)
			{
				//restore previous state
				restoreInstanceState(savedInstanceState);
			}
			
			Log.i(TAG, "Current Query: " + query);
			
			if(query != null)
			{
				searchResultsTask = new RetrieveSearchResultsTask(this, list, cache, query);
				searchResultsTask.doPostExecute(TaskResult.TRUE); //set up everything
			}
			
		}

        return mainView;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        searchItem = menu.findItem(R.id.action_search);
        setupSearchView((SearchView) searchItem.getActionView());
	}
	
	
    private void setupSearchView(SearchView view) 
    {
    	searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
                    | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	view.setSubmitButtonEnabled(true);

    	view.setOnQueryTextListener(this);
    }


	@Override
	public boolean onQueryTextChange(String newText)
	{
		return false;
	}
	
	
	public void updateTask(RetrieveSearchResultsTask task)
	{
		searchResultsTask = task;
		GamePool.getInstance().setQuery(task.getQuery());
		query = GamePool.getInstance().getQuery();

		
		GamePool.getInstance().setSearchResultsCache(task.getCache());
		cache = GamePool.getInstance().getSearchCache();

		Log.i(TAG, "Updating data to reflect new search: " + query);
		Log.i(TAG, cache.toString());
	}

	@Override
	public boolean onQueryTextSubmit(String newQuery)
	{
		newQuery = newQuery.trim();
		GameCache newCache = new GameCache(newQuery + " Search Result Cache"); //clear cache, new search
		RetrieveSearchResultsTask newTask = new RetrieveSearchResultsTask(this, list, newCache, newQuery);
		newTask.execute();
		searchItem.collapseActionView();
		return true;
	}
}
