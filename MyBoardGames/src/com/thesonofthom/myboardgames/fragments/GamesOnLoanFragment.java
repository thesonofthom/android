package com.thesonofthom.myboardgames.fragments;


import android.view.View;

import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

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
