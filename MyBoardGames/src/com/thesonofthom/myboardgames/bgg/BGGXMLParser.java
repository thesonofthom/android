package com.thesonofthom.myboardgames.bgg;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GameCache;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.asynctask.DialogAsyncTask;

/*
 * Class to parse the xml data retrived from BoardGameGeek.com's XML API
 * See: http://boardgamegeek.com/wiki/page/BGG_XML_API
 */

public class BGGXMLParser
{
	private static String TAG = "BGGXMLParser";
	private Resources r;
	
	private GameCache cache;
	
	public BGGXMLParser(Context context, GameCache cache)
	{
		this.r = context.getResources();
		this.cache = cache;
	}
	
	public static enum XMLType
	{
		SEARCH_RESULT,
		USER_COLLECTION,
		GAME_INFO,
	}
	
	
	private Document parseXml(String xml) throws ParserConfigurationException, SAXException, IOException
	{
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		DocumentBuilder db = dbf.newDocumentBuilder();

		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xml));
		doc = db.parse(is); 
		return doc;
	}
	

	
	private Integer getObjectId(Node node)
	{
		Integer objectid = null;
		try
		{
			if(node.hasAttributes())
			{
				Node objectIdNode = node.getAttributes().getNamedItem(r.getString(R.string.bgg_objectid_attribute));
				if(objectIdNode != null)
				{
					objectid = Integer.parseInt(objectIdNode.getNodeValue());
				}
			}
			return objectid;
		}
		catch(Exception e)
		{
			Log.e("ERROR", e.getMessage());
			return null;
		}
	}
	
	
	

	public boolean parseGameList(String xml, XMLType type) throws ParserConfigurationException, SAXException, IOException
	{
		Log.i(TAG, "Parsing XML...");
		Document doc = parseXml(xml);
		int tagResource;
		if(type == XMLType.USER_COLLECTION)
		{
			tagResource = R.string.bgg_usercollection_tag;
		}
		else
		{
			tagResource = R.string.bgg_boardgame_tag;
		}
		NodeList boardGames = doc.getElementsByTagName(r.getString(tagResource));
		boolean success = false;

		for(int i = 0; i < boardGames.getLength(); i++)
		{
			if(DialogAsyncTask.isDone())
			{
				Log.i(TAG, "Task Cancelled. Aborting parse.");
				return false;
			}
			Node boardGame = boardGames.item(i);
			
			Integer objectid = getObjectId(boardGame);
			if(objectid == null)
			{
				continue;
			}
			
			Game game = GamePool.getInstance().get(objectid);
			if(game == null)
			{
				game = new Game(objectid); //will automatically get added to the pool
			}
			
			boolean validGame = false;
			NodeList properties = boardGame.getChildNodes();
			for(int j = 0; j < properties.getLength(); j++)
			{
				if(DialogAsyncTask.isDone())
				{
					Log.i(TAG, "Task Cancelled. Aborting parse.");
					return false;
				}
				Node property = properties.item(j);
				if(property.getNodeType() == Node.ELEMENT_NODE)
				{
					String propertyName = property.getNodeName();

					Property propertyEnum = Property.getProperty(propertyName);
					if(propertyEnum == Property.name)
					{
						validGame = true;
					}
					
					if(propertyEnum == null)
					{
						continue; //not a property we're interested in
					}
					
					if(game.get(propertyEnum) != null && !propertyEnum.allowMultiple())
					{
						continue; //already have this property
					}
					
					Integer pobjectid = getObjectId(property);
					
					String propertyValue = property.getTextContent().trim();
					
					//check for the validity of the integer properties
					switch(propertyEnum)
					{
						case yearpublished:
						case minplayers:
						case maxplayers:
						case age:
						case playingtime:
						{
							try
							{
								int propertyValueInt = Integer.parseInt(propertyValue);
								if(propertyValueInt <= 0)
								{
									continue; //not a valid property, skip it
								}
							}
							catch(Exception e)
							{
								continue; //not a valid property, skip it
							}
							break;
						}
						default:
					}
					
					if(propertyEnum == Property.name)
					{
						if(type == XMLType.GAME_INFO &&
								(!property.hasAttributes() || property.hasAttributes() && property.getAttributes().getNamedItem(r.getString(R.string.bgg_primary_name)) == null))
						{
							continue; //don't care about property
						}
					}
					else if(propertyEnum == Property.boardgameexpansion)
					{
						if(property.hasAttributes() && property.getAttributes().getNamedItem("inbound") != null)
						{
							propertyEnum = Property.baseboardgame; //not actually an expansion, but instead a link back to the base game
						}
					}
					else if(propertyEnum == Property.thumbnail || propertyEnum == Property.image)
					{
						if(propertyValue.startsWith("//"))
						{
							propertyValue = "http:" + propertyValue;
						}
					}
					
					if(propertyValue.toLowerCase().contains(r.getString(R.string.bgg_fan_expansion)))
					{
						if(propertyEnum == Property.name)
						{
							validGame = false;
							break;
						}
						else
						{
							continue; //don't care about property
						}
					}
					
					//if we get to this point, this is a valid property and we need to add it to the game
					if(pobjectid == null)
					{
						game.add(propertyEnum, propertyValue);
					}
					else
					{
						game.add(propertyEnum, pobjectid, propertyValue);
					}
				}
			}
			
			if(type == XMLType.SEARCH_RESULT)
			{
				game.setStatus(Game.STATUS_MINIMAL);
			}
			else if(type == XMLType.GAME_INFO)
			{
				game.setStatus(Game.STATUS_FULL);
			}
			
			if(validGame)
			{
				Log.i(TAG, "  GAME: " + game.getObjectId() +" " + game.get(Property.name));
				if(cache != null)
				{
					cache.cache(game);
				}
				success = true;
			}
		}
		Log.i(TAG, "Done parsing XML");
		return success;
	}

}
