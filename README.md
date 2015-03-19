Small java program to convert and upload ical files generated by Outlook to a google calendar.

Progam depends on biweekly library(http://sourceforge.net/projects/biweekly) which is used to parse ical files. It then uploads the events from ical file to a user specified google account.

Usage:

1. User must first set up a google developer guide and create a project that uses the calendar:
Follow steps 1 through 6 on this page https://developers.google.com/google-apps/calendar/instantiate
2. When application is run for the first time it will create a properties file named "config.properties". In this file it will store Client ID and Client Secret provided by user.
For subsequent runs there will be no more user interaction.


