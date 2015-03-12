package iCal2google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;

public class Main {


	static public void main(String[] args) throws IOException {
		Setup set = new Setup();
		try {
			try {
				set.execute();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
