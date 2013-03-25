package net.minecraft.src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.src.BaseMod;
import net.minecraft.src.Block;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Item;
import net.minecraft.src.ModLoader;


public class mod_MLhook extends BaseMod{
	List modlist;

	@Override
	public String getVersion() {
		return "0.0.0.1";
	}

	@Override
	public void load() {
		ModLoader.setInGameHook(this, true, true);
		ModLoader.setInGUIHook(this, true, true);
	}

	@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		boolean return1=super.onTickInGame(f, minecraft);
		this.generateConsoleOutput();
		return true;
	}

	@Override
	public boolean onTickInGUI(float f, Minecraft minecraft, GuiScreen var3) {
		boolean return1=super.onTickInGUI(f, minecraft, var3);
		this.generateConsoleOutput();
		return true;
	}

	public static String magicstring="MLHOOK FOR MODDED SAVE CONVERTER";

	public void generateConsoleOutput(){

		//String filename=".\\idnamemapping.txt";

		//try {
		//PrintWriter out;
		//File outfile=new File(filename);
		//out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
		System.out.println(magicstring + "BEGIN LIST");
		System.out.println(magicstring+"Blocks");
		int type=0;
		Block[] blocklist=Block.blocksList;
		for (int n=0;n<blocklist.length;n++){
			if (blocklist[n]!=null){
				String output=magicstring+"<>"+type+"<>"+blocklist[n].blockID;
				if (blocklist[n].getBlockName()!=null){
					output+=" "+blocklist[n].getBlockName();
				}
				output+=" "+blocklist[n].getClass().getCanonicalName();
				System.out.println( output );
			}
		}
		System.out.println(magicstring+"Items");
		type=1;
		Item[] itemlist=Item.itemsList;
		for (int n=0;n<itemlist.length;n++){
			if (itemlist[n]!=null){
				String output=magicstring+"<>"+type+"<>"+itemlist[n].shiftedIndex;
				if (itemlist[n].getItemName()!=null){
					output+="<>"+itemlist[n].getItemName();
				}
				output+="<>"+itemlist[n].getClass().getCanonicalName();
				System.out.println(output);
			}
		}
		
		
		System.out.println(magicstring+"Entities");
		type=2;
		Class <?> classentitylist=EntityList.class;
		Field[] entitylistfields=classentitylist.getDeclaredFields();
		TreeMap<Integer,String[]> idmap=new TreeMap<Integer,String[]>();
		for (int nf=0;nf<entitylistfields.length;nf++){
			Field f=entitylistfields[nf];
			if (Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				try {
					//System.out.println(f.getName() + ": " + f.get(null));
					Object fieldvalue=f.get(null);
					if (fieldvalue instanceof HashMap){
						fieldvalue=(HashMap) fieldvalue;
						Class<?> keyclass=((HashMap) fieldvalue).keySet().iterator().next().getClass();
						Class<?> valclass=((HashMap) fieldvalue).values().iterator().next().getClass();
						//stringToClassMapping
						//classToStringMapping
						
						//classToIDMapping
						
						HashMap hash=(HashMap) f.get(null);
						if (keyclass.equals(Integer.class)&&valclass.equals(Class.class)){
							//IDtoClassMapping
							Iterator it=hash.entrySet().iterator();
							while (it.hasNext()){
						        Map.Entry pairs = (Map.Entry)it.next();
						        Integer id=(Integer) pairs.getKey();
						        String classname=((Class) pairs.getValue()).getCanonicalName();
						        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
						        if (idmap.containsKey(id)){
						        	String[] value=idmap.get(id);
						        	idmap.put(id,new String[]{value[0],classname});
						        }else{
						        	idmap.put(id,new String[]{null,classname});
						        }
							}
						}
						if (keyclass.equals(String.class)&&valclass.equals(Integer.class)){
							//StringtoIDMapping
							Iterator it=hash.entrySet().iterator();
							while (it.hasNext()){
								Map.Entry pairs = (Map.Entry)it.next();
								String name=(String) pairs.getKey();
								Integer id=(Integer) pairs.getValue();
								//System.out.println(pairs.getKey() + " = " + pairs.getValue());
						        if (idmap.containsKey(id)){
						        	String[] value=idmap.get(id);
						        	idmap.put(id,new String[]{name,value[1]});
						        }else{
						        	idmap.put(id,new String[]{name,null});
						        }
							}
						}
					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		Iterator it=idmap.keySet().iterator();
		while(it.hasNext()){
			Integer id=(Integer) it.next();
			System.out.println(magicstring+"<>"+type+"<>"+id+"<>"+idmap.get(id)[0]+"<>"+idmap.get(id)[1]);
		}
		
		System.out.println(magicstring+"Tile Entities");
		type=3;
		Class <?> classTE=TileEntity.class;
		Field[] TEfields=classTE.getDeclaredFields();
		TreeMap<String,String> idmapTE=new TreeMap<String,String>();
		for (int nf=0;nf<TEfields.length;nf++){
			Field f=TEfields[nf];
			if (Modifier.isStatic(f.getModifiers())) {
				f.setAccessible(true);
				try {
					//System.out.println(f.getName() + ": " + f.get(null));
					Object fieldvalue=f.get(null);
					if (fieldvalue instanceof HashMap){
						fieldvalue=(HashMap) fieldvalue;
						Class<?> keyclass=((HashMap) fieldvalue).keySet().iterator().next().getClass();
						Class<?> valclass=((HashMap) fieldvalue).values().iterator().next().getClass();
						//stringToClassMapping
						//classToStringMapping
						
						//classToIDMapping
						
						HashMap hash=(HashMap) f.get(null);
						if (keyclass.equals(String.class)&&valclass.equals(Class.class)){
							//nameToClassMap
							Iterator it1=hash.entrySet().iterator();
							while (it1.hasNext()){
						        Map.Entry pairs = (Map.Entry)it1.next();
						        String id=(String) pairs.getKey();
						        String classname=((Class) pairs.getValue()).getCanonicalName();
						        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
						        if (idmapTE.containsKey(id)){
						        	idmapTE.put(id,classname);
						        }else{
						        	idmapTE.put(id,classname);
						        }
							}
						}
					}

				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		Iterator it1=idmapTE.keySet().iterator();
		while(it1.hasNext()){
			String id=(String) it1.next();
			System.out.println(magicstring+"<>"+type+"<>"+id+"<>"+idmapTE.get(id));
		}
		
		System.out.println(magicstring + "END LIST");
		System.exit(0);
		//out.close();
		// } catch (IOException e) {
		// System.out.println("Error finding where to put the ID mapping text file");
		// System.out.println(filename);
		// e.printStackTrace();
		// }
	}
}
