package com.thesonofthom.myboardgames.fragments;


import java.util.ArrayList;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.GameSorter;
import com.thesonofthom.myboardgames.GameSorter.SortOption;
import com.thesonofthom.myboardgames.GameSorter.SortOrder;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;
import com.thesonofthom.myboardgames.adapters.GameListAdapter;
import com.thesonofthom.myboardgames.asynctask.RemoveGamesTask;
import com.thesonofthom.myboardgames.asynctask.RetrieveUserCollectionTask;
import com.thesonofthom.myboardgames.fragments.FilterDialogFragment.FilterDialogListener;
import com.thesonofthom.myboardgames.tools.Filter;
import com.thesonofthom.myboardgames.tools.FragmentTools;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Base class for all of the Local games lists ("All Games", "My Games", "Games on Loan")
 */
public abstract class LocalGamesFragment extends BaseFragment implements FilterDialogListener
{
	protected GameCache cache;
	protected Filter filter;
	protected GameListAdapter adapter;
	protected ListView listView;
	
	protected View mainView;
	
	protected ImageView clearFiltersView;
	protected TextView activeFiltersTextView;
	protected View filterDivider;
	
	protected TextView sortTextView;
	protected Spinner sortSpinner;
	protected CheckBox reverseDirectionCheckBox;
	
	protected TextView emptyView_text1;
	protected ImageView emptyView_sadFace;
	protected TextView emptyView_text2;
	
	
	
	private SortOption option = SortOption.NAME;
	private SortOrder order = SortOrder.ASCENDING;
	
	
	public LocalGamesFragment(String TAG, Navigation fragmentType)
	{
		super(TAG, fragmentType);
	}
	
	@Override
	public String getTitle()
	{
		String title = super.getTitle();
		if(cache != null && cache.getSize() > 0)
		{
			title += " (";
			
			if(filter.isActive())
			{
				title += filter.getCount(cache) + "/";
			}
			
			title += cache.getSize() + ")";
		}
		return title;
	}
	
