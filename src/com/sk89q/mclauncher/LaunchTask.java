/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010, 2011 Albert Pham <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.mclauncher;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.net.ssl.SSLHandshakeException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import com.rienafairefr.moddedSaveConverter.ConsoleCommunication;
import com.rienafairefr.moddedSaveConverter.ConverterMain;
import com.rienafairefr.moddedSaveConverter.Logger;
import com.rienafairefr.moddedSaveConverter.MinecraftApp;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftIDnameMapper;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftLauncher;
import com.rienafairefr.moddedSaveConverter.RienaUtil;
import com.sk89q.mclauncher.LoginSession.LoginException;
import com.sk89q.mclauncher.LoginSession.OutdatedLauncherException;
import com.sk89q.mclauncher.addons.Addon;
import com.sk89q.mclauncher.config.Configuration;
import com.sk89q.mclauncher.config.Def;
import com.sk89q.mclauncher.config.LauncherOptions;
import com.sk89q.mclauncher.launch.GameLauncher;
import com.sk89q.mclauncher.util.SettingsList;
import com.sk89q.mclauncher.util.UIUtil;
import com.sk89q.mclauncher.util.Util;

/**
 * Used for launching the game.
 * 
 * @author sk89q
 */
public class LaunchTask extends Task {

	//private static final Logger logger = Logger.getLogger(LaunchTask.class.getCanonicalName());

	//private volatile boolean running = true;

	public MinecraftApp minecraftapp;
	private JFrame frame;
	private String username;
	private String password;
	private String activeJar;

	private LoginSession session;
	private Configuration configuration;
	private File rootDir;
	private boolean playOffline = false;
	private boolean demo = false;
	private boolean allowOfflineName = false;

	private boolean showConsole = false;
	private String autoConnect;

	/**
	 * Construct the launch task.
	 * 
	 * @param frame starting frame
	 * @param configuration workspace
	 * @param username username
	 * @param password password
	 * @param jar jar name
	 */
	public LaunchTask(JFrame frame, Configuration configuration,
			String username, String password, String jar) {
		this.frame = frame;
		this.configuration = configuration;
		this.username = username;
		this.password = password;
		this.activeJar = jar;
	}

	/**
	 * Set play online state.
	 * 
	 * @param playOffline true to play offline
	 */
	public void setPlayOffline(boolean playOffline) {
		this.playOffline = playOffline;
	}


	/**
	 * Set to show the Java console.
	 * 
	 * @param showConsole true to show console
	 */
	public void setShowConsole(boolean showConsole) {
		this.showConsole = showConsole;
	}

	/**
	 * Run Minecraft in demo mode.
	 * 
	 * @param demo true for demo mode, false for normal mode if a premium account.
	 */
	public void setDemo(boolean demo) {
		this.demo = demo;
	}

	/**
	 * Set the ability to use the player's username while playing offline.
	 * 
	 * @param allow address (addr:port, addr) or null
	 */
	public void setAllowOfflineName(boolean allow) {
		this.allowOfflineName = allow;
	}

	/**
	 * Execute the launch task.
	 */
	@Override
	public void execute() throws ExecutionException {
		rootDir = configuration.getMinecraftDir();
		rootDir.mkdirs();

		session = new LoginSession(username);

		if (!playOffline) {
			login();
		}

		launch();
	}

