package com.thesonofthom.myboardgames.bgg;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.thesonofthom.myboardgames.Game;
import com.thesonofthom.myboardgames.GamePool;
import com.thesonofthom.myboardgames.R;
import com.thesonofthom.myboardgames.Game.Property;
import com.thesonofthom.myboardgames.Game.PropertyData;
import com.thesonofthom.myboardgames.tools.FileTools;


public class BGGXMLWriter
{
	private static final String TAG = "BGGXMLWriter";
	
	private Context context;
	private Resources r;
	private File directory;
	
	public BGGXMLWriter(Context context)
	{
		this.context = context;
		r = context.getResources();
		
		
	}
	
	public void writeToXml(Game game) throws ParserConfigurationException, TransformerException, IOException
	{
		Log.i(TAG, "Writing " + game + " to xml");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("boardgames");
		doc.appendChild(rootElement);
		Element boardGame = doc.createElement(r.getString(R.string.bgg_boardgame_tag));
		rootElement.appendChild(boardGame);
		boardGame.setAttribute(r.getString(R.string.bgg_objectid_attribute), Integer.toString(game.getObjectId()));
		HashMap<Property, LinkedList<PropertyData>> properties = game.getProperties();
		for (Map.Entry<Property, LinkedList<PropertyData>> entry : properties.entrySet()) 
		{
			Iterator<PropertyData> iterator = entry.getValue().iterator();
			while(iterator.hasNext())
			{
				PropertyData data = iterator.next();
				Element property = doc.createElement(entry.getKey().name());
				if(data.objectid != Game.UNKNOWN_OBJECT_ID)
				{
					property.setAttribute(r.getString(R.string.bgg_objectid_attribute), Integer.toString(data.objectid));
				}
				if(entry.getKey() == Property.name)
				{
					property.setAttribute(r.getString(R.string.bgg_primary_name), "true");
				}
				
				if(data.value == null)
				{
					Log.e(TAG, "Data value is null! Property: " + data.name);
				}
				
				property.appendChild(doc.createTextNode(data.value));
				boardGame.appendChild(property);
			}
			
		}
		
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		
		File file = getGameFile(game);
		
		StreamResult result = new StreamResult(file);
 
		transformer.transform(source, result);
		game.clearDirty(); //game is saved, so clear the dirty flag in case we try to save it again
		Log.i(TAG, "Saved to " + file);
	}
	
	public File getGameFile(Game game)
	{
		File directory = context.getExternalFilesDir(FileTools.GAME_STORAGE_DIRECTORY);
		return new File(directory, game.getFileName());
	}
}
