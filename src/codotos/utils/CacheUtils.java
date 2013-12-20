package codotos.utils;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;


public final class CacheUtils {
	
	
	/*
		Load an object from a cached file
		
		@return Object Cached Object
	*/
	public static Object getCachedObject(String sCachedFile) throws java.io.FileNotFoundException, java.io.IOException, java.lang.ClassNotFoundException{
	
		// Create a file input stream to load the object cache from
		FileInputStream oFileStream = new FileInputStream(sCachedFile);
		ObjectInputStream oObjectStream = new ObjectInputStream(oFileStream);
			
		// Load our routes
		Object toReturn = oObjectStream.readObject();
		
		// Close the stream
		oObjectStream.close();
		
		return toReturn;
		
	}
	
	
	/*
		Save the object to a cache file
		
		@param oCachedObject Object Object to cache
		
		@return null
	*/	
	public static void setCachedObject(Object oCachedObject,String sCachedFile) throws java.io.FileNotFoundException, java.io.IOException{
		
		// Open our object stream
		FileOutputStream oFileStream = new FileOutputStream(sCachedFile);
		ObjectOutputStream oObjectStream = new ObjectOutputStream(oFileStream);
		
		// Write our routes array
		oObjectStream.writeObject(oCachedObject);
		
		// Close our object stream
		oObjectStream.close();
		
	}
	
	
	/*
		Check if the cache is current
		Cache is expired when:
			No cache file exists
			Original file is modified later than cache file
		
		@return Object Cached Object
	*/
	public static Boolean isCacheCurrent(String sOriginalFile, String sCachedFile) throws codotos.exceptions.FileNotFoundException {
		
		// Get the cache file
		File oCacheFile = new File(sCachedFile);
		
		// if file does not exist, cache is not current because it does not exist
		if(!oCacheFile.exists()){
			return false;
		}
		
		// get the original file
		File oOriginalFile = new File(sOriginalFile);
		
		if(!oOriginalFile.exists()){
			
			throw new codotos.exceptions.FileNotFoundException("File '"+ sOriginalFile +"' does not exist");
			
		}
		
		// If the file was last modified after our last cache was created, our cache is no good
		if(oOriginalFile.lastModified() > oCacheFile.lastModified()){
			return false;
		}
		
		// If we got here, the cache is good and can be trusted
		return true;
		
	}


}