	public void setupEmptyView()
	{
		//use default
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		if(mainView == null)
		{
			Log.i(TAG, "Inflating view...");
			mainView = inflater.inflate(R.layout.activity_my_games_list, container, false);
			listView = (ListView)mainView.findViewById(R.id.my_game_list);
		
			clearFiltersView = (ImageView)mainView.findViewById(R.id.clear_filters_button);
			clearFiltersView.setVisibility(View.GONE);
			clearFiltersView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					filter.clearAllFilters();
					onFilterChanged();
				}
			});
			activeFiltersTextView = (TextView)mainView.findViewById(R.id.activeFiltersTextView);
			activeFiltersTextView.setVisibility(View.GONE);
			
			filterDivider = mainView.findViewById(R.id.filter_divider);
			filterDivider.setVisibility(View.GONE);
			
			refreshFilterView();
			
			
			sortTextView = (TextView)mainView.findViewById(R.id.sortTextView);
		
			emptyView_text1 = (TextView)mainView.findViewById(R.id.my_game_list_empty);
			emptyView_sadFace = (ImageView)mainView.findViewById(R.id.sad_face);
			emptyView_text2 = (TextView)mainView.findViewById(R.id.my_game_list_empty_2);
			
			setupEmptyView();

			listView.setAdapter(adapter);
			
			sortSpinner = (Spinner)mainView.findViewById(R.id.spinner1);
			reverseDirectionCheckBox = (CheckBox)mainView.findViewById(R.id.reverseDirectionCheckBox);
			
			if(sortSpinner != null && reverseDirectionCheckBox != null)
			{
				ArrayList<SortOption> itemList = new ArrayList<SortOption>();
				SortOption[] values = SortOption.values();
				for(int i = 0; i < values.length; i++)
				{
					SortOption option = values[i];
					itemList.add(option);
				}
				ArrayAdapter<SortOption> aAdpt = new ArrayAdapter<SortOption>(a, R.layout.spinner_item, itemList);
				sortSpinner.setAdapter(aAdpt);
				sortSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
				{

					@Override
					public void onItemSelected(AdapterView<?> parent, View view,
							int pos, long id)
					{
						option = (SortOption)parent.getItemAtPosition(pos);
						sort();
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent)
					{
					}
				});
			
				reverseDirectionCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{
					
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
					{
						order = isChecked ? SortOrder.DESCENDING : SortOrder.ASCENDING;
						sort();
					}
				});
			}
			
			listView.setOnItemClickListener(new OnItemClickListener() {
	        	
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) 
				{
					Log.i(TAG, "onChildClick: position: " + position + " id: " + id);
					Game game = (Game)listView.getAdapter().getItem(position);
					GameInfoFragment.setGame(game);
					addAdapter(game);
					Fragment gameFragment = new GameInfoFragment();
					FragmentTools.transitionToFragment(a, gameFragment, null);
				}
			});		
			
		}
		setCorrectView();
		return mainView;
	}
	
	protected void addAdapter(Game game)
	{
		
	}

	
	private void sort()
	{
		if(cache.setSortOptions(option, order))
		{
			Log.i(TAG, "BEFORE SORT: " + cache);
			//TODO: Do this on a background thread
			GameSorter.sort(cache.getList(), option, order);
			Log.i(TAG, "AFTER SORT: " + cache);
			adapter = new GameListAdapter(a, cache, filter);
			if(listView != null)
			{
				listView.setAdapter(adapter);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
			
		cache = getCache();
		filter = getFilter();
		Log.i(TAG, cache.toString());
		adapter = new GameListAdapter(a, cache, filter);
	}
	
	protected GameCache getCache()
	{
		return GamePool.getInstance().getCache(fragmentType.getGroup());
	}
	
	protected Filter getFilter()
	{
		return GamePool.getInstance().getFilter(fragmentType.getGroup());
	}
	
	protected MenuItem filterItems;
	protected MenuItem reloadGamesIcon;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.gamelist, menu);
		filterItems = menu.findItem(R.id.action_filter_games);
		reloadGamesIcon = menu.findItem(R.id.action_reload_game_list);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		if(cache == null || cache.getSize() == 0)
		{
			filterItems.setVisible(false);
		}
		else
		{
			filterItems.setVisible(true);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_reload_game_list:
				return reloadGameListCheck();
			case R.id.action_filter_games:
				return filter();
//			case R.id.action_remove_all_games:
//				return removeAllGames();
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private boolean filter()
	{
		FilterDialogFragment dialog = FilterDialogFragment.newInstance(this, filter);
		dialog.show(getFragmentManager(), null);
		return true;
	}
	
	private boolean removeAllGames()
	{
		new AlertDialog.Builder(a)
		.setMessage(
				"This will remove ALL games from your games library. Are you sure you want to continue?\nTHIS ACTION CANNOT BE UNDONE.")
		.setPositiveButton("Yes. Remove All Games",
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,
							int which)
					{
						Game[] games = cache.getList().toArray(new Game[cache.getSize()]);
						RemoveGamesTask task = new RemoveGamesTask(LocalGamesFragment.this);
						task.execute(games);
					}
				})
		.setNegativeButton("No",
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog,
							int which)
					{
						// do nothing
					}
				}).show();
		return true;
	}
	
	private boolean reloadGameListCheck()
	{
		if (settings.getBGGAccount() == null)
		{
			new AlertDialog.Builder(a)
					.setMessage(
							"You have not yet specified your BoardGameGeekAccount.com Username in the Settings. Do you want to set it up now?")
					.setPositiveButton("Go to Settings",
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog,
										int which)
								{
									//a.transitionToFragment(Navigation.Settings);
									FragmentTools.transitionToFragment(a,
											new SettingsFragment(), null);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog,
										int which)
								{
									// do nothing
								}
							}).show();
			return true;
		}
		else
		{
			new AlertDialog.Builder(a)
			.setMessage(
					"This operation will pull all games linked on your BoardGameGeek.com profile to your Games Library. " +
					"This may take a long time. Do you want to continue?")
			.setTitle(settings.getBGGAccount())
			.setPositiveButton("Yes",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,
								int which)
						{
							reloadGameList();
						}
					})
			.setNegativeButton("No",
					new DialogInterface.OnClickListener()
					{
						public void onClick(DialogInterface dialog,
								int which)
						{
							// do nothing
						}
					}).show();
		}
		
		return true;
	}

	private void reloadGameList()
	{
		RetrieveUserCollectionTask task = new RetrieveUserCollectionTask(this, listView, cache, filter, settings.getBGGAccount());
		task.execute();
	}
	
	public void setCorrectView()
	{
		boolean showList = cache.getSize() > 0;
		Log.i(TAG, "setCorrectView(): show list: " + showList);
		listView.setVisibility(visibility(showList));
		sortTextView.setVisibility(visibility(showList));
		sortSpinner.setVisibility(visibility(showList));
		reverseDirectionCheckBox.setVisibility(visibility(showList));
		if(emptyView_sadFace != null)
		{
			emptyView_sadFace.setVisibility(visibility(!showList));
		}
		if(emptyView_text1 != null)
		{
			emptyView_text1.setVisibility(visibility(!showList));
		}
		if(emptyView_text2 != null)
		{
			emptyView_text2.setVisibility(visibility(!showList));
		}
		updateTitle();
	}
	
	private int visibility(boolean show)
	{
		return show ? View.VISIBLE : View.GONE;
	}
	
	private void refreshFilterView()
	{
		int visibility = filter.isActive() ? View.VISIBLE : View.GONE;
		clearFiltersView.setVisibility(visibility);
		activeFiltersTextView.setVisibility(visibility);
		filterDivider.setVisibility(visibility);
		if(filter.isActive())
		{
			activeFiltersTextView.setText(Html.fromHtml(filter.toString()));
		}
	}

	@Override
	public void onFilterChanged()
	{
		refreshFilterView();
		updateTitle();
		adapter.notifyDataSetChanged();
	}
}
