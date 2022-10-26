package se.kau.isgb33;
import java.io.*;



public class View {
    private Controler c;
    private BufferedReader in;
    public View(Controler con) {
           in=new BufferedReader(new InputStreamReader(System.in));
	   c=con;
	   
    }
    public void runUI()  {
           String slask="100";
           try {
                while (Integer.parseInt(slask)!=0) {
                    System.out.println("*********Menu*******************");
                    System.out.println("1. Add record");
                    System.out.println("2. Search record");
                    System.out.println("3. show Next record");
                    System.out.println("4. show Previous record");
                    System.out.println("5. Remove record");
                    System.out.println("\n0. Quit");
                    slask=in.readLine();
	                c.handleEvent(Integer.parseInt(slask));
	           }
	        }
	        catch (Exception e) {
	            System.out.println("Unexpected events in View.runUI exiting");
	            e.printStackTrace();
	            System.exit(1);
	        }
     }
     public void viewCurr(Record r) {
            System.out.println("*********Current record*********");
            System.out.println("Pnr: "+r.getPnr());
            System.out.println("Name: "+r.getName());
            System.out.println("Club: "+r.getClub());
            System.out.println("\n\n");
    }
    public String viewSearch() {
       String slask="";
       try {
	       System.out.println("Enter Pnr for search: ");
	       slask= in.readLine();
	   }
       catch (Exception e) {
            System.out.println("Unexpected events in View.viewSearch exiting");
	        System.exit(1);
	    }
	    return(slask);
    }
    public void viewEdit(Record r) {
       try {
            System.out.println("Enter Pnr  : ");
	        r.setPnr(in.readLine());
	        System.out.println("Enter Name : ");
	        r.setName(in.readLine());
	        System.out.println("Enter Club: ");
	        r.setClub(in.readLine());
	    }
	    catch (Exception e) {
            System.out.println("Unexpected events in View.viewEdit exiting");
	        System.exit(1);
	    }

    }
}




