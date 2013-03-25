package com.rienafairefr.moddedSaveConverter.tasks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.Tag;
import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.MinecraftSave;

@SuppressWarnings("unchecked")
public class MinecraftAnalyzerConverter extends CustomTask{
	private static List<Object>[] problematicids=new ArrayList[ConverterMain.Ntype];
	private static List<Object>[] removedids=new ArrayList[ConverterMain.Ntype];
	private static List<Object>[] converteds=new ArrayList[ConverterMain.Ntype];
	private static TreeMap<Object,Integer>[] nconverteds=new TreeMap[ConverterMain.Ntype];

	private static int xc;
	private static int zc;
	private static String currentregionname;
	private static String currentmode;
	
	public MinecraftAnalyzerConverter(boolean convert) {
		super();
		this.convert = convert;
	}

	public boolean convert;

	public static int level=0;
	public static void traverseTags_treatitems(Tag orig,boolean convert){
		if (orig instanceof CompoundTag){
			CompoundTag ctagorig=(CompoundTag) orig;
			Collection alltags=ctagorig.getAllTags();
			Iterator it=alltags.iterator();
			level++;
			while (it.hasNext()){					
				traverseTags_treatitems((Tag) it.next(),convert);
			}
			level--;
		}
		if (orig instanceof ListTag){
			ListTag listTagorig=(ListTag) orig;
			Field list=null;
			Iterator it = null;
			try {
				list=ListTag.class.getDeclaredField("list");
				list.setAccessible(true);
				it = ((List)list.get(listTagorig)).iterator();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			level++;
			while(it.hasNext()){
				//Logger.log(new String(new char[level]).replace("\0", "-")+orig.getId());
				Tag tag=(Tag) it.next();
				if (tag instanceof CompoundTag){
					CompoundTag ctag=(CompoundTag) tag;
					if (listTagorig.getName().toLowerCase().indexOf("item")==-1){
						traverseTags_treatitems(ctag,convert);
					}else{
						int ntype=1;
						int id0=(int) ctag.getShort("id");
						int id1=(int)(Integer) ConverterMain.options.getconvertmaps()[1].get(id0)[0];
						count(ntype,id0,id1,convert);

						if (convert){
							int typefound = (int)(Integer) ConverterMain.options.getconvertmaps()[1].get(id0)[1];
							if (typefound>=0){
								if (id0!=id1 && !converteds[ntype].contains( id0)){
									ctag.putShort("id", (short )id1);
								}
							}
						}

					}
				}
			}
			level--;
		}
		//Logger.log(new String(new char[level]).replace("\0", "-")+orig.getId());
	}

	private static void count(int ntype, Object id0, Object id1,boolean convert) {
		//type=1  convert
		//type=-1 remove
		if (convert){
			if (id1.equals(Integer.valueOf(-1))){
				if (!id0.equals(id1) && !converteds[ntype].contains(id0)){
					Logger.logln(currentmode+" | Converting "+ConverterMain.typenames[ntype]+" ID: "+id0+" to "+id1+" in chunk "+xc+","+zc);
					converteds[ntype].add(id0);
					nconverteds[ntype].put(id0, 0);
				}
				if (!id0.equals(id1) && converteds[ntype].contains(id0)){
					nconverteds[ntype].put(id0, nconverteds[ntype].get(id0)+1);
				}
			}
			if (id1.equals(Integer.valueOf(-1))){
				if (!problematicids[ntype].contains(id0)){
					//Logger.logln("Found "+ConverterMain.typenames[ntype]+" ID :"+id0+" in the file that is not in the converter map");
					//Logger.logln("      First seen in region "+currentregionname+" X "+xc+" Z "+zc);
					Logger.logln(currentmode+" | Removing "+ConverterMain.typenames[ntype]+" ID: "+id0);

					problematicids[ntype].add(id0);
				}
			}
		}else{
			if (! ConverterMain.options.getsave(0).listofidsinsave[ntype].contains(id0)){
				ConverterMain.options.getsave(0).listofidsinsave[ntype].add(id0);
				ConverterMain.options.getsave(0).nidsinsave[ntype].put(id0,1);
			}else{
				ConverterMain.options.getsave(0).nidsinsave[ntype].put(id0,ConverterMain.options.getsave(0).nidsinsave[ntype].get(id0)+1);
			}
		}
	}
	public static void treatentities(ListTag Entitiesctag,int ntype, boolean convert){
		Field list=null;
		Iterator it = null;
		try {
			list=ListTag.class.getDeclaredField("list");
			list.setAccessible(true);
			it = ((List)list.get(Entitiesctag)).iterator();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		if (it!=null){
			while (it.hasNext()){
				CompoundTag tag=(CompoundTag) it.next();

				Object id0=tag.getString("id");
				//				//Object id0=ConverterMain.options.idmap[0].get(stringid);
				if (id0!=null){
					if (ConverterMain.options.getconvertmap(ntype).containsKey(id0)){
						Object id1=ConverterMain.options.getconvertmap(ntype).get(id0);
						if (convert){
							if (!id0.equals(id1) && !converteds[ntype].contains(id0)){
								tag.putString("id", id1.toString());
							}
							if (id1.equals(Integer.valueOf(-1)) || id1.equals(Integer.valueOf(-2))){
								it.remove();
							}
						}
						count(ntype,id0,id1,convert);
					}
				}
			}
		}
	}

	static byte Nibble4(byte[] arr, int index){ 
		if (index%2==0){
			return (byte) (arr[index/2] & 0x0f);
		}else{
			return (byte) ((arr[index/2]>>4) & 0x0F);
		}
	}
	public static void treatblocks(CompoundTag tag, boolean convert){
		byte[] blocks=tag.getByteArray("Blocks");
		byte[] blocksadd=tag.getByteArray("Add");

		byte[] blocks2=blocks;
		byte[] blocksadd2=blocksadd;

		int ntype=0;
		//CompoundTag tag2=(CompoundTag) tag.copy();
		for (int nx=0;nx<16;nx++){
			for (int ny=0;ny<16;ny++){
				for (int nz=0;nz<16;nz++){
					//int BlockPos = ny*16*16 + nz*16 + nx;
					int BlockPos =(ny << 8 | nz << 4 | nx);
					int id0;
					if (blocksadd.length>0){
						byte BlockID_a = blocks[BlockPos];
						byte BlockID_b = Nibble4(blocksadd, BlockPos);
						id0 = (int) (BlockID_a + (BlockID_b << 8));
					}else{
						byte BlockID_a = (byte) (blocks[BlockPos]);
						id0 = (int) (BlockID_a & 0xff);
					}
					//System.out.println(id0);
					//int nb = (ny << 8 | nz << 4 | nx);

					//int id0= (blocks[nb] << 8);
					if (convert){
						int nb2 = (nx << 4 | nz);
						if (ConverterMain.options.getconvertmaps()[0].containsKey(id0)){
							int id1=(int)(Integer)ConverterMain.options.getconvertmaps()[0].get(id0)[0];
							byte BlockID_a= (byte) (id1 & 0xFF);
							byte BlockID_b= (byte) ((id1>>8) & 0xFF);
							if (BlockID_b>0 && blocksadd2.length==0 ){
								//we need a add array
								blocksadd2=new byte[2048];
							}
							int typefound=(int)(Integer)ConverterMain.options.getconvertmaps()[0].get(id0)[1];
							if(typefound>=0){
								blocks2[BlockPos]=BlockID_a;
								//System.out.println(blocks[nb]+" "+blocks2[nb]+xc+" "+zc);
								if (blocksadd2.length>0){
									blocksadd2[BlockPos]=BlockID_b;
								}
							}else{
								//stone below the height map, air above, for blocks that are removed
								blocks2[BlockPos]=(byte) ((nz<=heightmap[nb2])?1:0);
								if (blocksadd2.length>0){
									blocksadd2[BlockPos]=0;
								}
							}
							count(ntype,id0,id1,convert);
						}
					}else{
						count(ntype,id0,null,convert);
					}
				}
			}
		}
		if (convert){
			tag.putByteArray("Block", blocks2);
			tag.putByteArray("Add", blocksadd2);
		}
	}

	public static void Print(){
		MinecraftSave save=ConverterMain.options.getsave(0);
		if (save!=null){
			if (ConverterMain.isdone.get("analyzesave") && ConverterMain.isdone.get("idmap0")){
				for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
					Iterator it=save.listofidsinsave[ntype].iterator();
					while (it.hasNext()){
						Object id=(Object) it.next();
						if (ConverterMain.idmaps[ntype].get(0).containsKey(id)){
							Logger.logln(save.nidsinsave[ntype].get(id)+" "+ConverterMain.typenames[ntype]+" ID "+id+ "("+ConverterMain.idmaps[ntype].get(0).get(id)[0]+")");
						}else{
							Logger.logln("problem"+id);
							return;
						}
					}
				}
			}
		}
	}

	

	public static void copyDirectory(File sourceLocation , File targetLocation) throws IOException {
		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {
			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	static int[] heightmap;
	public void analyzeorconvert() {
		//Doing the meat of the conversion. Assumes the ConverterMain.convertmap is done
		//HashMap<List<Integer>, List<Integer>> convertmap=ConverterMain.getInstance().convertmap;
		HashMap<Integer, Integer> convertermaps[]=new HashMap[2];

		File rootfolderorig=ConverterMain.options.getsave(0).getRootfolder();
		File rootfolderdest=ConverterMain.options.getsave(1).getRootfolder();
		File cachefile=ConverterMain.options.getsave(0).getcachefile();

		if (!rootfolderorig.exists()  || !rootfolderdest.isDirectory()){
			Logger.logln("ERROR with the provided save origin or destination");
			return ;
		}
		try {
			//File rootfolderdestnew=new File(rootfolderdest,rootfolderorig.getName()+"_convertedto_"+ConverterMain.options.getminecraftversion(1));
			if (convert){
				if (new File(rootfolderdest,"level.dat").exists()){
					int response = JOptionPane.showConfirmDialog(null, "there's already a converted save. Destroy it?", "Confirm",
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
						Logger.logln("INFO Aborting conversion");
						return ;
					}
					Logger.logln("WARNING there's already a converted save there. We will destroy it");
					rootfolderdest.delete();
				}

				try {
					copyDirectory(rootfolderorig,rootfolderdest);
				} catch (IOException e) {
					Logger.logln("ERROR copying data from the old to the new level folder");
					return ;
				}
			}else{
				if (cachefile.exists() && !MainFrame.redoanalyzesaveCheck.isSelected()){
					try {
						FileInputStream fis = new FileInputStream(cachefile);
						ObjectInputStream ois = new ObjectInputStream(fis);
						for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
							ConverterMain.options.getsave(0).listofidsinsave[ntype]= (List<Object>) ois.readObject();
							ConverterMain.options.getsave(0).nidsinsave[ntype]= (TreeMap<Object, Integer>) ois.readObject();
						}
						ois.close();
						ConverterMain.isdone.put("analyzesave", true);
						Logger.logln("Finished reading the cached analyze map for the origin save");
						//Print();
						return;
					} catch ( IOException e) {
						Logger.logln("Couldn't read the ID map  cache");
					} catch ( ClassNotFoundException e) {
						Logger.logln("Couldn't read the ID map  cache");
					}
				}
			}

			FilenameFilter directoryFilter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					File file=new File(dir,name);
					return file.isDirectory();
				}
			};

			class FileWalker {
				TreeSet<File> regionfiles=new TreeSet<File>();				

				public void walk( File root ) {
					File[] list = root.listFiles();

					for ( File f : list ) {
						if ( f.isDirectory() ) {
							walk( f );
						}
						else {
							if (f.getName().endsWith(".mca")){
								regionfiles.add(root);
							}
						}
					}
				}
			}

			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				problematicids[ntype]=new ArrayList<Object>();
				removedids[ntype]=new ArrayList<Object>();
				converteds[ntype]=new ArrayList<Object>();
				nconverteds[ntype]=new TreeMap<Object,Integer>();
				ConverterMain.options.getsave(0).listofidsinsave[ntype]=new ArrayList<Object>();
				ConverterMain.options.getsave(0).nidsinsave[ntype]=new TreeMap<Object,Integer>();
			}

			FileWalker filewalker0=new FileWalker();
			filewalker0.walk(rootfolderorig);
			List<Object> list=Arrays.asList(filewalker0.regionfiles.toArray());
			Collections.shuffle(list);
			Iterator it=list.iterator();

			//for (int nr=0;nr<regions.length;nr++){
			while (it.hasNext()){
				File oldregionfolder=(File) it.next();

				String relative = rootfolderorig.toURI().relativize(oldregionfolder.toURI()).getPath();

				File newregionfolder=new File(rootfolderdest,relative);
				if (!oldregionfolder.exists() || !oldregionfolder.isDirectory()){
					Logger.logln("ERROR with the regions directory");
					return;
				}

				File[] regionfiles=newregionfolder.listFiles();
				if (convert){
					Logger.logln("Converting "+relative+" files from "+oldregionfolder.getCanonicalPath());
					Logger.logln("                          to "+newregionfolder.getCanonicalPath());
				}else{
					Logger.logln("Analyzing "+relative+" files from "+oldregionfolder.getCanonicalPath());
				}
				for (int nf=0;nf<regionfiles.length;nf++){
					currentregionname=regionfiles[nf].getName();
					RegionFile newregion=new RegionFile(regionfiles[nf]);
					RegionFile oldregion=new RegionFile(new File(oldregionfolder,regionfiles[nf].getName()));
					Logger.logln("Treating "+relative+" file "+(nf+1)+"/"+regionfiles.length+"   "+currentregionname);
					MainFrame.updateframe();
					int blabla=1;
					for (xc=0;xc<32;xc++){
						for (zc=0;zc<32;zc++){
							if (stop){
								return;
							}
							if (oldregion.hasChunk(xc, zc)){
								//System.out.println(xc+" "+zc);
								try{
									DataInputStream dis=oldregion.getChunkDataInputStream(xc,zc);
									DataOutputStream dos=newregion.getChunkDataOutputStream(xc,zc);
									CompoundTag comptag=NbtIo.read(dis);
									dis.close();
									CompoundTag comptag2=(CompoundTag) comptag.copy();
									CompoundTag leveldata=comptag2.getCompound("Level");

									heightmap=leveldata.getIntArray("HeightMap");

									currentmode="Blocks";
									ListTag sectionsdata=leveldata.getList("Sections");
									for (int ns=0;ns<sectionsdata.size();ns++){
										//CompoundTag tag=;
										treatblocks((CompoundTag) sectionsdata.get(ns),convert);
										//tag.putByteArray("Blocks", convertblocks(tag.getByteArray("Blocks")));
										//tag.putByteArray("Add", convertblocksAdd(tag.getByteArray("Add")));
									}
									//long t0=System.currentTimeMillis();

									currentmode="Entities_Items";
									traverseTags_treatitems(leveldata.getList("Entities"),convert);


									currentmode="Entities";
									treatentities(leveldata.getList("Entities"),2,convert);

//									currentmode="TileEntities";
//									treatentities(leveldata.getList("TileEntities"),3,convert);

									currentmode="TileEntities_Items";
									traverseTags_treatitems(leveldata.getList("TileEntities"),convert);

									//long t1=System.currentTimeMillis();
									//System.out.println(" "+(t1-t0));

									if (convert){
										NbtIo.write(comptag2, dos);
										dos.flush();
										dos.close();
									}
									//									dis=oldregion.getChunkDataInputStream(xc,zc);
									//									CompoundTag comptag3=NbtIo.read(dis);
									//									System.out.println(comptag3.equals(comptag));


								}catch (IOException e) {
									Logger.logln("Unrecoverable Problem reading chunk data. Possibly corrupt level");
								}
							}
						}
					}
				}
			}

			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				Iterator it2=nconverteds[ntype].keySet().iterator();
				while(it2.hasNext()){
					Integer id=(Integer) it2.next();
					if (nconverteds[ntype].get(id)>0){
						Logger.logln("Converted "+nconverteds[ntype].get(id)+"("+
								ConverterMain.idmaps[ntype].get(0).get(id)+
								") "+ConverterMain.typenames[ntype]+" with ID :"+id+" to "+ConverterMain.options.getconvertmaps()[ntype].get(id)+
								"("+ConverterMain.idmaps[ntype].get(1).get(id)+")");
					}
				}
			}
			if (!convert) {
				Logger.logln("Finished the analysis.");
				FileOutputStream fos=new FileOutputStream(cachefile);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
					oos.writeObject(ConverterMain.options.getsave(0).listofidsinsave[ntype]);
					oos.writeObject(ConverterMain.options.getsave(0).nidsinsave[ntype]);
				}
				oos.close();
				//Print();
			}else{
				Logger.logln("Finished the conversion ! Hopefully.");
			}
			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				problematicids[ntype].clear();
				converteds[ntype].clear();
				nconverteds[ntype].clear();
			}
			ConverterMain.options.save();

		} catch (IOException e) {
			Logger.logln("ERROR accessing the save folder");
			return;
		}
		return ;
	}
	
	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Computing the convert map....");
		Logger.level=1;
		analyzeorconvert();
		Logger.level=0;
	}
}
