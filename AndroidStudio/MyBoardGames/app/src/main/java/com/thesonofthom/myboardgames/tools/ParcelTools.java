package com.thesonofthom.myboardgames.tools;

import android.os.Parcel;

/**
 * Tools to help write data to Parcel objects
 * @author Kevin Thomson
 *
 */
public class ParcelTools
{
	public static final String NULL = "null";
	
	//for some reason parcel doesn't have the ability to read/write boolenas
	public static void writeBoolean(Parcel parcel, boolean b)
	{
		parcel.writeInt(b ? 1 : 0);
	}
	
	public static boolean readBoolean(Parcel parcel)
	{
		int val = parcel.readInt();
		return val != 0;
	}
	
	public static void writeEnum(Parcel parcel, Enum<?> enumVal)
	{
		if(enumVal != null)
		{
			parcel.writeString(enumVal.name());
		}
		else
		{
			parcel.writeString(NULL);
		}
	}
	
	public static <T extends Enum<T>> T readEnum(Parcel parcel, Class<T> classType)
	{
		String enumVal = parcel.readString();
		if(!enumVal.equals(NULL))
		{
			return Enum.valueOf(classType, enumVal);
		}
		return null;
		
	}

}
