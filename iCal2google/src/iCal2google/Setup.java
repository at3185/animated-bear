package iCal2google;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class Setup {
	Calendar service;

	public void setUp() throws IOException, GeneralSecurityException {
		boolean use_refresh = true;
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		// The clientId and clientSecret can be found in Google Developers
		// Console
		String clientId = "CLIENT_ID";
		String clientSecret = "CLIENT_SECRET";

		// Or your redirect URL for web based applications.
		String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
		String scope = "https://www.googleapis.com/auth/calendar";
		GoogleTokenResponse response = null;
		if (!use_refresh) {

			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
					clientId, clientSecret, Collections.singleton(scope)).setAccessType("offline")
					.setApprovalPrompt("force").build();
			// Step 1: Authorize
			String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUrl).build();

			// Point or redirect your user to the authorizationUrl.
			System.out.println("Go to the following link in your browser:");
			System.out.println(authorizationUrl);

			// Read the authorization code from the standard input stream.
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("What is the authorization code?");
			String code = in.readLine();
			// End of Step 1

			// Step 2: Exchange
			response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
			System.out.println("Refresh token is " + response.getRefreshToken());

		} else {

			String refreshTokenStr = "REFRESH_TOK";

			try {
				response = new GoogleRefreshTokenRequest(httpTransport, jsonFactory, refreshTokenStr, clientId,
						clientSecret).execute();
				System.out.println("Access token: " + response.getAccessToken());
			} catch (TokenResponseException e) {
				if (e.getDetails() != null) {
					System.err.println("Error: " + e.getDetails().getError());
					if (e.getDetails().getErrorDescription() != null) {
						System.err.println(e.getDetails().getErrorDescription());
					}
					if (e.getDetails().getErrorUri() != null) {
						System.err.println(e.getDetails().getErrorUri());
					}
				} else {
					System.err.println(e.getMessage());
				}
			}
		}

		// End of Step 2
		Credential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory)
				.setClientSecrets(clientId, clientSecret).build().setFromTokenResponse(response);

		service = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("APP_NAME").build();
	}

	public void execute() throws IOException, GeneralSecurityException, ParseException {
		// login and initialize service
		setUp();

		// clear calendar
		service.calendars().clear("primary").execute();

		// open Ical file and parse all entries to icals var
		File file = new File("C:\\Users\\eiontol\\Desktop\\ITC.ics");
		List<ICalendar> icals = Biweekly.parse(file).all();

		for (int i = 0; i < icals.size(); i++) {
			List<VEvent> events = icals.get(i).getEvents();
			for (int j = 0; j < events.size(); j++) {
				//extract information from events
				String summary = events.get(j).getSummary().getValue();
				Date startTime = events.get(j).getDateStart().getValue().getRawComponents().toDate();
				Date endTime = events.get(j).getDateEnd().getValue().getRawComponents().toDate();
				
				//create Google API specific event and populate it 
				Event event = new Event();
				event.setSummary(summary);
				event.setStart(new EventDateTime().setDateTime(new DateTime(startTime.getTime())));
				event.setEnd(new EventDateTime().setDateTime(new DateTime(endTime.getTime())));

				System.out.println("Summary is " + summary);
				System.out.println("pretty start time is " + event.getStart().toPrettyString());
				
				//Save to calendar
				service.events().insert("primary", event).execute();
			}
		}
		
		
	}
}