	/**
	 * Try launching.
	 * 
	 * @throws ExecutionException
	 *             on error while executing
	 */
	public void launch() throws ExecutionException {
		fireTitleChange("Launching...");
		fireStatusChange("Launching Minecraft...");
		fireValueChange(-1);

		LauncherOptions options = Launcher.getInstance().getOptions();
		SettingsList settings = new SettingsList(
				options.getSettings(), configuration.getSettings());

		// Find launcher path
		String launcherPath;
		try {
			launcherPath = Launcher.class.getProtectionDomain()
			.getCodeSource().getLocation().toURI().getPath();
		} catch (URISyntaxException e) {
			throw new ExecutionException("The path to the launcher could not be discovered.", e);
		}

		// Read some settings
		String username = !allowOfflineName && playOffline ? "Player" : this.username;
		String runtimePath = Util.nullEmpty(settings.get(Def.JAVA_RUNTIME));
		String wrapperPath = Util.nullEmpty(settings.get(Def.JAVA_WRAPPER_PROGRAM));
		int minMem = settings.getInt(Def.JAVA_MIN_MEM, 128);
		int maxMem = settings.getInt(Def.JAVA_MAX_MEM, 1024);
		String[] extraArgs = settings.get(Def.JAVA_ARGS, "").split(" +");
		String extraClasspath = Util.nullEmpty(settings.get(Def.JAVA_CLASSPATH));
		final boolean showConsole = (this.showConsole || settings.getBool(Def.JAVA_CONSOLE, false));
		final boolean relaunch = settings.getBool(Def.LAUNCHER_REOPEN, false);
		final boolean coloredConsole = settings.getBool(Def.COLORED_CONSOLE, true);
		final boolean consoleKillsProcess = settings.getBool(Def.CONSOLE_KILLS_PROCESS, true);
		String validatedRuntimePath = "";

		// Figure out what to use for the Java runtime
		if (runtimePath != null) {
			File test = new File(runtimePath);
			// Try the parent directory
			if (!test.exists()) {
				throw new ExecutionException("The configured Java runtime path '" + runtimePath + "' doesn't exist.");
			} else if (test.isFile()) {
				test = test.getParentFile();
			}
			File test2 = new File(test, "bin");
			if (test2.isDirectory()) {
				test = test2;
			}
			validatedRuntimePath = test.getAbsolutePath() + File.separator;
		}

		// Set some things straight
		String actualJar = activeJar != null ? activeJar : "minecraft.jar";
		File actualWorkingDirectory = configuration.getBaseDir();

		if (!new File(configuration.getMinecraftDir(), "bin/" + actualJar).exists()) {
			throw new ExecutionException("The game is not installed.");
		}


		// Get the MLhook addon
		Addon addonVersion;
		List<Addon> addons=new ArrayList<Addon>();
		File addonversionfile = null;
		try {
			File tempaddondirectory=RienaUtil.createTempDirectory();
			addonversionfile=new File(tempaddondirectory,"mod_MLhook.zip");

			URL url = Launcher.class.getResource("/resources/MLhookclasses/"+minecraftapp.version+"/mod_MLhook.zip");
			FileOutputStream output = new FileOutputStream(addonversionfile);
			InputStream input = url.openStream();
			byte [] buffer = new byte[4096];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
			output.close();
			input.close();
		} catch (IOException e2) {
		}

		try {
			addonVersion = new Addon("0", minecraftapp.version, "mod_MLhook", addonversionfile, new URL("http://www.rienafairefr.com"));
			addons.add(addonVersion);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		ArrayList<String> params = new ArrayList<String>();

		// Start with a wrapper
		if (wrapperPath != null) {
			params.add(wrapperPath);
		}

		// Choose the java version that we want
		params.add(validatedRuntimePath + "java");

		// Add memory options
		if (minMem > 0) {
			params.add("-Xms" + minMem + "M");
		}
		if (maxMem > 0) {
			params.add("-Xmx" + maxMem + "M");
		}

		// Add some Java flags
		params.add("-Dsun.java2d.noddraw=true");
		params.add("-Dsun.java2d.d3d=false");
		params.add("-Dsun.java2d.opengl=false");
		params.add("-Dsun.java2d.pmoffscreen=false");
		if (settings.getBool(Def.LWJGL_DEBUG, false)) {
			params.add("-Dorg.lwjgl.util.Debug=true");
		}

		// Add extra arguments
		for (String arg : extraArgs) {
			arg = arg.trim();
			if (arg.length() > 0) {
				params.add(arg);
			}
		}

		// Add classpath
		params.add("-classpath");
		params.add(launcherPath + (extraClasspath != null ? File.pathSeparator + extraClasspath : ""));

		// Class to run
		params.add(GameLauncher.class.getCanonicalName());

		// Child launcher flags
		params.add("-width");
		params.add(String.valueOf(settings.getInt(Def.WINDOW_WIDTH, 300)));
		params.add("-height");
		params.add(String.valueOf(settings.getInt(Def.WINDOW_HEIGHT, 300)));

		// Child launcher arguments
		params.add(actualWorkingDirectory.getAbsolutePath());
		params.add(actualJar);

		ProcessBuilder procBuilder = new ProcessBuilder(params);

		// Have to do this for Windows here; can't do it in the launcher spawn
		procBuilder.environment().put("APPDATA", actualWorkingDirectory.getAbsolutePath());

		procBuilder.redirectErrorStream(true);



		// Start the baby!
		final Process proc;
		try {
			proc = procBuilder.start();
		} catch (IOException e) {
			throw new ExecutionException("The game could not be started: " + e.getMessage(), e);
		}

		//        ConsoleCommunication consolecomm=new ConsoleCommunication(proc, nminecraft);
		//        ConsoleOutputStream cout=consolecomm.getOutputStream();
		//        BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		//        String in;
		//        try {
		//			while((in = input.readLine()) != null) {
		//				cout.write(in+"\n");
		//			}
		//		} catch (IOException e2) {
		//			e2.printStackTrace();
		//		}

		//        try {
		//			int exitVal = proc.waitFor();
		//		} catch (InterruptedException e1) {
		//			e1.printStackTrace();
		//		}

		// Create console
		if (showConsole) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					ConverterMain.getInstance().consoleComm=new ConsoleCommunication(proc, minecraftapp.index);
					ConverterMain.getInstance().consoleComm.consume(proc.getInputStream());
					ConverterMain.getInstance().consoleComm.consume(proc.getErrorStream());
				}
			});
		}

		PrintStream out = new PrintStream(new BufferedOutputStream(proc.getOutputStream()));

		// Add parameters
		out.println("@username=" + username);
		out.println("@mppass=" + username);
		out.println("@sessionid=" + (session.isValid() ? session.getSessionId() : ""));
		if (demo) {
			out.println("@demo=true");
		}
		if (settings.getBool(Def.WINDOW_FULLSCREEN, false)) {
			out.println("@fullscreen=true");
		}
		if (autoConnect != null) {
			String[] parts = autoConnect.split(":", 2);
			if (parts.length == 1) {
				out.println("@server=" + parts[0]);
				out.println("@port=25565");
			} else {
				out.println("@server=" + parts[0]);
				out.println("@port=" + parts[1]);
			}
		}

		// Add enabled addons
		for (Addon addon : addons) {
			out.println("!" + addon.getFile().getAbsolutePath());
		}

		out.close(); // Here it starts

		try {
			proc.waitFor();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		Document document=ConverterMain.getInstance().consoleComm.getDocument();
		@SuppressWarnings("unchecked")
		TreeMap<Object, String[]>[] idmapbi=new TreeMap[ConverterMain.Ntype];
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			idmapbi[ntype]=new TreeMap<Object, String[]>();
		}
		try {
			String stringdocument=document.getText(0, document.getLength());
			MinecraftIDnameMapper.stringdocument=stringdocument;
			String[] tokens = stringdocument.split("\\r\\n");  

			Logger.logln(tokens.length+" lines founds in the Console");
			for (int nlines=0;nlines<tokens.length;nlines++){
				String line=tokens[nlines];
				//System.out.println("parse Console: "+line);
				if (line.indexOf(ConverterMain.magicstring)!=-1){
					line=line.substring(line.indexOf(ConverterMain.magicstring)+ConverterMain.magicstring.length()+2,line.length());
					if (!line.startsWith("BEGIN LIST") && !line.startsWith("END LIST")){
						String[] tokens2 = line.split("<>");

						if (tokens2.length>=3){
							//TODO
							Object id=null;
							String name="";
							String classname="";
							int type=0;
							try{
								type=Integer.parseInt(tokens2[0]);
							}catch (NumberFormatException ex){
							}
							try{
								id=Integer.parseInt(tokens2[1]);
							}catch (NumberFormatException ex){
								id=tokens2[1];
							}
							name=tokens2[2];
							if (tokens2.length==4){
								classname=tokens2[3];
							}
							if (type<=(idmapbi.length-1)){
								idmapbi[type].put(id,new String[]{name,classname});
								Logger.logln("              :"+type+" "+id+" "+name+" "+classname);	
							}
						}
					}
				}
			}
			idmapbi[0].put(0, new String[]{"Air",""});
			try {
				synchronized(MinecraftLauncher.idmap){
					for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
						MinecraftLauncher.idmap[ntype]=idmapbi[ntype];
					}
					ConverterMain.idmaps.notify();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}


		if (showConsole || relaunch) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.dispose();
				}
			});

			if (relaunch) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							if (!showConsole) {
								Util.consumeBlindly(proc.getInputStream());
								Util.consumeBlindly(proc.getErrorStream());
							}
							proc.waitFor();
						} catch (InterruptedException e) {
						}
						//Launcher.startLauncherFrame();
					}
				}).start();
			}
		} else {
			System.exit(0);
		}
	}

	/**
	 * Try logging in.
	 * 
	 * @throws ExecutionException on error while executing
	 */
	public void login() throws ExecutionException {
		fireTitleChange("Logging in...");
		fireStatusChange("Connecting to " + session.getLoginURL().getHost() + "...");
		fireValueChange(-1);

		try {
			if (!session.login(password)) {
				throw new ExecutionException("You've entered an invalid username/password combination.");
			}

			username = session.getUsername();
		} catch (SSLHandshakeException e) {
			throw new ExecutionException("Verification of the identity of the authentication server failed. You may need to update the launcher, or someone has attmpted to steal your credentials.");
		} catch (OutdatedLauncherException e) {
			throw new ExecutionException("Your launcher has to be updated.");
		} catch (LoginException e) {
			if (e.getMessage().equals("User not premium")) {
				if (!demo) {
					UIUtil.showError(frame, "Not Premium", "You aren't logging in to a premium account.\nMinecraft will run in demo mode.");
				}
				demo = true;
			} else {
				throw new ExecutionException("A login error has occurred: " + e.getMessage());
			}
		} catch (final IOException e) {
			e.printStackTrace();
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						String message;
						if (e instanceof UnknownHostException) {
							message = "host is unresolved: " + e.getMessage();
						} else {
							message = e.getMessage();
						}

						if (JOptionPane.showConfirmDialog(getComponent(), 
								"The Minecraft login server is unreachable (" + message + "). " +
								"Would you like to play offline?",
								"Login error", JOptionPane.YES_NO_OPTION) == 0) {
							playOffline = true;
						}
					}
				});

				if (!playOffline) {
					throw new CancelledExecutionException();
				}
			} catch (InterruptedException e1) {
			} catch (InvocationTargetException e1) {
			}
		} finally {
			password = null;
		}
	}

	/**
	 * Request a cancel.
	 */
	@Override
	public Boolean cancel() {
		if (JOptionPane.showConfirmDialog(getComponent(), "Are you sure you want to cancel?",
				"Cancel", JOptionPane.YES_NO_OPTION) != 0) {
			return false;
		}

		return true;
	}
}
