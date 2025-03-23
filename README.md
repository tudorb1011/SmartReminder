
# SmartReminder

**Developed by:** Tudor-Neculai Bâlbă & Raul-Anton Jac  
**Group:** 1231EA  

**Full Code:** [GitHUB](https://github.com/jacraul/SmartReminder)

---

## Introduction

**SmartReminder** is an Android application designed to help users manage tasks efficiently. With SmartReminder, users can:

- Add tasks with specific deadlines.
- Receive timely notifications 24 hours, 1 hour, and at the exact moment before the task deadline.
- Share daily schedules via social apps.
- Stay updated with weather forecasts and motivational quotes.

---

## Features

- **Task Notifications:** Alerts 24 hours, 1 hour, and at the task’s deadline.
- **Weather Updates:** Real-time weather information displayed in the notification bar.
- **Daily Quotes:** Inspirational quotes fetched from ZenQuotes API.
- **Dark Mode:** Theme switching between light and dark modes.
- **Share Functionality:** Share daily task schedules with others.
- **Persistent Storage:** SQLite database for task management.

---

## Technologies Used

- **IDE:** Android Studio
- **Language:** Kotlin
- **Database:** SQLite
- **APIs:**
  - [ZenQuotes API](https://zenquotes.io/api/random)
  - [OpenWeather API](https://openweathermap.org/api)

---

## File Structure

### Main Directories:
- **app**
  - **manifests**
    - `AndroidManifest.xml`
  - **java/kotlin**
    - `com.example.smartreminder`
      - `AboutActivity.kt`
      - `AddTask.kt`
      - `Database.kt`
      - `DeadlineNotifier.kt`
      - `DeadlineReceiver.kt`
      - `MainActivity.kt`
      - `TasksActivity.kt`
      - `SettingsActivity.kt`
      - `WeatherService.kt`
  - **res**
    - **drawable**: App icons and images
    - **layout**: XML files for UI layouts
    - **values**: XML files for themes and constants

---

## Key Components and Explanations

### Database (Database.kt)

- SQLite database stores tasks with the following schema:
  - **ID**: Unique task identifier.
  - **TASK**: Task description.
  - **DEADLINE_DATE**: Task deadline date.
  - **DEADLINE_TIME**: Task deadline time.
- Key Functions:
  - `addTask`: Adds a new task.
  - `deleteTask`: Deletes a specific task.
  - `getAllTasks`: Fetches all tasks.
  - `getTodayTasks`: Retrieves tasks scheduled for the current day.

### MainActivity

- **APIs:**
  - **Quotes API**: Fetches motivational quotes via a GET request.
  - **Weather API**: Fetches current weather information.
- **Notifications:**
  - Handles alerts 24 hours, 1 hour, and at task deadlines.
  - Processes task date and time to schedule accurate notifications.
- **Task Management:**
  - Displays today’s tasks or a "No tasks today" message.
- **Share Functionality:**
  - Allows sharing the daily schedule through social apps.

For both api’s we had to addthe INTERNET permission use in AndroidManifest.xml 

**Tasks for today** – we display all tasks from today, if not, we display the message „No tasks today”

**Notifications for tasks** (24h before, 1h before and when the task is in progress – finished the deadline):
-	1st we get all the tasks from database
-	then we call the DeadlineNotifier (more about it in the DeadlineNotifier explanations)
-	in the for we retrve the taskDate and time STR comes from strings cause we tried with date and time format – a total fail – so we didn’t changed the name of the variables back to taskDate and taskTime, we keep them as we used in testing mode
-	sdf was used for formatting the date and time

**TRY:**
- we transform the taskDateTimeStr string into Date object, then we take the time from it to compute the time miliseconds to use it for comparisons
-	using the calendar we take the current date into nowCalendar and nowMillis for current time in miliseconds
- then in the taskCalendar we put taskDateTime cause we will use taskDateTime later and we will modify it
-	we compute the time for notifications for each task for 24 hours in miliseconds and also 1h in miliseconds 
-	and now we check if the they are for 24h or 1h notifications 
  - for 24h we check the date also to be in this day (variabile isDayBefore)
  -	for 1h we check the date to be today or one hour before if the day is also tommorrow
  -	for notifications when deadline is done we simply check the time to be the same

**Share button:** you can share your daily program on any social app (like we did in the lab).


### TasksActivity

- **Task Operations:**
  - View, add, and delete tasks.
- **User Interaction:**
  - Long press to prompt task deletion confirmation.
 
-	1st we connect the database
-	then we implement the add task button
-	and now we list the tasks
-	in the if task!null we display the tasks and also we check if the user pressed long – this will popup the builder to display the alertdialog to check if the user is sure that want to delete the task


### AddTask

-	1st we connect the database
-	then we declare two variables which will store the selected date and time
-	then we have the declaration of layout elements
-	**selectDateButton** let the user choose the task date deadline and we verify the selected date: if the month is smaller than 9 we add a 0 before. why? cause we will need when we display the tasks ordered by month; if we let it be 9, when we have 11, 11 will be displayed before 9 cause the date is a string; same for dayOfMonth like for month
-	in the same way we did the selectTimeButton
-	then after the addButton is clicked we process the adding into the database AFTER we check that all elements which will be inserted are NOT EMPTY.
-	the backButton is declared as the buttons from the fixed bottom menu


### SettingsActivity

- **Theme Management:**
  - Implements light and dark mode switching.
- **Weather Service Control:**
  - Starts or stops the weather notification service.

### WeatherService

This part of our app will manage the weather forecast in the notification bar. How? Let’s see:
-	1st we call the createNotificationChannel (taken from AndroidStudio website) 
-	we implement the foreground service (WeatherService) which is responsible for fetching and displaying the weather information. This service runs continuously in the background, and it’s made a foreground service to keep it running reliably even when the app is in the background. A notification is shown to the user while the service is active
-	we use a Timer object to set up a task that will execute every 60 seconds. The task calls the fetchWeather() method which is responsible for fetching the latest weather data
-	In the fetchWeather() function, a coroutine (concept in Kotlin (and other programming languages) that allows you to write asynchronous, non-blocking code in a more readable and structured way) is launched on the IO dispatcher to perform the network request asynchronously. An HTTP GET request is made to the OpenWeatherMap API, fetching weather data for a specified city (Bucharest in this case). Upon a successful response, the data is parsed to retrieve the weather description and temperature, which are then used to update the lastWeatherUpdate string with the latest information
-	we update the notification by calling updateNotification(lastWeatherUpdate). This updates the content of the ongoing notification with the most recent weather data, ensuring that the user is kept informed of the latest weather conditions. The notification is continuously updated every 60 seconds as long as the service is running, ensuring real-time data is displayed


### DeadlineNotifier & DeadlineReceiver

- **DeadlineNotifier:**
The scheduleNotification function schedules an alarm to trigger a notification one minute before a task's deadline. It checks for permissions to schedule exact alarms on Android 12+ and requests them if not granted. The function calculates the time for the notification, creates an intent to trigger a DeadlineReceiver, and wraps it in a PendingIntent. It then uses AlarmManager to set the exact time for the notification. If successful, it logs the schedule; otherwise, it shows a Toast with an error message.

- **DeadlineReceiver:**
We have a BroadcastReceiver that listens for alarms triggered by the DeadlineNotifier. When the alarm goes off, it receives the task name from the intent. It then creates a notification using NotificationCompat.Builder with a title indicating the task deadline and the task name. This notification is displayed with a high priority and a specified icon. The NotificationManager is used to issue the notification, and the task name's hash code is used to uniquely identify it.

---

## Usage of Android Components

- **Foreground Services:** Weather updates in the notification bar.
- **Background Services:** Task notifications.
- **Intents:** Navigation and data sharing.
- **Activities:** Separate screens for task management, settings, etc.
- **Broadcast Receivers:** Handle scheduled alarms.
- **Shared Preferences:** Save user preferences for themes.
- **Content Providers:** Enable sharing daily schedules.
- **Database:** Manage task data with SQLite.
- **External APIs:** Fetch quotes and weather data.
- **Notifications:** Alerts at multiple time intervals.

---

## Future Enhancements

- Add customizable notification intervals.
- Support multiple languages.
- Integrate with cloud services for task synchronization.

---

## Contributors

- Tudor-Neculai Bâlbă
- Raul-Anton Jac


