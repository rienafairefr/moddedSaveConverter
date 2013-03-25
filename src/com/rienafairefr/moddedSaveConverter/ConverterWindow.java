package com.rienafairefr.moddedSaveConverter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.rienafairefr.moddedSaveConverter.tasks.MinecraftAnalyzerConverter;
import com.rienafairefr.moddedSaveConverter.tasks.MinecraftConvertMapper;
import com.sk89q.mclauncher.util.UIUtil;

public class ConverterWindow extends JDialog{

	
	public static int columnid0=0;
	public static int columnid1=5;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1303408767013364985L;
	static final JTable[][] tables=new JTable[2][ConverterMain.Ntype];
	static final TableRowSorter[][] sorters=new TableRowSorter[2][ConverterMain.Ntype];
	static final String[][] filters=new String[2][ConverterMain.Ntype];



	static int index;

	/**
	 * Construct the dialog.
	 * 
	 * @param owner owning frame
	 * @param configuration configuration
	 * @param options options object
	 * @param initialTab index of the initial tab, 0 for the first
	 */
	public ConverterWindow(JFrame owner) {
		super(owner, "ID List", true);
		ConverterMain.addworker(new MinecraftConvertMapper());

		buildUI();
		pack();
		setSize(900, 600);
		setLocationRelativeTo(owner);
	}

//	class CustomRowFilter extends RowFilter {
//
//		public CustomRowFilter(int ntype, int leftorright) {
//			super();
//			this.ntype = ntype;
//			this.leftorright = leftorright;
//		}
//		int ntype; 
//		int leftorright; //0 for left table 1 for right table
//		@Override
//		public boolean include(Entry entry) {
//			if (leftorright==0){
//				return (entry.getStringValue(6).toLowerCase().indexOf("-1")!=-1 
//						|| entry.getStringValue(columnid1).toLowerCase().indexOf(filters[leftorright][ntype].toLowerCase())!=-1);
//			}else{
//				return (entry.getStringValue(1).toLowerCase().indexOf(filters[leftorright][ntype].toLowerCase())!=-1);
//			}
//
//		}  
//
//	}

	class CustomCellRenderer extends DefaultTableCellRenderer {    

		/**
		 * 
		 */
		private static final long serialVersionUID = 7243887534949577343L;

