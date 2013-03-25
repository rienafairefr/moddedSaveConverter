package com.rienafairefr.moddedSaveConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import com.rienafairefr.moddedSaveConverter.tasks.MinecraftAnalyzerConverter;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftIDnameMapper;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftLoader;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftSaveSelecter;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftTester;
@SuppressWarnings("unchecked")
public class MainFrame extends JPanel{

	ActionListener actionlistener=new buttonsActionListener();
	private static final long serialVersionUID = -5727402712831109479L;
	public static DefaultComboBoxModel[] models = new DefaultComboBoxModel[]{new DefaultComboBoxModel(ConverterMain.availableversions),new DefaultComboBoxModel(ConverterMain.availableversions)};
	static JComboBox[] mineversions=new JComboBox[]{new JComboBox(models[0]),new JComboBox(models[1])};

	static boolean initialized=false;

	static JButton[] choosesavebuttons=new JButton[2];
	static JButton analyzesavebutton;
	public static JCheckBox redoanalyzesaveCheck;
	JButton[] chooseminebuttons=new JButton[2];
	static JButton[] TestButtons=new JButton[2];
	static JButton ConvertButton;
	static JButton[] idmapmines=new JButton[2];

	public static JCheckBox[] relaunchidmaps=new JCheckBox[2];

	public static JTextField userText;
	public static JTextField passText;
	static JLabel userLabel;
	static JLabel passLabel;
	public static JCheckBox playOfflineCheck;
	public static JCheckBox savePassCheck;
	public static JCheckBox redoconvertmapCheck;
	static JTextArea log;
	JFileChooser JF=new JFileChooser();

	public static JPanel[] testmineOKlights=new JPanel[2];
	public static JPanel[] idmapmineOKlights=new JPanel[2];
	public static JPanel[] versionOKlights=new JPanel[2];

	static JTextField originleveldattextfield;
	static JTextField destinationsavefoldertextfield;
	static JTextField origintextfield;
	static JTextField destinationtextfield;

	public MainFrame(){
		super(new BorderLayout());
	}

