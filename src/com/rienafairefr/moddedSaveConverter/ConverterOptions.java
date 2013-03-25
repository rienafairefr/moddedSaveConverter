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

package com.rienafairefr.moddedSaveConverter;

import static com.sk89q.mclauncher.util.XMLUtil.getInt;
import static com.sk89q.mclauncher.util.XMLUtil.getNodes;
import static com.sk89q.mclauncher.util.XMLUtil.getString;
import static com.sk89q.mclauncher.util.XMLUtil.getStringOrNull;
import static com.sk89q.mclauncher.util.XMLUtil.newXml;
import static com.sk89q.mclauncher.util.XMLUtil.parseXml;
import static com.sk89q.mclauncher.util.XMLUtil.start;
import static com.sk89q.mclauncher.util.XMLUtil.writeXml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import util.Base64;

import com.rienafairefr.moddedSaveConverter.tasks.MinecraftSaveSelecter;
import com.sk89q.mclauncher.Launcher;
import com.sk89q.mclauncher.util.SimpleNode;
import com.sk89q.mclauncher.util.Util;

/**
 * Stores options for the launcher.
 * 
 * @author sk89q
 */
public class ConverterOptions {

	public boolean hasloadedoptions=false;

	private File file;
	private String lastConfigName;

	private File lastInstallDir;
	private Map<String, String> identities = new HashMap<String, String>();
	//private SettingsList defaultSettings = new SettingsList();
	//private SettingsList settings = new SettingsList(defaultSettings);
	private String username;
	@SuppressWarnings("unchecked")
	private static TreeMap<Object,Object[]>[] convertmaps= new TreeMap[ConverterMain.Ntype];
	private MinecraftSave[] saves=new MinecraftSave[2];
	private MinecraftApp[] minecraftapps=new MinecraftApp[2];

	public MinecraftSave getsave(int b){
		if (b>=0 || b<=1){
			return saves[b];
		} else{
			return null;
		}
	}

	public File getsavefile(int b) {
		if (getsave(b)!=null){
			if (getsave(b).getRootfolder()!=null){
				return getsave(b).getRootfolder();
			}
		}
		return null;
	}

	public String getsavepath(int b) {
		if (getsavefile(b)!=null){
			return getsavefile(b).getAbsolutePath();
		}else{
			return "";
		}
	}

	public String getminecraftpath(int index) {
		if (getminecraft(index)!=null){
			if (getminecraft(index).MCdir!=null){
				return getminecraft(index).MCdir.getAbsolutePath();
			}else{
				return "";
			}
		}else{
			return "";
		}
	}

	public File getMCdir(int index) {
		if (getminecraft(index)==null){
			return null;
		}else{
			return getminecraft(index).MCdir;
		}
	}

	public MinecraftApp[] getminecrafts() {
		return minecraftapps;
	}

	public void setminecrafts(MinecraftApp[] mines) {
		if (minecraftapps.length==2){
			this.setminecrafts(minecraftapps);
		}
	}

	/**
	 * Construct the options based off of the given file.
	 * 
	 * @param file file
	 */
	public ConverterOptions(File file) {
		this.file = file;
	}

	/**
	 * Get a list of saved usernames.
	 * 
	 * @return list of usernames
	 */
	public Set<String> getSavedUsernames() {
		return identities.keySet();
	}

	/**
	 * Get a saved password.
	 * 
	 * @param username username
	 * @return password or null if no password is saved
	 */
	public String getSavedPassword(String username) {
		return identities.get(username);
	}

	/**
	 * Remember a given identity.
	 * 
	 * @param username username
	 * @param password password, possibly null to only remember the name
	 */
	public void saveIdentity(String username, String password) {
		identities.put(username, password);
	}

	/**
	 * Forget a user's password but the user him/herself.
	 * 
	 * @param username username
	 */
	public void forgetPassword(String username) {
		identities.put(username, null);
	}

	/**
	 * Forget a given identity.
	 * 
	 * @param username username
	 */
	public void forgetIdentity(String username) {
		identities.remove(username);
	}

	/**
	 * Forget all remembered identities.
	 */
	public void forgetAllIdentities() {
		identities.clear();
	}

	/**
	 * Get the last configuration name.
	 * 
	 * @return configuration name
	 */
	public String getLastConfigName() {
		return lastConfigName;
	}

	/**
	 * Set the last configuration name.
	 * 
	 * @param lastConfigName
	 */
	public void setLastConfigName(String lastConfigName) {
		this.lastConfigName = lastConfigName;
	}

	/**
	 * Get the last used username.
	 * 
	 * @return username or null
	 */
	public String getLastUsername() {
		return username;
	}

	/**
	 * Set the last used username.
	 * 
	 * @param lastUsername username
	 */
	public void setLastUsername(String lastUsername) {
		this.username = lastUsername;
	}

