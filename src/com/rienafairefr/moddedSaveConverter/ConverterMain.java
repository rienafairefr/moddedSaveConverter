package com.rienafairefr.moddedSaveConverter;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.Document;

import com.rienafairefr.moddedSaveConverter.tasks.MinecraftAnalyzerConverter;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftIDnameMapper;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftSaveSelecter;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftTester;
import com.sk89q.mclauncher.Launcher;

/* 
 * JWSFileChooserDemo.java must be compiled with jnlp.jar.  For
 * example, if jnlp.jar is in a subdirectory named jars:
 * 
 *   javac -classpath .:jars/jnlp.jar JWSFileChooserDemo.java [UNIX]
 *   javac -classpath .;jars/jnlp.jar JWSFileChooserDemo.java [Microsoft Windows]
 *
 * JWSFileChooserDemo.java requires the following files when executing:
 *   images/Open16.gif
 *   images/Save16.gif
 */
@SuppressWarnings("unchecked")
public class ConverterMain {

	public static List<Future> workers=new ArrayList<Future>();
	
	public Launcher sklauncher;
	
	public static ExecutorService execservice;

	public static boolean redoidmap=false;

	public static ConverterOptions options;
	
	public static int Ntype=3; // 4 types: blocks,items,entities, tile entities
	public static String[] typenames=new String[]{"Blocks","Items","Entities"};

	//public static MinecraftApp[] minecraftapps=new MinecraftApp[3];

	public static List<TreeMap<Object, String[]>>[] idmaps=new ArrayList[ConverterMain.Ntype];
	//public static List<TreeMap<Integer, String>>[] idmaps2=new ArrayList[ConverterMain.Ntype];

	//public File[] minecrafts;
	//public String[] mineversions=new String[3];

	public static String[] availableversions=new String[]{"1.0","1.1","1.2.3","1.2.4","1.2.5","1.3.1","1.3.2","1.4.2","1.4.4","1.4.5","1.4.6","1.4.7","Unknown","Unsupported"};

	public static String magicstring="MLHOOK FOR MODDED SAVE CONVERTER";

	//public File selectedleveldat;

	public static HashMap<String,Boolean> isdone=new HashMap<String,Boolean>();

	public static ConverterMain instance;

	public Document TextFromlaunch;

	public Boolean isminelaunched[]={null,false,false};

	public ConsoleCommunication consoleComm;
	public String hookString;

	public static MainFrame mainframe;

	public static ConverterMain getInstance(){
		return instance;
	}

	public static void addworker(Runnable run){
		ConverterMain.workers.add(ConverterMain.execservice.submit(run));
		Iterator itworkers=ConverterMain.workers.iterator();
		while(itworkers.hasNext()){
			Future future=(Future) itworkers.next();
			if (future.isDone()){
				itworkers.remove();
			}
		}
	}
	
	public ConverterMain()  {
		instance=this;
		execservice=Executors.newFixedThreadPool(1);
		for (int ntype=0;ntype<Ntype;ntype++){
			idmaps[ntype]=	new ArrayList();
			idmaps[ntype].add(new TreeMap<Object, String[]>());
			idmaps[ntype].add(new TreeMap<Object, String[]>());
		}

		try {
			Class<?> skl=(Class <?>) this.getClass().getClassLoader().loadClass("com.sk89q.mclauncher.Launcher");

			Class<?>[] emptyparams=new Class<?>[0];
			Constructor<?> constructor = skl.getDeclaredConstructor(emptyparams);
			if (!constructor.isAccessible()){
				constructor.setAccessible(true);
			}
			sklauncher=(Launcher) constructor.newInstance((Object[]) emptyparams);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		// Read options
		File base = Launcher.getLauncherDataDir();
		base.mkdirs();
		File optionsFile = new File(base, "configModdedSaveConverter.xml");
		isdone.put("choosemine0", false);
		isdone.put("choosemine1", false);
		isdone.put("testmine0", false);
		isdone.put("testmine1", false);
		isdone.put("choosesave0", false);
		isdone.put("choosesave1", false);
		isdone.put("analyzesave", false);
		
		isdone.put("idmap0", false);
		isdone.put("idmap1", false);
		
		options = new ConverterOptions(optionsFile);
		options.load();
		mainframe=new MainFrame();
		mainframe.initializeGUI();

		if (options.hasloadedoptions){
			for (int nmine=0;nmine<2;nmine++){
				if (options.getminecraft(nmine)!=null){
					//if (nmine==0); Logger.logln("!! Loaded origin minecraft jar from previous launch");
					//if (nmine==1); Logger.logln("!! Loaded destination minecraft jar from previous launch");
					ConverterMain.isdone.put("choosemine"+nmine, true);
					ConverterMain.execservice.submit(new MinecraftTester(options.getminecraft(nmine)));
					MinecraftIDnameMapper idmapper=new MinecraftIDnameMapper(options.getminecraft(nmine));
					if (idmapper.cachefile.exists()){
						ConverterMain.execservice.submit(idmapper);
					}
				}
			}
			if (ConverterMain.options.getsave(0)!=null){
				ConverterMain.execservice.submit(new MinecraftAnalyzerConverter(false));
				
			}
		}
		MainFrame.updateframe2();
	}

	public static void LaunchConvert() {
		ConverterWindow convwindow=new ConverterWindow(new JFrame());
		convwindow.setVisible(true);
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ConverterMain.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}


	public JPanel createContentPane(){
		return mainframe;
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event-dispatching thread.
	 * @throws IOException 
	 */
	public static void createAndShowGUI() throws Exception {
		ConverterMain MainApp = new ConverterMain();
		//Create and set up the window.
		JFrame frame = new JFrame("modded Minecraft Save Converter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Add content to the window.
		frame.add(MainApp.createContentPane());
		frame.setSize(900,600);
		frame.setResizable(false);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}


	public static void main(String[] args) {

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//UIManager.put("swing.boldMetal", Boolean.FALSE);

		        try {
					UIManager.setLookAndFeel(
					    UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
				try {
					createAndShowGUI();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public String[] getmapping(int index,int type,int id){
		Integer[] array=new Integer[]{type,id};
		return idmaps[type].get(index).get(array);
	}

	public static void cancelworkers() {
		Iterator itworkers=ConverterMain.workers.iterator();
		boolean success=true;
		while(itworkers.hasNext()){
			Future future=(Future) itworkers.next();
			success=future.cancel(true)&success;
		}
		if (!success){
			ConverterMain.execservice.shutdownNow();
			ConverterMain.execservice=Executors.newFixedThreadPool(1);
		}
		Logger.logln("---------Cancelled---------");
	}
}


