package com.rienafairefr.moddedSaveConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MinecraftApp {

	//public String minecraftdir;
	//public String jarFile;
	public File MCdir; //the directory where bin,saves etc are
	public File MCjar; //the file representing the jar file
	public String jarFile;
	public String minecraftdir;
	public String version;
	public int index; //0 (origin) or 1(destination)
	public String md5;

	public static String getMD5(MinecraftApp minecraftapp) throws NoSuchAlgorithmException, FileNotFoundException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream is = new FileInputStream(minecraftapp.MCjar);                
		byte[] buffer = new byte[8192];
		int read = 0;
		try {
			while( (read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}       
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			//System.out.println("MD5: " + output);
			return output;
		}
		catch(IOException e) {
			throw new RuntimeException("Unable to process jar file for MD5", e);
		}
		finally {
			try {
				is.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Unable to close input stream for MD5 calculation", e);
			}
		} 
	}

	public MinecraftApp(String minejar, String vers, int index) throws BadMinecraftAppException {
		this(new File(minejar), vers, index);
	}
	public MinecraftApp(File minejar, String vers, int index) throws BadMinecraftAppException {
		File minedir=minejar.getParentFile().getParentFile().getParentFile();
		if (!minedir.exists() || !minejar.exists()){
			throw(new BadMinecraftAppException());
		}else{
			this.MCdir=minedir;
			this.MCjar=minejar;
			this.jarFile=minejar.getAbsolutePath();
			this.minecraftdir=minedir.getAbsolutePath();
		}
		if (Arrays.asList(ConverterMain.availableversions).indexOf(vers)!=1){
			this.version=vers;
		}else{
			throw(new BadMinecraftAppException());
		}
		if (index==0 || index==1){
			this.index=index;
		}else{
			throw(new BadMinecraftAppException());
		}
		try {
			md5 = getMD5(this);
		} catch (FileNotFoundException e) {
			Logger.logln("Couldn't get the md5 of the jar File");
			throw(new BadMinecraftAppException());
		} catch (NoSuchAlgorithmException  e) {
			Logger.logln("Couldn't get the md5 of the jar File");
			throw(new BadMinecraftAppException());
		}
	}
}
