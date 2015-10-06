package com.thesonofthom.myboardgames.fragments;


import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

public class AllGamesFragment extends LocalGamesFragment
{
	private static final String TAG = "AllGamesFragment";
	public AllGamesFragment()
	{
		super(TAG, Navigation.AllGames);
	}
}
