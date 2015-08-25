Attribute VB_Name = "ExportCalendarSub"
Public Sub ExportCalendar1(Optional MyMail As MailItem)

 Dim oNamespace As NameSpace
 Dim oFolder As Folder
 Dim oCalendarSharing As CalendarSharing
 Dim sUsername As String
  
 On Error GoTo ErrRoutine
 
  
 ' Wait  a bit before running the script for the calendar to update
 ' since rule is triggered when message arrives in Inbox
 If Not MyMail Is Nothing Then
   Application.Wait (Now + TimeValue("0:00:10"))
 End If
 
 ' Get a reference to the Calendar default folder
 Set oNamespace = Application.GetNamespace("MAPI")
 Set oFolder = oNamespace.GetDefaultFolder(olFolderCalendar)
   
 ' Get a CalendarSharing object for the Calendar default folder.
 Set oCalendarSharing = oFolder.GetCalendarExporter
 ' Set the CalendarSharing object to export the contents of
 ' the entire Calendar folder, including attachments and
 ' private items, in full detail.
 With oCalendarSharing
 .CalendarDetail = olFullDetails
 .IncludeWholeCalendar = False
 .IncludeAttachments = False
 .StartDate = Now()
 .EndDate = Now() + 7
 .IncludePrivateDetails = False
 .RestrictToWorkingHours = False
 End With
 
 
 'Intent is to get the login username (assuming application does not modify system variable)
 sUsername = Environ("Username")
 
 ' Export calendar to an iCalendar calendar (.ics) file.
 oCalendarSharing.SaveAsICal "C:\users\" & sUsername & "\myCal.ics"
  
 ' Run sync script
 Shell "C:\users\" & sUsername & "\sync.bat"
 
EndRoutine:
 On Error GoTo 0
 Set oCalendarSharing = Nothing
 Set oFolder = Nothing
 Set oNamespace = Nothing
Exit Sub
 
 
 
ErrRoutine:
 Select Case Err.Number
 Case 287 ' &H0000011F
 ' The user denied access to the Address Book.
 ' This error occurs if the code is run by an
 ' untrusted application, and the user chose not to
 ' allow access.
 MsgBox "Access to Outlook was denied by the user.", vbOKOnly
 
 Case -2147467259 ' &H80004005
 ' Export failed.
 ' This error typically occurs if the CalendarSharing
 ' method cannot export the calendar information because
 ' of conflicting property settings.
 MsgBox Err.Description, vbOKOnly, Err.Number & " - " & Err.Source
 
 Case -2147221233 ' &H8004010F
 ' Operation failed.
 ' This error typically occurs if the GetCalendarExporter method
 ' is called on a folder that doesn't contain calendar items.
 MsgBox Err.Description, vbOKOnly, Err.Number & " - " & Err.Source
 
 Case Else
 ' Any other error that may occur.
 MsgBox Err.Description, vbOKOnly, Err.Number & " - " & Err.Source
 End Select
 
 GoTo EndRoutine
End Sub


