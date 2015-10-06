package com.thesonofthom.myboardgames.adapters;

/*
 * Interface that any Adapter that displays a Game should implement.
 * The refresh() method is called whenever the state of the game chances and should be used to notifyDataSetChanged()
 * 
 * This is needed because we want an easy way to notify ALL adapters, both ones that extent from BaseExpandableListAdapter and BaseAdapter.
 * Both of these have independently declared a notifyDataSetChanged() method, and we want to easily be able to call both
 */
public interface GameAdapter
{
	public void refresh();
}
