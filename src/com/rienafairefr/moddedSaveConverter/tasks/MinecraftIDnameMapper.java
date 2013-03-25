package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftApp;

public class MinecraftIDnameMapper extends CustomTask{

	@SuppressWarnings("unchecked")
	public static TreeMap<Object,String[]>[] idmap= new TreeMap[ConverterMain.Ntype];
	public static String stringdocument;
	
	public MinecraftIDnameMapper(MinecraftApp minecraftapp) {
		super();
		this.minecraftapp = minecraftapp;
		this.minecrafthashesdir=new File(this.minecraftapp.MCjar.getParentFile(),"/moddedsaveconverter");
		this.cachefile=new File(this.minecrafthashesdir,this.minecraftapp.jarFile+minecraftapp.md5+".IDcache");
		if (!minecrafthashesdir.exists()){
			if (!minecrafthashesdir.mkdir()){
				Logger.logln("Couldn't create the ID map cache directory");
			}
		}
	}

	public File cachefile,minecrafthashesdir;
	
	private MinecraftApp minecraftapp;
	
	@SuppressWarnings("unchecked")
	public void IDMapMinecraft(){
		ConverterMain.isdone.put("idmap"+minecraftapp.index,false);
		ConverterMain.options.save();
		
		File cachefileHR=new File(this.minecrafthashesdir,minecraftapp.jarFile+minecraftapp.md5+".HumanReadableIDcache");
		File MClaunchoutput=new File(this.minecrafthashesdir,minecraftapp.jarFile+minecraftapp.md5+".MClaunchoutput");

		if ( cachefile.exists() && !MainFrame.relaunchidmaps[minecraftapp.index].isSelected()){
			try {
				FileInputStream fis = new FileInputStream(cachefile);
				ObjectInputStream ois = new ObjectInputStream(fis);
				for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
					idmap[ntype]=new TreeMap<Object, String[]>();
					idmap[ntype]=(TreeMap<Object, String[]>) ois.readObject();
				}
				ois.close();
				Logger.logln("Finished reading the ID map for Minecraft"+minecraftapp.index+" from cache");
			} catch ( IOException e) {
				Logger.logln("Couldn't read the ID map  cache");
			} catch ( ClassNotFoundException e) {
				Logger.logln("Couldn't read the ID map  cache");
			}
		}else{
			try {
				Logger.logln("Launching Minecraft"+minecraftapp.index+" to get the ID map...");

				JOptionPane.showMessageDialog(null, "Launching Minecraft"+minecraftapp.index+" to get the ID map... \nYou might need to load the map once Minecraft is launched");

				idmap=null;
				MinecraftLauncher launcher=new MinecraftLauncher(minecraftapp);
				launcher.LaunchMinecraft();
				idmap=MinecraftLauncher.idmap;
				//				ConverterMain.getInstance().idmaps.wait();
				if (idmap!=null){
					FileOutputStream fos=new FileOutputStream(cachefile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
						oos.writeObject(idmap[ntype].get(minecraftapp.index));
					}
					oos.close();
					Logger.logln("Finished launching Minecraft"+minecraftapp.index+" and wrote the ID map to cache");
				}
			} catch (IOException e) {
				Logger.logln("Couldn't launch Minecraft or write the resulting ID map to cache");
			}
			// Human readable versions of the ID dumps, with the output of the console as well
			try {

				FileWriter writer = new FileWriter(cachefileHR);
				for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
					writer.append("TYPE ID NAME\n");
					List keys=(new ArrayList(idmap[ntype].keySet()));
					Iterator it=keys.iterator();
					while(it.hasNext()){
						Object id=it.next();
						writer.append(ntype+" "+id+" "+idmap[ntype].get(id)+"\n");
					}
				}
				writer.close();

				FileWriter writer2 = new FileWriter(MClaunchoutput);
				writer2.append(stringdocument);
				writer2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		idmap[0].put(0, new String[]{"Air",""});
		idmap[1].put(0, new String[]{"Air",""});
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			Logger.logln(idmap[ntype].size()+" "+ConverterMain.typenames[ntype]+" IDs found");
			ConverterMain.idmaps[ntype].set(minecraftapp.index,idmap[ntype]);
		}
		ConverterMain.isdone.put("idmap"+minecraftapp.index,true);
		MainFrame.updateframe();
		return;
	}
	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Mapping the IDs....");
		Logger.level=1;
		IDMapMinecraft();
		Logger.level=0;
	}
}

