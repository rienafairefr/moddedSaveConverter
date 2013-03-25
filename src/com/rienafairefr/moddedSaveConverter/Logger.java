package com.rienafairefr.moddedSaveConverter;

public class Logger {
	public static boolean canwritetologframe=false;
	public static String buffer="";
	public static int level=0;
	public static void logln(String string){
		log(string+"\n");
	}
	
	public static void log(String string){

		String	repeated = new String(new char[level]).replace("\0", "  ");
		string=repeated+string;
		if (MainFrame.log!=null){
			ConverterMain.mainframe.repaint();
			if (!canwritetologframe){
				MainFrame.log.append(buffer);
				System.out.print(buffer);
				MainFrame.log.setCaretPosition(MainFrame.log.getDocument().getLength());
				canwritetologframe=true;
			}
			MainFrame.log.append(string);
			System.out.print(string);
			MainFrame.log.setCaretPosition(MainFrame.log.getDocument().getLength());
		}else{
			buffer=buffer+string;
		}
	}

	public static void log(int inte) {
		logln(Integer.toString(inte));
	}
}