		public CustomCellRenderer() {
			int blabla=1;
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,int row,int column) {  
			Component c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);  
			//if (table.getModel().isCellEditable(row, column)){
			Object id1 = table.getValueAt(row, columnid1);
			Object id0 = table.getValueAt(row, columnid0);
			Integer typefound = (Integer) ConverterMain.options.getconvertmap(index).get(id0)[1];
			if (ConverterMain.options.getsave(0).listofidsinsave[index].contains(id0)){
				c.setEnabled(true);
				Integer minus1=Integer.valueOf(-1);
				Integer minus2=Integer.valueOf(-2);
				if (typefound<0){  
					c.setBackground( Color.RED );  
				}
				if (typefound==0){  
					c.setBackground( Color.GREEN ); 
				}
				if (typefound>0){  
					//c.setBackground( new Color(125,255,0));
					c.setBackground( Color.YELLOW );  
				}
			}else{
				c.setBackground( Color.GRAY );
				c.setEnabled(false);
			}
			return c;  
		}  
	}  

	private class CustomTablemodel extends DefaultTableModel{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7123347790903927783L;

		@SuppressWarnings("unchecked")
		@Override
		public Class getColumnClass(int columnIndex) {
			Class[] types;
			if (index==0 || index==1){
				types= new Class[]{ Integer.class, String.class,String.class, String.class,
						String.class,Integer.class,Integer.class };
			}else{
				types = new Class[]{ String.class, String.class, String.class,
						String.class,String.class,String.class,Integer.class };
			}
			return types[columnIndex];
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex){

			if (columnIndex==columnid1){
				Object id0=tables[0][index].getValueAt(rowIndex, columnid0);
				Class<?> keyclass=ConverterMain.idmaps[index].get(1).firstKey().getClass();
				if (keyclass.equals(aValue.getClass())){
					if(ConverterMain.idmaps[index].get(1).containsKey(aValue)) {
						ConverterMain.options.getconvertmap(index).put(id0, new Object[]{aValue,1});
					}
				}else{ 
					if (aValue.equals(String.class) && keyclass.equals(Integer.class)){
						try{
							Integer intvalue=Integer.parseInt((String) aValue);
							if(ConverterMain.idmaps[index].get(1).containsKey(intvalue)){
								ConverterMain.options.getconvertmap(index).put(id0, new Object[]{intvalue,1});
							}
						}catch (NumberFormatException e1){
							return;
						}
					}
				}
				if (aValue.equals(Integer.valueOf(-2)) ||aValue.equals("-2")){
					ConverterMain.options.getconvertmap(index).put(id0, new Object[]{aValue,-2});
					this.setValueAt("Exists in destination", rowIndex, 2);
				}else if (aValue.equals(Integer.valueOf(-1)) ||aValue.equals("-1")){
					ConverterMain.options.getconvertmap(index).put(id0, new Object[]{aValue,-1});
					this.setValueAt("no correspondence", rowIndex, 2);
				}else if (aValue.equals("")){
					ConverterMain.options.getconvertmap(index).put(id0, new Object[]{-1,-1});
					this.setValueAt("", rowIndex, 2);
				}
				ConverterMain.options.save();
			}

			super.setValueAt(aValue, rowIndex, columnIndex);
			this.fireTableDataChanged();
		}
		//			this.
		////			Object oldValue=this.getValueAt(rowIndex, columnIndex);
		////			if (oldValue instanceof String){
		////				oldValue=(String) oldValue;
		////			}else if (oldValue instanceof Integer){
		////				oldValue=Integer.toString((Integer)oldValue);
		////			}else{
		////				return;
		////			}
		////			if (aValue instanceof String){
		////				aValue=(String) aValue;
		////			}else if (aValue instanceof Integer){
		////				aValue=Integer.toString((Integer)aValue);
		////			}else{
		////				return;
		////			}
		////			if (!aValue.equals(oldValue)){
		////				
		////			}
		////			int blabla=1;
		//		}
		@Override
		public boolean isCellEditable(int row, int column) {
			if (column==5){
				return true;
			}else{
				return false;
			}
		}
	}
	
	class CustomRowFilter extends RowFilter {

		public CustomRowFilter(int ntype, int leftorright) {
			super();
			this.ntype = ntype;
			this.leftorright = leftorright;
		}
		int ntype; 
		int leftorright; //0 for left table 1 for right table
		@Override
		public boolean include(Entry entry) {
			if (leftorright==0){
				for (int ne=0;ne<((TableModel) entry.getModel()).getColumnCount();ne++){
					if (entry.getStringValue(1).toLowerCase().indexOf(filters[leftorright][ntype].toLowerCase())!=-1){
						return true;
					}
				}
				return false;
			}else{
				return (entry.getStringValue(1).toLowerCase().indexOf(filters[leftorright][ntype].toLowerCase())!=-1);
			}

		}  

	}
	/**
	 * Build the interface.
	 */
	@SuppressWarnings("unchecked")
	private void buildUI() {
		final ConverterWindow self = this;
		ToolTipManager ttm= ToolTipManager.sharedInstance();
		ttm.setInitialDelay(0);
		JPanel container = new JPanel();
		container.setBorder(BorderFactory.createEmptyBorder(8, 8, 5, 8));
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

		TreeMap<Object, Object[]>[] convertermaps=ConverterMain.options.getconvertmaps();
		TreeMap<Object, String[]>[] hash0=new TreeMap[ConverterMain.Ntype];
		TreeMap<Object, String[]>[] hash1=new TreeMap[ConverterMain.Ntype];

		// ConverterMain.getInstance().idmaps[ type (0 for blocks, 1 for items)].get( mineapp index (1 or 2))
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			hash0[ntype]=ConverterMain.idmaps[ntype].get(0);
			hash1[ntype]=ConverterMain.idmaps[ntype].get(1);
		}

		//Object minusone=new Integer(-1);

		Object[][][] datas = new Object[ConverterMain.Ntype][][];
		Integer[][] columnwidths = new Integer[ConverterMain.Ntype][];
		Object[][][] datasid1 = new Object[ConverterMain.Ntype][][];
		
		DefaultTableModel[] tableModels=new CustomTablemodel[ConverterMain.Ntype];

		//Object[] columnNames=new Object[]{"ID origin","NAME CLASSname","NAME CLASSname","ID destination"};
		Object[] columnNames=new Object[]{"ID","NAME","CLASS","NAME","CLASS","ID","type"};
		Object[] columnNamesid1=new Object[]{"ID destination","NAME","CLASSname"};

		
		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			Iterator it=convertermaps[ntype].keySet().iterator();
			int nl=0;

			datas[ntype]=new Object[convertermaps[ntype].size()][7];
			columnwidths[ntype] = new Integer[7];
			for (int nc=0;nc<7;nc++){
				columnwidths[ntype][nc]=0;
			}
			while(it.hasNext()){
				Object id0 = it.next();
				Object id1 = convertermaps[ntype].get(id0)[0];
				Object typefound = convertermaps[ntype].get(id0)[1];

				Class<?> keyclass=hash1[ntype].keySet().toArray()[0].getClass();
				Class<?> id1class=id1.getClass();
				columnid0=0;
				columnid1=5;
				if (hash1[ntype].containsKey(id1)){
					datas[ntype][nl][columnid0]=id0;
					datas[ntype][nl][1]=hash0[ntype].get(id0)[0];
					datas[ntype][nl][2]=hash0[ntype].get(id0)[1];
					datas[ntype][nl][3]=hash1[ntype].get(id1)[0];
					datas[ntype][nl][4]=hash1[ntype].get(id1)[1];
					datas[ntype][nl][columnid1]=id1;
				}else{
					datas[ntype][nl][columnid0]=id0;
					datas[ntype][nl][1]=hash0[ntype].get(id0)[0];
					datas[ntype][nl][2]=hash0[ntype].get(id0)[1];
					datas[ntype][nl][3]="no correspondence";
					datas[ntype][nl][4]="";
					datas[ntype][nl][columnid1]=-1;
				}
				
				datas[ntype][nl][6]=convertermaps[ntype].get(id0)[1];
				for (int nc=0;nc<7;nc++){
					columnwidths[ntype][nc]=Math.max(columnwidths[ntype][nc],datas[ntype][nl][nc].toString().length());
				}
				nl++;
				
			}

			datasid1[ntype]=new Object[ConverterMain.idmaps[ntype].get(1).size()][columnid1];
			Iterator itid1=ConverterMain.idmaps[ntype].get(1).entrySet().iterator();
			nl=0;
			while(itid1.hasNext()){
				Entry entry = (Entry) itid1.next();
				datasid1[ntype][nl][0]=entry.getKey();
				String[] arr=(String[]) entry.getValue();
				datasid1[ntype][nl][1]=arr[0];
				datasid1[ntype][nl][2]=arr[1];
				nl++;
			}
			
			tableModels[ntype]=new CustomTablemodel();
			tableModels[ntype].setDataVector(datas[ntype], columnNames);
			tables[0][ntype] = new JTable(tableModels[ntype]){
				private static final long serialVersionUID = 2674841423977356658L;
				public String getToolTipText(MouseEvent event) {
					Point p = event.getPoint();
					return this.getValueAt(rowAtPoint(p), columnAtPoint(p)).toString();
				}
			};

			//for (int nc=0;nc<7;nc++){
				//tables[0][ntype].getColumnModel().getColumn(nc).setMinWidth(minwidths[ntype][nc]);
				
				//tables[0][ntype].getColumnModel().getColumn(nc).setMaxWidth(minwidths[ntype][nc]);
			//}
			//tables[0][ntype].getColumnModel().getColumn(0).setMaxWidth(columnwidths[ntype][0]+1);
			//tables[0][ntype].getColumnModel().getColumn(5).setMaxWidth(columnwidths[ntype][5]+1);

			//tables[0][ntype].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tables[0][ntype].setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			TableColumnAdjuster tca = new TableColumnAdjuster(tables[0][ntype]);
			tca.adjustColumns();
			

			List<Object> ids=new ArrayList(ConverterMain.idmaps[ntype].get(1).keySet());
			if (ids.get(0) instanceof String){
				ids.add("-1");
				ids.add("-2");
			}else if (ids.get(0) instanceof Integer){
				ids.add(Integer.valueOf(-1));
				ids.add(Integer.valueOf(-2));
			}
			Collections.sort(ids,new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					if (arg0 instanceof String && arg1 instanceof String){
						return ((String) arg0).compareTo((String) arg1);
					}else if (arg0 instanceof Integer && arg1 instanceof Integer){
						return ((Integer) arg0).compareTo((Integer) arg1);
					}else if (arg0 instanceof String && arg1 instanceof Integer){
						return -1;
					}else if (arg0 instanceof Integer && arg1 instanceof String){
						return 1;
					}else{
						return 0;
					}
				}

			});
			Object[] cbdata=(new TreeSet(ids)).toArray();		
			JComboBox combobox=new JComboBox(cbdata);
			combobox.setEditable(true);
			tables[0][ntype].getModel().addTableModelListener(new TableModelListener(){

				@Override
				public void tableChanged(TableModelEvent e) {
					if (e.getColumn()==columnid1){
						CustomTablemodel model=(CustomTablemodel) e.getSource();
						Object id1=tables[0][index].getValueAt(e.getFirstRow(), e.getColumn());
						if (id1!=null){
							model.setValueAt(ConverterMain.idmaps[index].get(1).get(id1)[0], e.getFirstRow(), 3);
							model.setValueAt(ConverterMain.idmaps[index].get(1).get(id1)[1], e.getFirstRow(), 4);
							//if (ConverterMain.options.getconvertmap(index).containsKey(id1)){
//								model.setValueAt(4, e.getFirstRow(), 6);
	//						}
							//
						}
						
					}
				}
			});
			tables[0][ntype].getColumnModel().getColumn(columnid1).setCellEditor(new DefaultCellEditor(combobox));
			tables[1][ntype] = new JTable(datasid1[ntype],columnNamesid1){
				private static final long serialVersionUID = 1578432920309542187L;
				public String getToolTipText(MouseEvent event) {
					Point p = event.getPoint();
					return this.getValueAt(rowAtPoint(p), columnAtPoint(p)).toString();
				}
			};
			tables[1][ntype].setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			TableColumnAdjuster tca1 = new TableColumnAdjuster(tables[1][ntype]);
			tca1.adjustColumns();
		}


		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			filters[0][ntype]="";
			filters[1][ntype]="";

			tables[0][ntype].setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
			/*tables[0][ntype].getColumnModel().getColumn(0).setPreferredWidth(40);
			tables[0][ntype].getColumnModel().getColumn(1).setPreferredWidth(150);
			tables[0][ntype].getColumnModel().getColumn(2).setPreferredWidth(150);
			tables[0][ntype].getColumnModel().getColumn(3).setPreferredWidth(40);*/
			tables[0][ntype].getTableHeader().setVisible(true);
			tables[0][ntype].setDefaultRenderer(String.class, new CustomCellRenderer());
			tables[0][ntype].setDefaultRenderer(Integer.class, new CustomCellRenderer());

			sorters[0][ntype]=new TableRowSorter(tables[0][ntype].getModel());
			sorters[0][ntype].setRowFilter(new CustomRowFilter(ntype, 0));
			tables[0][ntype].setRowSorter(sorters[0][ntype]);
			//TODO
