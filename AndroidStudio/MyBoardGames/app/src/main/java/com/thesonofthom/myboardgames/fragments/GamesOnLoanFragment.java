package com.thesonofthom.myboardgames.fragments;


import android.view.View;

import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

/**
 * Fragment for the "Games on Loan" view. This will show the subset of the users owned games that are currently on loan to other people
 * @author Kevin Thomson
 *
 */
public class GamesOnLoanFragment extends LocalGamesFragment
{
	private static final String TAG = "GamesOnLoanFragment";
	public GamesOnLoanFragment()
	{
		super(TAG, Navigation.GamesOnLoan);
	}
	
	@Override
	public void setupEmptyView()
	{
		emptyView_sadFace.setVisibility(View.GONE);
		emptyView_sadFace = null;
		
		emptyView_text1.setText("You currently have no games on loan.");
		emptyView_text2.setVisibility(View.GONE);
		emptyView_text2 = null;
	}
}
