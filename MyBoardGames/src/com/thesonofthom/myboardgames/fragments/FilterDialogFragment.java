package com.thesonofthom.myboardgames.fragments;

import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.tools.Filter;
import com.thesonofthom.myboardgames.tools.Filter.FilterOption;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class FilterDialogFragment extends DialogFragment
{
	private static Filter activeFilter;
	private static final String TAG = "FilterDialogFragment";
	private Filter filter;

	public static FilterDialogFragment newInstance(FilterDialogListener listener, Filter filter)
	{
		FilterDialogFragment fragment = new FilterDialogFragment();
		activeFilter = filter;
		fragment.setTargetFragment((Fragment)listener, 0);
		return fragment;
	}
	
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		filter = activeFilter;
		builder.setTitle("Filter by...").setItems(FilterOption.filterOptions, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				FilterOption option = FilterOption.get(which);
				FilterAlertDialog d = null;
				switch(option)
				{
					case Number_of_Players:
						d = new NumberOfPlayersAlertDialog();
						break;
					case Playing_Time:
						d = new PlayingTimeFilterDialog();
						break;
					case Year_Published:
						d = new YearPublishedFilterDialog();
						break;
					case Category:
						d = new CategoriesFilterDialog();
						break;
				}
				
				if(d != null)
				{
					d.showDialog();
				}
			}
		});
		AlertDialog d = builder.create();
		
		return d;
	}

	public class NumberOfPlayersAlertDialog extends FilterAlertDialog
	{
		private EditText numberOfPlayersView;
		public NumberOfPlayersAlertDialog()
		{
			super(FilterOption.Number_of_Players);
		}

		@Override
		public void setView(Builder b)
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_filter_number_of_players, null);
			b.setView(view);
			numberOfPlayersView = (EditText) view.findViewById(R.id.filter_number_of_players_edittext);
			
			if(filter.isFilterActive(FilterOption.Number_of_Players))
			{
				numberOfPlayersView.setText(Integer.toString(filter.getNumberOfPlayers()));
			}
			
			numberOfPlayersView.addTextChangedListener(new TextWatcher()
			{
				public void onTextChanged(CharSequence s, int start, int count, int after){}
				public void beforeTextChanged(CharSequence s, int start, int before, int count){}
				
				@Override
				public void afterTextChanged(Editable s)
				{
					refreshButtonState(); //only enable the button if the user has entered something in the text box
				}
			});
		}

		@Override
		public void populateFilter(Filter f)
		{
			int numberOfPlayers = Integer.parseInt(numberOfPlayersView.getText().toString());
			f.setNumberOfPlayers(numberOfPlayers);
		}

		@Override
		public boolean getButtonState()
		{
			Editable text = numberOfPlayersView.getText();
			return text != null && text.length() > 0;
		}
	}
	
	public class PlayingTimeFilterDialog extends FilterAlertDialog implements TextWatcher
	{
		private EditText minPlayingTimeView;
		private EditText maxPlayingTimeView;
		public PlayingTimeFilterDialog()
		{
			super(FilterOption.Playing_Time);
		}

		@Override
		public void setView(Builder b)
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_filter_playing_time, null);
			b.setView(view);
			minPlayingTimeView = (EditText) view.findViewById(R.id.filter_min_playing_time_edittext);
			minPlayingTimeView.addTextChangedListener(PlayingTimeFilterDialog.this);
			maxPlayingTimeView = (EditText) view.findViewById(R.id.filter_max_playing_time_edittext);
			maxPlayingTimeView.addTextChangedListener(PlayingTimeFilterDialog.this);
		
			if(filter.isFilterActive(FilterOption.Playing_Time))
			{
				minPlayingTimeView.setText(Integer.toString(filter.getMinPlayingTime()));
				maxPlayingTimeView.setText(Integer.toString(filter.getMaxPlayingTime())); 
			}
			else
			{
				minPlayingTimeView.setText(Integer.toString(filter.getInitialMinPlayingTime()));
				maxPlayingTimeView.setText(Integer.toString(filter.getInitialMaxPlayingTime()));
			}
		}

		@Override
		public void populateFilter(Filter f)
		{
			f.setPlayingTime(getMin(), getMax());
		}

		@Override
		public boolean getButtonState()
		{
			Editable minText = minPlayingTimeView.getText();
			boolean  minValid = minText != null && minText.length() > 0;
			
			Editable maxText = maxPlayingTimeView.getText();
			boolean  maxValid = maxText != null && maxText.length() > 0;
			
			return (minValid && maxValid && (getMin() <= getMax()));
		}
		
		private int getMin()
		{
			return Integer.parseInt(minPlayingTimeView.getText().toString());
		}
		
		private int getMax()
		{
			return Integer.parseInt(maxPlayingTimeView.getText().toString());
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			refreshButtonState();
		}

		public void onTextChanged(CharSequence s, int start, int count, int after){}
		public void beforeTextChanged(CharSequence s, int start, int before, int count){}
	}
	
	public class YearPublishedFilterDialog extends FilterAlertDialog implements TextWatcher
	{
		private EditText minYearView;
		private EditText maxYearView;
		public YearPublishedFilterDialog()
		{
			super(FilterOption.Year_Published);
		}

		@Override
		public void setView(Builder b)
		{
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_filter_year_published, null);
			b.setView(view);
			minYearView = (EditText) view.findViewById(R.id.filter_min_year_edittext);
			minYearView.addTextChangedListener(YearPublishedFilterDialog.this);
			maxYearView = (EditText) view.findViewById(R.id.filter_max_year_edittext);
			maxYearView.addTextChangedListener(YearPublishedFilterDialog.this);
			if(filter.isFilterActive(FilterOption.Year_Published))
			{
				minYearView.setText(Integer.toString(filter.getMinYear()));
				maxYearView.setText(Integer.toString(filter.getMaxYear())); 
			}
			else
			{
				minYearView.setText(Integer.toString(filter.getInitialMinYear()));
				maxYearView.setText(Integer.toString(filter.getInitialMaxYear()));
			}
		}

		@Override
		public void populateFilter(Filter f)
		{
			f.setYear(getMin(), getMax());
		}

		@Override
		public boolean getButtonState()
		{
			Editable minText = minYearView.getText();
			boolean  minValid = minText != null && minText.length() > 0;
			
			Editable maxText = maxYearView.getText();
			boolean  maxValid = maxText != null && maxText.length() > 0;
			
			return (minValid && maxValid && (getMin() <= getMax()));
		}
		
		private int getMin()
		{
			return Integer.parseInt(minYearView.getText().toString());
		}
		
		private int getMax()
		{
			return Integer.parseInt(maxYearView.getText().toString());
		}

		@Override
		public void afterTextChanged(Editable s)
		{
			refreshButtonState();
		}

		public void onTextChanged(CharSequence s, int start, int count, int after){}
		public void beforeTextChanged(CharSequence s, int start, int before, int count){}
	}
	
	public class CategoriesFilterDialog extends FilterAlertDialog
	{
		public CategoriesFilterDialog()
		{
			super(FilterOption.Category);
		}
		
		private boolean[] selections;

		@Override
		public void setView(Builder b)
		{
			String[] categories = filter.getAllCategories().toArray(new String[filter.getAllCategories().size()]);
			
			selections = new boolean[categories.length];
			for(int i = 0; i < selections.length; i++)
			{
				String category = categories[i];
				if(filter.getCategories().contains(category))
				{
					selections[i] = true;
				}
			}
			
			b.setMultiChoiceItems(categories, selections, new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					selections[which] = isChecked;
					refreshButtonState();
				}
			});
		}

		@Override
		public void populateFilter(Filter f)
		{
			filter.getCategories().clear();
			for(int i = 0; i < selections.length; i++)
			{
				if(selections[i])
				{
					filter.getCategories().add(filter.getAllCategories().get(i));
				}
			}
		}

		@Override
		public boolean getButtonState()
		{
			for(boolean selection : selections)
			{
				if(selection)
				{
					return true;
				}
			}
			return false;
		}
	}
	
	private abstract class FilterAlertDialog
	{
		private FilterOption option;
		private AlertDialog dialog;
		FilterDialogListener listener;
		
		public FilterAlertDialog(FilterOption option)
		{
			this.option = option;
			listener = (FilterDialogListener)getTargetFragment();;
		}
		
		public abstract void setView(AlertDialog.Builder b);
		
		public abstract void populateFilter(Filter f);
		
		public abstract boolean getButtonState();
		
		public void clearFilter()
		{
			filter.clear(option);
		}
		
		public void refreshButtonState()
		{
			setButtonState(getButtonState());
		}
		
		public void setButtonState(boolean enabled)
		{
			if(dialog != null)
			{
				dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(enabled);
			}
		}
		
		public void setClearFilterButtonState()
		{
			dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setEnabled(filter.isFilterActive(option));
		}
		
		public void showDialog()
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle(option.toString());
			setView(builder);
			builder.setPositiveButton("Filter", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					populateFilter(filter);
					listener.onFilterChanged();
				}
			});
			builder.setNegativeButton("Cancel", new OnClickListener()
			{
				public void onClick(DialogInterface dialog, int which){}
			});
			
			builder.setNeutralButton("Clear Filter", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					clearFilter();
					listener.onFilterChanged();
				}
				
			});

			dialog = builder.create();
			dialog.show();
			refreshButtonState();
			setClearFilterButtonState();
		}
	}
	
	public interface FilterDialogListener
	{
		public void onFilterChanged();
	}
}