//			tables[1][ntype].setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
//			tables[1][ntype].getTableHeader().setVisible(true);
//			tables[1][ntype].getColumnModel().getColumn(0).setPreferredWidth(40);
//			tables[1][ntype].getColumnModel().getColumn(1).setPreferredWidth(150);

			sorters[1][ntype]=new TableRowSorter(tables[1][ntype].getModel());
			sorters[1][ntype].setRowFilter(new CustomRowFilter(ntype, 1));
			tables[1][ntype].setRowSorter(sorters[1][ntype]);


			//tablesid1[ntype].setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
			//TableRowSorter<?> sorter=new TableRowSorter();


		}
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				index=tabbedPane.getSelectedIndex();
			}
		});
		JPanel[] tablespanels=new JPanel[ConverterMain.Ntype];

		for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
			Dimension minimumSize = new Dimension(150, 600);
			Dimension maximumSize = new Dimension(150, 600);
			tables[1][ntype].setMinimumSize(minimumSize);
			tables[1][ntype].setMaximumSize(maximumSize);

			JScrollPane scrollpane1=new JScrollPane(tables[0][ntype]);
			JScrollPane scrollpane2=new JScrollPane(tables[1][ntype]);

			Dimension minimumSize2 = new Dimension(600, 600);
			scrollpane1.setMinimumSize(minimumSize2);

			JPanel scrollandsearch1=new JPanel(new BorderLayout());
			JPanel scrollandsearch0=new JPanel(new BorderLayout());
			//rightscrollandsearch.setMinimumSize(minimumSize);
			//			rightscrollandsearch.setPreferredSize(null);
			//rightscrollandsearch.setMaximumSize(minimumSize);
			//scrollpane2.setMinimumSize(minimumSize);
			//		scrollpane2.setPreferredSize(null);
			//scrollpane2.setMaximumSize(minimumSize);

			class actionlistener implements ActionListener{

				int index;
				int leftorright;

				public actionlistener(int ntype,int leftorright) {
					this.index=ntype;
					this.leftorright=leftorright;
				}
				@Override
				public void actionPerformed(ActionEvent arg0) {
					filters[leftorright][index]=((JTextField)arg0.getSource()).getText();
					((AbstractTableModel) tables[leftorright][index].getModel()).fireTableDataChanged();
				}

			}
			class customkeylistener implements KeyListener{

				int index;
				int leftorright;

				public customkeylistener(int ntype,int leftorright) {
					this.index=ntype;
					this.leftorright=leftorright;
				}

				public void updatefilter(KeyEvent arg0){
					filters[leftorright][index]=((JTextField)arg0.getSource()).getText();
					((AbstractTableModel) tables[leftorright][index].getModel()).fireTableDataChanged();
				}

				@Override
				public void keyPressed(KeyEvent arg0) {
					updatefilter(arg0);
					//((Customid1CellRenderer) tablesid1[index].getCellRenderer(0, 0)).filter=((JTextField) arg0.getSource()).getText();
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
					updatefilter(arg0);
				}

				@Override
				public void keyTyped(KeyEvent arg0) {
					updatefilter(arg0);
				}
			}

			JTextField searchfield1=new HintTextField("filter");
			searchfield1.addKeyListener(new customkeylistener(ntype,1));
			searchfield1.addActionListener(new actionlistener(ntype,1));

			scrollandsearch1.add(searchfield1,BorderLayout.NORTH);
			scrollandsearch1.add(scrollpane2,BorderLayout.SOUTH);
			//scrollpane2.setMinimumSize(minimumSize);

			JTextField searchfield0=new HintTextField("filter");
			searchfield0.addKeyListener(new customkeylistener(ntype,0));
			searchfield0.addActionListener(new actionlistener(ntype,0));
			scrollandsearch0.add(searchfield0,BorderLayout.NORTH);
			scrollandsearch0.add(scrollpane1,BorderLayout.SOUTH);

			JSplitPane tablessplitpane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					scrollandsearch0, scrollandsearch1);
			//tablessplitpane.setDividerLocation(0.75d);
			tabbedPane.addTab(ConverterMain.typenames[ntype], null, tablessplitpane,ConverterMain.typenames[ntype]);

			//			tablespanels[ntype]=new JPanel();
			//			tablespanels[ntype].setLayout(new GridBagLayout());
			//			GridBagConstraints c=new GridBagConstraints();
			//			//new BoxLayout(tablespanels[ntype], BoxLayout.X_AXIS)
			//			c.weightx=1;
			//			c.weighty=1;
			//			c.gridx=0;
			//			c.fill=GridBagConstraints.BOTH;
			//			//tables[ntype].setMaximumSize(new Dimension(1000,1000));
			//			
			//			//scrollpane1.setSize(800, 800);
			//			tablespanels[ntype].add(scrollpane1,c);
			//			c.gridx=1;
			//			c.weightx=0.3;
			//			c.weighty=1;	
			//			c.fill=GridBagConstraints.BOTH;
			//			
			//			scrollpane2.setMaximumSize(new Dimension(1000,200));
			//			//scrollpane2.setSize(800, 800);
			//			tablespanels[ntype].add(scrollpane2,c);
		}


		//for (int ntype=0;ntype<ConverterMain.Ntype;ntype++){
		//tabbedPane.addTab(ConverterMain.typenames[ntype], null, new JScrollPane(tables[ntype]),ConverterMain.typenames[ntype]);
		//tabbedPane.addTab(ConverterMain.typenames[ntype], null, tablespanels[ntype],ConverterMain.typenames[ntype]);

		//}

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		JButton closeButton = new JButton("Close");
		JLabel explanation = new JLabel();
		explanation.setText("if ID is <0 the conversion will remove the block/item/entity");
		JButton convertButton = new JButton("Convert");
		UIUtil.equalWidth(convertButton, closeButton);
		buttonsPanel.add(explanation);
		buttonsPanel.add(convertButton);
		buttonsPanel.add(closeButton);
		container.add(tabbedPane, BorderLayout.NORTH);
		container.add(buttonsPanel, BorderLayout.SOUTH);

		convertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				self.setVisible(false);
				self.dispose();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							ConverterMain.options.save();
							ConverterMain.addworker(new MinecraftAnalyzerConverter(true));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		});

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				self.dispose();
			}
		});

		add(container, BorderLayout.CENTER);
		revalidate();
	}

	class HintTextField extends JTextField implements FocusListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1469720205766937951L;
		private final String hint;

		public HintTextField(final String hint) {
			super(hint);
			this.hint = hint;
			super.addFocusListener(this);
			super.setForeground(Color.GRAY);
		}

		@Override
		public void focusGained(FocusEvent e) {
			if(this.getText().isEmpty()) {
				super.setText("");
				super.setForeground(Color.BLACK);
			}
		}
		@Override
		public void focusLost(FocusEvent e) {
			if(this.getText().isEmpty()) {
				super.setText(hint);
				super.setForeground(Color.GRAY);
			}
		}

		@Override
		public String getText() {
			String typed = super.getText();
			return typed.equals(hint) ? "" : typed;
			//return super.getText();
		}
	}
}