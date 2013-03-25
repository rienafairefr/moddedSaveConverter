package com.rienafairefr.moddedSaveConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class MinecraftSave {
	@SuppressWarnings("unchecked")
	public List<Object>[] listofidsinsave=new ArrayList[ConverterMain.Ntype];
	@SuppressWarnings("unchecked")
	public TreeMap<Object,Integer>[] nidsinsave=new TreeMap[ConverterMain.Ntype];
	private File rootfolder;
	public int type=0;

	public MinecraftSave(File save, int b) throws BadMinecraftSaveException {
		this.type=b;
		this.setRootfolder(save);
	}

	public String getLeveldatpath() {
		try {
			if (rootfolder!=null){
				File leveldat=new File(rootfolder,"level.dat");
				if (leveldat.exists())
					return leveldat.getCanonicalPath();
			}else{
				return "";
			}
		} catch (IOException e) {
			Logger.log("Problem with the save path");
		}
		return null;
	}

	public void setRootfolder(File rootfolder) {
		this.rootfolder = rootfolder;
	}

	public File getRootfolder() {
		return rootfolder;
	}

	public String getRootfolderpath() {
		if (rootfolder!=null){
			return rootfolder.getAbsolutePath();
		}else{
			return "";
		}
	}
	public File getcachefile() {
		if (rootfolder!=null){
			return new File(rootfolder,"moddedSaveConverter.cachebin");
		}else{
			return null;
		}
	}
}
