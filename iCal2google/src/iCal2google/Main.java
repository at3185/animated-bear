package iCal2google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class Main {
	

	static public void main(String[] args) {
		Setup set;
		try {
			set = new Setup();
			set.execute();
		} catch (IOException | GeneralSecurityException | ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
