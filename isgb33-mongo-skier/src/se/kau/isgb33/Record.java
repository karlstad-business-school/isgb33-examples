package se.kau.isgb33;
import java.io.*;

public class Record implements Serializable {
   
	private static final long serialVersionUID = 1L;
	private String pnr = "";
    private String name= "";
    private String club= "";
	public String getPnr() {
		return pnr;
	}
	public void setPnr(String pnr) {
		this.pnr = pnr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getClub() {
		return club;
	}
	public void setClub(String club) {
		this.club = club;
	}
   
}