	/**
	 * Gets the directory of where addons were last installed from.
	 * 
	 * @return directory, or null if one isn't set
	 */
	public File getLastInstallDir() {
		return lastInstallDir;
	}

	/**
	 * Set the last directory that addons were installed from.
	 * 
	 * @param dir directory
	 */
	public void setLastInstallDir(File dir) {
		this.lastInstallDir = dir;
	}

	/**
	 * Read the configuration.
	 * 
	 * @throws IOException on I/O error
	 */
	public void read() throws IOException {
		identities = new HashMap<String, String>();

		InputStream in = null;

		try {
			Cipher cipher = Launcher.getInstance().getCipher(Cipher.DECRYPT_MODE, "passwordfile");

			in = new BufferedInputStream(new FileInputStream(file));

			Document doc = parseXml(in);
			XPath xpath = XPathFactory.newInstance().newXPath();

			username = getStringOrNull(doc, xpath.compile("/converter/username"));
			//String minedir, String jar, String vers, int index
			try {
				minecraftapps[0] = new MinecraftApp(getStringOrNull(doc, xpath.compile("/converter/mine0/jarFile")),
						getStringOrNull(doc, xpath.compile("/converter/mine0/version")),
						getInt(doc, 0, xpath.compile("/converter/mine0/index")));
				
				minecraftapps[1] = new MinecraftApp(getStringOrNull(doc, xpath.compile("/converter/mine1/jarFile")),
						getStringOrNull(doc, xpath.compile("/converter/mine1/version")),
						getInt(doc, 1, xpath.compile("/converter/mine1/index")));
				
			} catch (BadMinecraftAppException e1) {
				//Logger.log("Problem reading the option file: bad saved MinecraftApps");
			}
			for (int ns=0;ns<2;ns++){
				String save=getStringOrNull(doc, xpath.compile("/converter/save"+ns));
				if (save!=null){
//					if (save!=null && ns==0){
//						Logger.logln("!! Loaded save from previous launch");
//					}
//					if (save!=null && ns==1){
//						Logger.logln("!! Loaded destination save from previous launch");
//					}
					ConverterMain.addworker(new MinecraftSaveSelecter(new File(save),ns));
					//setsave(new MinecraftSave(, ns),ns);
				}
			}

			XPathExpression nameExpr = xpath.compile("name/text()");
			XPathExpression keyExpr = xpath.compile("key/text()");

			// Read all the <identity> elements
			for (Node node : getNodes(doc, xpath.compile("/converter/identities/identity"))) {
				String username = getString(node, nameExpr);
				String key = getString(node, keyExpr);
				String password = null;

				if (key.length() > 0) {
					try {
						byte[] decrypted = cipher.doFinal(Base64.decode(key));
						password = new String(decrypted, "UTF-8");
					} catch (IllegalBlockSizeException e) {
						e.printStackTrace();
					} catch (BadPaddingException e) {
						e.printStackTrace();
					}
				}
				identities.put(username, password);
			}
			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				String convertmapsvalue=getStringOrNull(doc, xpath.compile("/converter/convertmap"+ntype));
				if (convertmapsvalue!=null){
					convertmaps[ntype]=new TreeMap<Object,Object[]>();
					String[] lines=convertmapsvalue.split("\n");
					for (int nl=0;nl<lines.length;nl++){
						String[] tokens=lines[nl].split("=");
						if (tokens.length==3){
							Object id0,id1,typefound = -1;
							try{
								id0=Integer.parseInt(tokens[0].trim());
							}catch(NumberFormatException ex){
								id0=tokens[0].trim();
							}
							try{
								id1=Integer.parseInt(tokens[1].trim());
							}catch(NumberFormatException ex){
								id1=tokens[1].trim();
							}
							try{
								typefound=Integer.parseInt(tokens[2].trim());
							}catch(NumberFormatException ex){
							}
							convertmaps[ntype].put(id0,new Object[]{id1,typefound,null});
						}
					}
				}
			}
			hasloadedoptions=true;
		} catch (FileNotFoundException e) {
		} catch (InvalidKeyException e) {
			throw new IOException(e);
		} catch (InvalidKeySpecException e) {
			throw new IOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		} catch (NoSuchPaddingException e) {
			throw new IOException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IOException(e);
		} catch (XPathExpressionException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		} catch (BadMinecraftSaveException e) {
			e.printStackTrace();
		} finally {
			Util.close(in);
		}
	}

	/**
	 * Write to disk.
	 * 
	 * @throws IOException on I/O error
	 */
	public void write() throws IOException {
		try {
			Cipher cipher = Launcher.getInstance().getCipher(Cipher.ENCRYPT_MODE, "passwordfile");

			Document doc = newXml();
			SimpleNode root = start(doc, "converter");

			root.addNode("username").addValue(username);

			if (minecraftapps[0]!=null){
				SimpleNode mine0=root.addNode("mine0");
				mine0.addNode("index").addValue(Integer.toString(minecraftapps[0].index));
				mine0.addNode("jarFile").addValue(minecraftapps[0].jarFile);
				mine0.addNode("version").addValue(minecraftapps[0].version);
			}
			if (minecraftapps[1]!=null){
				SimpleNode mine1=root.addNode("mine1");
				mine1.addNode("index").addValue(Integer.toString(minecraftapps[1].index));
				mine1.addNode("jarFile").addValue(minecraftapps[1].jarFile);
				mine1.addNode("version").addValue(minecraftapps[1].version);
			}
			for (int ns=0;ns<2;ns++){
				if (getsave(ns)!=null){
					root.addNode("save"+ns).addValue(this.getsavepath(ns));
				}
			}

			SimpleNode identitiesNode = root.addNode("identities");
			for (Map.Entry<String, String> entry : identities.entrySet()) {
				SimpleNode identityNode = identitiesNode.addNode("identity");
				identityNode.addNode("name").addValue(entry.getKey());

				// Save encrypted password
				if (entry.getValue() != null) {
					byte[] encrypted = cipher.doFinal(entry.getValue().getBytes());
					identityNode.addNode("key").addValue(Base64.encodeToString(encrypted, false));
				}
			}
			for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
				if (convertmaps[ntype]!=null){
					String objectstring="\n";
					Iterator it=convertmaps[ntype].keySet().iterator();
					while (it.hasNext()){
						Object id0=it.next();
						Object id1=convertmaps[ntype].get(id0)[0];
						Object typefound=convertmaps[ntype].get(id0)[1];
						objectstring+=id0+"="+id1+"="+typefound+"\n";
					}
					root.addNode("convertmap"+ntype).addValue(objectstring);
				}

			}

			writeXml(doc, file);
		} catch (InvalidKeyException e) {
			throw new IOException(e);
		} catch (InvalidKeySpecException e) {
			throw new IOException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		} catch (NoSuchPaddingException e) {
			throw new IOException(e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IOException(e);
		} catch (TransformerException e) {
			throw new IOException(e);
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		} catch (BadPaddingException e) {
			throw new IOException(e);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Load the configuration.
	 * 
	 * @return true if successful
	 */
	public boolean load() {
		try {
			read();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Save the configuration.
	 * 
	 * @return true if successful
	 */
	public boolean save() {
		try {
			write();
			return true;
		} catch (IOException e) {
			Logger.logln("Failed to load options");
			return false;
		}
	}

	public void setminecraft(MinecraftApp newminecraftapp, int index) {
		if (index<=(minecraftapps.length-1)){
			minecraftapps[index]= newminecraftapp;
		}
	}

	public MinecraftApp getminecraft(int index) {
		if (index>=0 && index<=1){
			return minecraftapps[index];
		}else{
			return null;
		}
	}
	public String getminecraftversion(int index) {
		if (getminecraft(index)==null){
			return "Unknown";
		}else{
			return getminecraft(index).version;
		}
	}
	public void setminecraftversion(int index,String vers) {
		if (getminecraft(index)==null){
			return;
		}else{
			getminecraft(index).version=vers;
		}
	}
	
	public File getsavedir(int index){
		if (getminecraft(index)==null) return null;
		return new File(ConverterMain.options.getminecraftjar(1).getParentFile().getParentFile(),"saves");
	}
	
	public File getminecraftjar(int index) {
		if (getminecraft(index)==null) return null;
		return getminecraft(index).MCjar;
	}
	public String getminecraftjarpath(int index) {
		if (getminecraft(index)==null) return "";
		return (getminecraft(index).MCjar).getAbsolutePath();
	}
	public boolean hasconvertmaps() {
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			if (getconvertmap(ntype)==null){
				return false;
			}
		}
		return true;
	}
	public TreeMap<Object, Object[]>[] getconvertmaps() {
		return convertmaps;
	}
	public TreeMap<Object, Object[]> getconvertmap(int index) {
		if (index>=0 && index<=ConverterMain.Ntype-1){
			return convertmaps[index];
		}else{
			return null;
		}
	}
	public void setconvertmap(TreeMap<Object, Object[]> input,int index) {
		if (index>=0 && index<=ConverterMain.Ntype-1){
			convertmaps[index]=input;
		}
	}
	public void setconvertmaps(TreeMap<Object, Object[]>[] convertmaps2) {
		convertmaps=convertmaps2;
	}

	public void setsave(MinecraftSave minecraftSave,int type) {
		this.saves[type]=minecraftSave;
	}
}
