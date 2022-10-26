package se.kau.isgb33;



public class Controler {
	private RecordManager rm;
	private View ui;
	//private Record curr;
	private String sok="";

	public Controler() {
		rm=new RecordManager();
		rm.openFile("skier");
		ui=new View(this);
		ui.runUI();
	}
	public void handleEvent(int val) {
		Record curr=null;
		if (val==0) {
			rm.closeFile();
			System.exit(0);
		}
		if (val==1) {
			curr=rm.createRecord();
			ui.viewEdit(curr);
			rm.addRecord(curr);
		}
		if (val==2) {
			sok=ui.viewSearch();
			curr=rm.searchRecord(sok);
		}
		if (val==3) {
			curr=rm.nextRecord();
			if (curr==null) {
				curr = rm.lastRecord();
			}
		}
		if (val==4) {
		curr=rm.prevRecord();
			if (curr==null) {
				curr = rm.firstRecord();
			}
		}
		if (val==5) {
			sok=ui.viewSearch();
			curr=rm.searchRecord(sok);
			rm.removeRecord(curr);
			curr=rm.firstRecord();
		}
		ui.viewCurr(curr);
	}
	public static void main( String [] args) {
		new Controler();

	}
}

