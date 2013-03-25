package com.rienafairefr.moddedSaveConverter.tasks;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MainFrame;
import com.rienafairefr.moddedSaveConverter.StringDists;

public class MinecraftConvertMapper extends CustomTask{
	Map[] hash0=new Map[ConverterMain.Ntype];
	Map[] hash1=new Map[ConverterMain.Ntype];

	static int typefound;
	static Object idfound;

	public void computeconvertmap(){
		if (!ConverterMain.options.hasconvertmaps() || MainFrame.redoconvertmapCheck.isSelected()){

			//if (true){
			//List<TypeIDName[]> typeidnames=new ArrayList<TypeIDName[]>();
			//List<TypeIDName> typeidname2=new ArrayList<TypeIDName>();

			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				hash0[ntype]=ConverterMain.idmaps[ntype].get(0);
				hash1[ntype]=ConverterMain.idmaps[ntype].get(1);
			}

			@SuppressWarnings("unchecked")
			TreeMap<Object,Object[]>[] convertmaps=new TreeMap[ConverterMain.Ntype];
			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				convertmaps[ntype]=new TreeMap<Object,Object[]>();
			}

			//			String classnameitemblock=ConverterMain.idmaps[1].get(1).get(1);
			//			String[] tokens0=classnameitemblock.split(" ");
			//			classnameitemblock=tokens0[tokens0.length-1];

			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				Set entries0= hash0[ntype].entrySet();
				Iterator it0=entries0.iterator();
				String[] strings0=new String[2];
				String[] strings1=new String[2];

				while (it0.hasNext()){
					if (stop){
						return;
					}
					Entry entry= (Entry) it0.next();
					Object id0=entry.getKey();
					strings0=(String[]) entry.getValue();
					String string0=strings0[0]+'.'+strings0[1];
					typefound=-1;
					idfound=id0;

					// -1 NO CORRESPONDENCE
					// 0 Same id same name
					// 1 Same id close name with classname length>3
					// 2 Same id close name 
					// 3 Same id but far name
					if (ntype==0 || ntype==1){
						if (isvanillaexception.contains(id0)){
							typefound=0;
						}else{
							if (hash1[ntype].containsKey(id0)){
								strings1=(String[]) hash1[ntype].get(id0);
								String string1=strings1[0]+'.'+strings1[1];
								if (strings0[0].equals(strings1[0])){
									typefound=0;
								}else{
									//								if ( strings0[0].length()<=3 && strings0[1].length()<=3){
									//									typefound=1;
									//								}else{
									float dist=Math.min(StringDists.tokenizedcompare(string0,string1,"[.| ]"),
											StringDists.tokenizedcompare(string1,string0,"[.| ]"));
									if (dist<=0.4){
										typefound=3;
									}else{
										findclosematch(strings0,ntype);
									}
									//}
								}
							}else{
								findclosematch(strings0,ntype);
							}
						}
					}else{
						if (hash1[ntype].containsKey(id0)){
							idfound=id0;
							typefound=0;
						}else{
							typefound=-1;
						}
					}
					if (idfound!=null){
						convertmaps[ntype].put(id0, new Object[]{idfound,typefound});
					}

					//System.out.println("class"+distclasss.values());
					//System.out.println("blocks"+distblocks.values());
				}
			}
			convertmaps[0].put(0,new Object[]{0,0});
			ConverterMain.options.setconvertmaps(convertmaps);
			ConverterMain.options.save();
		}
	}

	
	public List<Object> isvanillaexception=Arrays.asList(new Object[]{97,34,36,119,0});

	public void findclosematch(String[] strings0,int ntype){ 
		Iterator it = hash1[ntype].entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			//System.out.println(pairs.getKey() + " = " + pairs.getValue());

			Object id1=pairs.getKey();
			String[] strings1=(String[]) pairs.getValue();
			String string1=strings1[0]+'.'+strings1[1];
			String string0=strings0[0]+'.'+strings0[1];
			if (!isvanillaexception.contains(id1)){
				if (strings0[0].equals(strings1[0])){
					typefound=0;
					idfound=id1;
					return;
				}else{
					float dist=Math.min(StringDists.tokenizedcompare(string0,string1,"[.| ]"),
							StringDists.tokenizedcompare(string1,string0,"[.| ]"));
					if (dist<=0.4){
						typefound=3;
						idfound=id1;
						return;
					}
//					float dist1=Math.min(StringDists.tokenizedcompare(strings0[0],strings1[0],"[.| ]"),
//							StringDists.tokenizedcompare(strings1[0],strings0[0],"[.| ]"));
//					float dist2=Math.min(StringDists.tokenizedcompare(strings0[1],strings1[1],"[.| ]"),
//							StringDists.tokenizedcompare(strings1[1],strings0[1],"[.| ]"));
//					if ( dist1<0.3 || dist2<0.3){
//						typefound=3;
//						idfound=id1;
//						return;
//					}
				}
			}
		}
		return;
	}

	@Override
	protected void execute() throws ExecutionException {
		Logger.logln("Computing the convert map....");
		Logger.level=1;
		computeconvertmap();
		Logger.level=0;
	}
}