	public void initializeGUI(){

		choosesavebuttons[0] = new JButton("Choose save");
		choosesavebuttons[0].addActionListener(actionlistener);

		analyzesavebutton = new JButton("Analyze");
		analyzesavebutton.addActionListener(actionlistener);

		choosesavebuttons[1] = new JButton("Choose destination save");
		choosesavebuttons[1].addActionListener(actionlistener);

		chooseminebuttons[0] = new JButton("Choose origin jar");
		chooseminebuttons[0].addActionListener(actionlistener);

		chooseminebuttons[1] = new JButton("Choose destination jar");
		chooseminebuttons[1].addActionListener(actionlistener);

		originleveldattextfield=new JTextField(35);
		destinationsavefoldertextfield=new JTextField(35);
		origintextfield=new JTextField(35);
		destinationtextfield=new JTextField(35);

		JButton okbutton0=new JButton();
		JButton okbutton1=new JButton();
		JButton okbutton2=new JButton();
		JButton okbutton3=new JButton();
		okbutton0.setText("OK");
		okbutton1.setText("OK");
		okbutton2.setText("OK");
		okbutton3.setText("OK");
		okbutton0.addActionListener(new textfieldListener(0));
		okbutton1.addActionListener(new textfieldListener(1));
		okbutton2.addActionListener(new textfieldListener(2));
		okbutton3.addActionListener(new textfieldListener(3));

		//Create the log first, because the action listeners
		//need to refer to it.
		log = new JTextArea(22,50);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		log.setWrapStyleWord(true);
		log.setLineWrap(true);

		JScrollPane logScrollPane = new JScrollPane(log);

		JPanel rightside=new JPanel();
		rightside.setLayout(new BoxLayout(rightside,BoxLayout.Y_AXIS));

		JPanel textfields=new JPanel();
		textfields.setLayout(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.fill=GridBagConstraints.HORIZONTAL;
		//c.ipadx=8;
		//c.ipady=8;
		c.insets= new Insets(2, 2, 2, 2);

		c.gridy=0;
		c.weightx=0;
		textfields.add(new JLabel(".jar"),c);
		c.weightx=1;
		textfields.add(origintextfield,c);
		textfields.add(okbutton0,c);
		origintextfield.addActionListener(new textfieldListener(0));
		textfields.add(chooseminebuttons[0],c);
		textfields.add(chooseminebuttons[0],c);

		c.gridy=1;
		c.weightx=0;
		textfields.add(new JLabel(".jar"),c);
		c.weightx=1;
		textfields.add(destinationtextfield,c);
		textfields.add(okbutton1,c);
		destinationtextfield.addActionListener(new textfieldListener(1));
		textfields.add(chooseminebuttons[1],c);

		c.gridy=2;
		c.weightx=0;
		textfields.add(new JLabel("folder"),c);
		c.weightx=1;
		textfields.add(originleveldattextfield,c);
		textfields.add(okbutton2,c);
		originleveldattextfield.addActionListener(new textfieldListener(2));
		textfields.add(choosesavebuttons[0],c);

		c.gridy=3;
		c.weightx=0;
		textfields.add(new JLabel("folder"),c);
		c.weightx=1;
		textfields.add(destinationsavefoldertextfield,c);
		textfields.add(okbutton3,c);
		destinationsavefoldertextfield.addActionListener(new textfieldListener(3));
		textfields.add(choosesavebuttons[1],c);

		rightside.add(logScrollPane);

		JButton buttonclear=new JButton("clear log");
		buttonclear.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				log.setText("");
			}
		});

		JButton buttoncancel=new JButton("Cancel running operation");
		buttoncancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ConverterMain.cancelworkers();
			}
		});
		JPanel panelunderlog=new JPanel();
		panelunderlog.setLayout(new FlowLayout());
		panelunderlog.add(buttoncancel);
		panelunderlog.add(buttonclear);
		rightside.add(panelunderlog);
		rightside.add(textfields);


		logScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		//Buttons
		idmapmines[0] = new JButton("IDmap ");
		idmapmines[0].addActionListener(actionlistener);

		idmapmines[1] = new JButton("IDmap ");
		idmapmines[1].addActionListener(actionlistener);


		TestButtons[0] = new JButton("Test");
		TestButtons[0].addActionListener(actionlistener);

		TestButtons[1] = new JButton("Test");
		TestButtons[1].addActionListener(actionlistener);

		ConvertButton = new JButton("Convert...");
		ConvertButton.addActionListener(actionlistener);

		redoconvertmapCheck= new JCheckBox("Redo Convert Map");
		redoconvertmapCheck.setSelected(false);

		redoanalyzesaveCheck= new JCheckBox("Redo");
		redoanalyzesaveCheck.setSelected(false);

		ConverterMain.getInstance();
		mineversions[0].setSelectedItem(ConverterMain.options.getminecraftversion(0));
		mineversions[1].setSelectedItem(ConverterMain.options.getminecraftversion(1));

		userText = new JTextField(15);
		userText.setEditable(true);
		String username=ConverterMain.options.getLastUsername();
		userText.setText(username);
		passText = new JPasswordField(15);
		userText.setSize(50, 10);

		passText.setText(ConverterMain.options.getSavedPassword(username));

		playOfflineCheck= new JCheckBox("Offline mode");
		playOfflineCheck.setSelected(true);

		savePassCheck= new JCheckBox("Save Password");
		savePassCheck.setSelected(true);

		relaunchidmaps[0]= new JCheckBox("Relaunch");

		relaunchidmaps[1]= new JCheckBox("Relaunch");

		//For layout purposes, put the buttons in a separate panel

		GridBagLayout buttongridbag=new GridBagLayout(); 
		GridBagConstraints cbutton = new GridBagConstraints();
		JPanel buttonPanel = new JPanel(buttongridbag);

		//buttonPanel.setPreferredSize(new Dimension(100));

		//cbutton.fill=GridBagConstraints.HORIZONTAL;
		//cbutton.gridx=0;
		//cbutton.gridy=0;
		//buttonPanel.add(choosesavebutton,cbutton);

		cbutton.gridy=1;

		//		cbutton.gridx=0;
		//		cbutton.gridy=1;
		//		buttonPanel.add(chooseminebuttons[0],cbutton);
		//
		//		cbutton.gridx=1;
		//		cbutton.gridy=1;
		//		buttonPanel.add(chooseminebuttons[1],cbutton);
		JLabel[] labels=new JLabel[]{new JLabel("Origin"),new JLabel("Destination")};
		int n0=cbutton.gridy;
		for (int n=0;n<2;n++){
			cbutton.gridy=n0;
			cbutton.gridx=2*n+1;
			cbutton.gridy++;
			buttonPanel.add(labels[n],cbutton);

			cbutton.gridy++;
			testmineOKlights[n]=new JPanel();
			testmineOKlights[n].setBorder(BorderFactory.createLineBorder(Color.black));
			cbutton.gridx=2*n;
			//cbutton.weightx=0;
			cbutton.anchor=GridBagConstraints.EAST;
			buttonPanel.add(testmineOKlights[n],cbutton);
			cbutton.gridx=2*n+1;
			//cbutton.weightx=1;
			cbutton.anchor=GridBagConstraints.WEST;
			cbutton.fill=GridBagConstraints.HORIZONTAL;
			buttonPanel.add(TestButtons[n],cbutton);
			cbutton.fill=GridBagConstraints.NONE;

			cbutton.gridy++;
			//versionpanel.setLayout(new BoxLayout(versionpanel,BoxLayout.X_AXIS));
			versionOKlights[n]=new JPanel();
			versionOKlights[n].setBorder(BorderFactory.createLineBorder(Color.black));
			cbutton.gridx=2*n;
			cbutton.anchor=GridBagConstraints.EAST;
			buttonPanel.add(versionOKlights[n],cbutton);
			cbutton.gridx=2*n+1;
			cbutton.anchor=GridBagConstraints.WEST;
			cbutton.fill=GridBagConstraints.HORIZONTAL;
			buttonPanel.add(mineversions[n],cbutton);
			cbutton.fill=GridBagConstraints.NONE;
			mineversions[n].addActionListener(new comboboxlistener(n));			


			cbutton.gridy++;
			idmapmineOKlights[n]=new JPanel();
			idmapmineOKlights[n].setBorder(BorderFactory.createLineBorder(Color.black));
			cbutton.gridx=2*n;
			cbutton.anchor=GridBagConstraints.EAST;
			buttonPanel.add(idmapmineOKlights[n],cbutton);
			cbutton.gridx=2*n+1;
			cbutton.anchor=GridBagConstraints.WEST;
			cbutton.fill=GridBagConstraints.HORIZONTAL;
			buttonPanel.add(idmapmines[n],cbutton);
			cbutton.fill=GridBagConstraints.NONE;

			cbutton.gridx=2*n+1;
			cbutton.gridy++;
			buttonPanel.add(relaunchidmaps[n],cbutton);
		}

		cbutton.fill=GridBagConstraints.HORIZONTAL;
		cbutton.gridx=0;
		cbutton.gridy++;
		cbutton.gridwidth=2;
		buttonPanel.add(analyzesavebutton,cbutton);

		cbutton.fill=GridBagConstraints.NONE;
		cbutton.gridx=0;
		cbutton.gridy++;
		cbutton.gridwidth=2;
		buttonPanel.add(redoanalyzesaveCheck,cbutton);

		userLabel = new JLabel("Username:", SwingConstants.LEFT);
		passLabel = new JLabel("password:", SwingConstants.LEFT);
		userLabel.setLabelFor(userText);
		passLabel.setLabelFor(passText);

		cbutton.gridx=0;
		cbutton.gridy++;
		cbutton.gridwidth=2;
		buttonPanel.add(userLabel,cbutton);
		cbutton.gridx=2;
		buttonPanel.add(userText,cbutton);


		cbutton.gridx=0;
		cbutton.gridy++;
		buttonPanel.add(passLabel,cbutton);
		cbutton.gridx=2;
		buttonPanel.add(passText,cbutton);

		cbutton.gridx=0;
		cbutton.gridy++;
		buttonPanel.add(savePassCheck,cbutton);
		cbutton.gridx=2;
		buttonPanel.add(playOfflineCheck,cbutton);

		cbutton.fill=GridBagConstraints.HORIZONTAL;
		cbutton.gridx=0;
		cbutton.gridy++;
		cbutton.gridwidth=4;
		buttonPanel.add(ConvertButton,cbutton);

		cbutton.fill=GridBagConstraints.NONE;
		cbutton.gridx=0;
		cbutton.gridy++;
		cbutton.gridwidth=3;
		buttonPanel.add(redoconvertmapCheck,cbutton);
		//		cbutton.gridx=0;
		//		cbutton.gridy=9;
		//		cbutton.gridwidth=4;
		//		cbutton.gridheight=4;
		//		buttonPanel.add(Box.createVerticalStrut(200),cbutton);

		//Add the buttons and the log to this panel.
		add(buttonPanel,BorderLayout.LINE_START);
		add(rightside,BorderLayout.LINE_END);
		initialized=true;
		updateframe2();
	}
	private class textfieldListener implements ActionListener{
		int type;
		JTextField source;
		// 2 saveleveldattextfield
		// 0 origin
		// 1 destination
		public textfieldListener(int type) {
			super();
			this.type = type;
			switch (type){
			case 0:
				source=origintextfield;
				break;
			case 1:
				source=destinationtextfield;
				break;
			case 2:
				source=originleveldattextfield;
				break;
			case 3:
				source=destinationsavefoldertextfield;
				break;
			}
		}
		public void doupdate(ActionEvent arg0) throws BadLocationException{
			String doctext="";
			doctext=source.getDocument().getText(0,source.getDocument().getLength());

			if (!doctext.equals("")){
				switch(type){
				case 2:
					File origsave=new File(doctext);
					try {
						ConverterMain.addworker(new MinecraftSaveSelecter(origsave,0));
						ConverterMain.addworker(new MinecraftAnalyzerConverter(false));
					} catch (BadMinecraftSaveException e) {
						Logger.logln("Bad Minecraft save");
					}
					if (!ConverterMain.isdone.get("choosesave0")){
						source.getDocument().remove(0, source.getDocument().getLength());
					}
					break;
				case 3:
					File destsave=new File(doctext);
					try {
						ConverterMain.addworker(new MinecraftSaveSelecter(destsave,1));
					} catch (BadMinecraftSaveException e) {
						Logger.logln("Bad Minecraft save");
					}
					if (!ConverterMain.isdone.get("choosesave1")){
						source.getDocument().remove(0, source.getDocument().getLength());
					}

					break;
				case 0:case 1:
					File minecraftnew=new File(doctext);
					if (minecraftnew.exists()){
						ConverterMain.isdone.put("choosemine"+type,false);
						MinecraftLoader.LoadMinecraftApp(minecraftnew,type);
						if (!ConverterMain.isdone.get("choosemine"+type)){
							source.getDocument().remove(0, source.getDocument().getLength());
						}
					}else{
						Logger.logln(minecraftnew+" doesn't exist");
					}
					break;
				}
			}else{
				switch(type){
				case 2:
					ConverterMain.isdone.put("choosesave0", false);
					ConverterMain.options.setsave(null,0);
					Logger.logln("No selected save");
					break;
				case 3:
					ConverterMain.isdone.put("choosesave1", false);
					ConverterMain.options.setsave(null,1);
					Logger.logln("No selected destination save");
					break;
				case 0:case 1:
					ConverterMain.isdone.put("choosemine"+type, false);
					ConverterMain.isdone.put("testmine"+type, false);
					ConverterMain.isdone.put("idmap"+type, false);
					ConverterMain.options.setminecraft(null, type);
					Logger.logln("No selected jar");
					break;
				}
			}
			MainFrame.updateframe2();
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				doupdate(arg0);
			} catch (BadLocationException e) {
				Logger.logln("ERROR with the textfields");
			}
		}

	}

	private class comboboxlistener implements ActionListener{
		int type;
		comboboxlistener(int n){
			this.type=n;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() instanceof JComboBox){
				JComboBox versbox=(JComboBox) arg0.getSource();
				ConverterMain.options.setminecraftversion(type, versbox.getSelectedItem().toString());
				MainFrame.updateframe2();
			}
		}

	}

	private class buttonsActionListener implements ActionListener{

		FileFilter jarfilefilter=new FileFilter() {

			@Override
			public String getDescription() {
				return "*.jar";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".jar") || f.isDirectory();
			}
		};
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int n=0;n<2;n++){
				if (e.getSource() == choosesavebuttons[n]) {
					JF.setCurrentDirectory(ConverterMain.options.getMCdir(0));
					JF.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					JF.showOpenDialog(JF);
					try {
						ConverterMain.addworker(new MinecraftSaveSelecter(JF.getSelectedFile(),n));
						//ConverterMain.options.setsave(new MinecraftSave(JF.getSelectedFile(),n),n);
						if (n==0){
							ConverterMain.addworker(new MinecraftAnalyzerConverter(false));
						}
					} catch (BadMinecraftSaveException e1) {
						Logger.logln("Bad Minecraft save");
					}
				}
				if (e.getSource() == chooseminebuttons[n]) {
					JF.setCurrentDirectory(ConverterMain.options.getMCdir(n));
					//JF.setFileSelectionMode(JFileChooser.FILES_ONLY);
					JF.setFileFilter(jarfilefilter);
					JF.showOpenDialog(JF);
					MinecraftLoader.LoadMinecraftApp(JF.getSelectedFile(), 1);         
				}
				if (e.getSource() == idmapmines[n]) {
					ConverterMain.addworker(new MinecraftIDnameMapper(ConverterMain.options.getminecraft(n)));

					//MinecraftIDnameMapper.IDMapMinecraft(ConverterMain.options.getminecraft(n));
				}
				if (e.getSource() == TestButtons[n]) {
					ConverterMain.addworker(new MinecraftTester(ConverterMain.options.getminecraft(n)));
				}
			}
			if (e.getSource() == ConvertButton) {
				ConverterMain.LaunchConvert();			
			}
			if (e.getSource() == analyzesavebutton) {
				ConverterMain.addworker(new MinecraftAnalyzerConverter(false));
			}

		}

	}

	public static void updateframe() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					MainFrame.updateframe2();
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void updateframe2() {
		if (initialized){
			ConvertButton.setEnabled(false);
			originleveldattextfield.setText(ConverterMain.options.getsavepath(0));
			destinationsavefoldertextfield.setText(ConverterMain.options.getsavepath(1));
			origintextfield.setText(ConverterMain.options.getminecraftjarpath(0));
			destinationtextfield.setText(ConverterMain.options.getminecraftjarpath(1));
			savePassCheck.setEnabled(false);
			playOfflineCheck.setEnabled(false);
			userText.setEnabled(false);
			passText.setEnabled(false);
			userLabel.setEnabled(false);
			passLabel.setEnabled(false);
			analyzesavebutton.setEnabled(false);
			redoconvertmapCheck.setEnabled(false);
			redoanalyzesaveCheck.setEnabled(false);
			if (ConverterMain.isdone.get("choosesave0")){
				analyzesavebutton.setEnabled(true);	
				redoanalyzesaveCheck.setEnabled(true);
			}
			boolean canconvert=true;
			for (int n=0;n<2;n++){
				ConvertButton.setEnabled(false);
				idmapmines[n].setEnabled(false);
				relaunchidmaps[n].setEnabled(false);
				TestButtons[n].setEnabled(false);
				TestButtons[n].setEnabled(false);
				MainFrame.testmineOKlights[n].setBackground(Color.RED);
				MainFrame.versionOKlights[n].setBackground(Color.RED);
				MainFrame.idmapmineOKlights[n].setBackground(Color.RED);
				mineversions[n].setEnabled(false);

				mineversions[n].setSelectedItem(ConverterMain.options.getminecraftversion(n));
				if (ConverterMain.isdone.get("testmine"+n)){
					MainFrame.testmineOKlights[n].setBackground(Color.GREEN);
					idmapmines[n].setEnabled(true);
					relaunchidmaps[n].setEnabled(true);
				}else{
					canconvert=false;
				}
				if (!ConverterMain.options.getminecraftversion(n).equals("Unknown") &&
						!ConverterMain.options.getminecraftversion(n).equals("Unsupported")){
					MainFrame.versionOKlights[n].setBackground(Color.GREEN);
				}else{
					canconvert=false;
				}

				if (!ConverterMain.isdone.get("choosesave"+n)){
					canconvert=false;
				}

				if (ConverterMain.isdone.get("choosemine"+n)){
					TestButtons[n].setEnabled(true);
					idmapmines[n].setEnabled(true);
					mineversions[n].setEnabled(true);
					relaunchidmaps[n].setEnabled(true);
					savePassCheck.setEnabled(true);
					playOfflineCheck.setEnabled(true);
					userText.setEnabled(true);
					passText.setEnabled(true);
					userLabel.setEnabled(true);
					passLabel.setEnabled(true);
				}else{
					canconvert=false;
				}
				if (ConverterMain.isdone.get("idmap"+n)){
					TestButtons[n].setEnabled(true);
					idmapmines[n].setEnabled(true);
					MainFrame.idmapmineOKlights[n].setBackground(Color.GREEN);
				}else{
					canconvert=false;
				}
			}
			if (canconvert) {
				ConvertButton.setEnabled(true);
				redoconvertmapCheck.setEnabled(true);
			}
			ConverterMain.options.save();
		}
	}

}
