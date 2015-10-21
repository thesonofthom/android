package com.thesonofthom.myboardgames.fragments;


import com.thesonofthom.myboardgames.activities.BaseActivity.Navigation;

/**
 * Fragment for the "All Games" view. Shows all of the games the user owns, including ones on loan
 * @author Kevin Thomson
 *
 */
public class AllGamesFragment extends LocalGamesFragment
{
	private static final String TAG = "AllGamesFragment";
	public AllGamesFragment()
	{
		super(TAG, Navigation.AllGames);
	}
}
