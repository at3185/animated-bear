package iCal2google;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.Description;
import biweekly.property.Location;
import biweekly.property.Summary;

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
	private Calendar service;
	private MyProperties prop = new MyProperties();

	public Setup() throws IOException, GeneralSecurityException {
		connectToService();
	}

	public void connectToService() throws IOException, GeneralSecurityException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		// The clientId and clientSecret can be found in Google Developers
		// Console
		String clientId = prop.getProperty(prop.CLIENT_ID);
		if (clientId == null || clientId.isEmpty()) {
			System.err.println(prop.CLIENT_ID + " incorrectly set!");
			prop.deletePropFile();
		}
		String clientSecret = prop.getProperty(prop.CLIENT_SECRET);
		if (clientSecret == null || clientSecret.isEmpty()) {
			System.err.println(prop.CLIENT_SECRET + " incorrectly set!");
			prop.deletePropFile();
		}

		// Or your redirect URL for web based applications.
		String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";
		String scope = "https://www.googleapis.com/auth/calendar";
		GoogleTokenResponse response = null;

		String refreshTokenStr = prop.getProperty(prop.REFRESH_KEY);

		if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
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

			// Step 2: Get and save refresh token
			response = flow.newTokenRequest(code).setRedirectUri(redirectUrl).execute();
			refreshTokenStr = response.getRefreshToken();
			prop.setProperty(prop.REFRESH_KEY, refreshTokenStr);
		} else {
			// If we have the refresh token user intervention is not needed
			// anymore to get the access token
			try {
				response = new GoogleRefreshTokenRequest(httpTransport, jsonFactory, refreshTokenStr, clientId,
						clientSecret).execute();
				// System.out.println("Access token: " +
				// response.getAccessToken());
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

		service = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("mycal").build();
	}

	public void execute() throws IOException, GeneralSecurityException, ParseException {
		//used to keep track of number of events added to calendar
		Integer numEventsAdded = 0;
		
		// login and initialize service
		connectToService();
		String calID = getCalID();

		// clear all events in selected calendar
		clearEvents(calID);
		
		// open Ical file and parse all entries to icals var
		File file = new File("C:\\Users\\" + System.getProperty("user.name") + "\\myCal.ics");
		List<ICalendar> icals = Biweekly.parse(file).all();
		
		//for each calendar in the ical file..
		for (int i = 0; i < icals.size(); i++) {
			List<VEvent> events = icals.get(i).getEvents();
			//for each event in the calendar..
			for (int j = 0; j < events.size(); j++) {
				// convert biweekly date object to standard Date
//				Date startTime = df.parse(events.get(j).getDateStart().getValue().toString());
//				Date endTime = df.parse(events.get(j).getDateEnd().getValue().toString());
				Date startTime = new Date(events.get(j).getDateStart().getValue().getTime());
				Date endTime = new Date(events.get(j).getDateEnd().getValue().getTime());

				// create Google API specific event and populate it
				Event event = new Event();

				// extract summary and put it google event
				Summary summary = events.get(j).getSummary();
				if (summary != null)
					event.setSummary(summary.getValue());
				// extract location and put it google event
				Location location = events.get(j).getLocation();
				if (location != null)
					event.setLocation(location.getValue());
				Description description = events.get(j).getDescription();
				if (description != null)
					event.setDescription(description.getValue());

				// if event is recurrent then insert each recurrence otherwise
				// insert it once
				// NOTE: alternatively can use RRULE to google and make it a
				// single call, for example
				// "event.setRecurrence(Arrays.asList("RRULE:FREQ=WEEKLY;UNTIL=20110701T170000Z"));"
				if (events.get(j).getRecurrenceRule() != null) {
					Iterator<Date> startTimeRec = events.get(j).getRecurrenceRule().getDateIterator(startTime);
					Iterator<Date> endTimeRec = events.get(j).getRecurrenceRule().getDateIterator(endTime);
					while (startTimeRec.hasNext()) {
						event.setStart(new EventDateTime().setDateTime(new DateTime(startTimeRec.next().getTime())));
						event.setEnd(new EventDateTime().setDateTime(new DateTime(endTimeRec.next().getTime())));
						insertEvent(calID, event);
						numEventsAdded++;
					}

				} else {
					event.setStart(new EventDateTime().setDateTime(new DateTime(startTime.getTime())));
					event.setEnd(new EventDateTime().setDateTime(new DateTime(endTime.getTime())));
					insertEvent(calID, event);
					numEventsAdded++;
				}
			}
		}

		System.out.println(numEventsAdded.toString() + " events added!");
	}

	
	// insert an event to calendar; add printout before doing the insertion
	void insertEvent(String calID, Event event) throws IOException {
		System.out.println("Inserting at time " + event.getStart().toPrettyString() + " event \""
				+ event.getSummary() + "\"");

		// Save to calendar
		service.events().insert(calID, event).execute();
	}

	
	// Get calendar ID from properties file. Or create it for the first time if
	// ID is not valid.
	String getCalID() {
		com.google.api.services.calendar.model.Calendar calendar = null;
		String calID = prop.getProperty(prop.CALID);
		boolean createCal = false;
		if (calID != null) {
			try {
				service.calendars().get(calID).execute();
			} catch (IOException e) {
				System.out.println("Could not find calendar by ID! Creating a new one..");
				createCal = true;
			}
		}

		if (createCal || calID == null) {
			// Create a new calendar
			calendar = new com.google.api.services.calendar.model.Calendar();
			calendar.setSummary("-E-");
			calendar.setTimeZone("Europe/Stockholm");
			// Insert the new calendar
			try {
				calendar = service.calendars().insert(calendar).execute();
				calID = calendar.getId();
				//save new calendar ID to properties file
				prop.setProperty(prop.CALID, calID);
				System.out.println("New calendar created!");
			} catch (IOException e1) {
				System.err.println("Could not insert new calendar to calendars list!");
				e1.printStackTrace();
				System.exit(1);
			}
		}
		return calID;
	}

	
	//delete all events in selected calendar
	void clearEvents(String calID) {
		// Iterate over the events in the specified calendar
		String pageToken = null;
		do {
			com.google.api.services.calendar.model.Events events = null;
			try {
				events = service.events().list(calID).setPageToken(pageToken).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  List<Event> items = events.getItems();
		  for (Event event : items) {
		    try {
				service.events().delete(calID, event.getId()).execute();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  }
		  pageToken = events.getNextPageToken();
		} while (pageToken != null);
	}
}
