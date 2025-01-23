package com.WA;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.DayOfWeek;
import java.util.Locale;

public class WA_READER {

    public static void main(String[] args) {
        try {
            int instanceCount = getInstanceCount();

            for (int i = 1; i <= instanceCount; i++) {
                System.out.println("Starting instance #" + i);

                WebDriver driver = setupWebDriver(i);
                navigateToWhatsAppWeb(driver);
                waitForLogin(driver, i);

                String contactName = "Air India";

                if (scrollToContact(driver, contactName)) {
                    System.out.println("Contact " + contactName + " found.");
                    String day=openChat(driver, contactName);
                    processMessages(day,driver, contactName);
                } else {
                    System.out.println("Contact " + contactName + " not found.");
                }

                driver.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get the number of instances from the user
    private static int getInstanceCount() {
        System.out.println("Enter the number of WhatsApp instances to run:");
        return 2; // Fixed to 1 for now. Adjust as needed.
    }


    private static WebDriver setupWebDriver(int instanceIndex) {
        ChromeOptions options = new ChromeOptions();
        String userDataDir = "C:/Users/tejpr/AppData/Local/Google/Chrome/User Data/WhatsAppProfile" + instanceIndex;
        options.addArguments("--user-data-dir=" + userDataDir);
        options.addArguments("--profile-directory=Profile" + instanceIndex);
        return new ChromeDriver(options);
    }

    // Navigate to WhatsApp Web
    private static void navigateToWhatsAppWeb(WebDriver driver) {
        driver.get("https://web.whatsapp.com/");
    }

    // Check if the profile folder exists
    private static boolean isProfileFolderPresent(int instanceIndex) {
        String profilePath = "C:/Users/tejpr/AppData/Local/Google/Chrome/User Data/WhatsAppProfile" + instanceIndex;
        File profileFolder = new File(profilePath);
        return profileFolder.exists() && profileFolder.isDirectory();
    }

    // Trigger and scan only if profile folder is missing
    private static void waitForLogin(WebDriver driver, int instanceIndex) throws Exception {
        if (!isProfileFolderPresent(instanceIndex)) {
            System.out.println("Profile folder not found for instance #" + instanceIndex);
            System.out.println("Please scan the QR code for instance #" + instanceIndex + " and press Enter once logged in...");
            System.in.read(); // Wait for user input after scanning QR code
        } else {
            System.out.println("Profile folder exists for instance #" + instanceIndex + ". Skipping QR scan.");
        }
    }


    // Scroll to a specific contact in the chat list
    private static boolean scrollToContact(WebDriver driver, String contactName) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String chatsGrid = "//div[@role='grid']";
        String gridScroller = "//*[@id=\"pane-side\"]";
        String sendertag = "//div[div[div[span[@title='" + contactName + "' and @class='x1iyjqo2 x6ikm8r x10wlt62 x1n2onr6 xlyipyv xuxw1ft x1rg5ohu _ao3e']]]]";
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(chatsGrid)));

        boolean contactFound = false;

        while (!contactFound) {
            try {
                driver.findElement(By.xpath(sendertag));
                contactFound = true;
            } catch (Exception e) {
                scrollChatGrid(driver, gridScroller);
            }
        }

        return contactFound;
    }

    // Scroll the chat grid using JavaScript and fallback
    private static void scrollChatGrid(WebDriver driver, String gridScroller) throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[0].clientHeight",
                driver.findElement(By.xpath(gridScroller)));
        Thread.sleep(1000);
    }

    // Open a chat by clicking on the sender
    private static String openChat(WebDriver driver, String contactName) throws InterruptedException {
        String sendertag = "//div[div[div[span[@title='" + contactName + "' and @class='x1iyjqo2 x6ikm8r x10wlt62 x1n2onr6 xlyipyv xuxw1ft x1rg5ohu _ao3e']]]]";
        WebElement senderdiv = driver.findElement(By.xpath(sendertag));
        senderdiv.click();
        Thread.sleep(3000);

        String day = senderdiv.findElement(By.cssSelector("div[class='_ak8i']")).getText();
        System.out.println("Last message from " + contactName + " is on " + convertToDate(day));
        return day;
    }
    private static void processMessages(String dayOfLastMsg,WebDriver driver, String contactName) throws Exception {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.message-in")));

        JavascriptExecutor js = (JavascriptExecutor) driver; // For scrolling
        Set<String> uniqueMessages = new HashSet<>(); // To store unique messages
        List<String> notifications = new ArrayList<>();

        WebElement chatContainer = driver.findElement(By.xpath("//*[@id=\"main\"]/div[3]/div/div[2]")); // Chat container selector
        Double lastScrollHeight = null;
        boolean reachedTop = false; // Flag to indicate if we've reached the top of the chat

        while (!reachedTop) {
            // Get the current scroll height before scrolling
            Number currentScrollHeight = (Number) js.executeScript("return arguments[0].scrollTop;", chatContainer);
            double currentScrollHeightValue = currentScrollHeight.doubleValue();

            // Scroll up
            js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop - arguments[0].offsetHeight;", chatContainer);

            // Wait briefly for messages to load
            Thread.sleep(1000);

            // Get the new scroll height after scrolling
            Number newScrollHeight = (Number) js.executeScript("return arguments[0].scrollTop;", chatContainer);
            double newScrollHeightValue = newScrollHeight.doubleValue();

            // If the scroll height hasn't changed, we've reached the top
            if (lastScrollHeight != null && newScrollHeightValue == lastScrollHeight) {
                reachedTop = true;
            } else {
                lastScrollHeight = newScrollHeightValue;
            }
        }

        // Process all incoming messages after scrolling is complete
        List<WebElement> validIncomingMsgs= driver.findElements(By.xpath("//div[contains(@class, 'message-in') or contains(@class, '_amk4 _amkb')]"));
         String date= "";

         boolean firstEverDateFound=false;

        for (int i = 0; i < validIncomingMsgs.size();i++) {
            boolean canPush=true;
            WebElement messageElement = validIncomingMsgs.get(i);
            String messageText = readMessageText(messageElement); // Get the message text
            String time = readMessageTimestamp(messageElement);   // Get the timestamp

            // Create a unique identifier for each message (e.g., text + timestamp)
            String messageIdentifier = messageText + " | " + time;

            // Only process unique messages
            if (!messageText.isEmpty() ) {

                if (messageElement.getAttribute("class").contains("_amk4 _amkb")) {
                    firstEverDateFound=true;
                    canPush=false;
                    date= convertToDate(messageText);
                    time = messageElement.findElement(By.xpath(".//span[@class='_ao3e']")).getText();
                }
               if(firstEverDateFound==false && date==""){date=convertToDate(dayOfLastMsg); firstEverDateFound=true;}
                String formattedNotification = String.format(
                        "Message: %s\nDate: %s\nTime: %s",
                        messageText,date,time
                );
                if(firstEverDateFound && canPush)notifications.add(formattedNotification); // Add formatted notification to the list
            }
        }


        // Display all unique notifications
        displayNotifications(contactName, notifications);
    }

    public static String convertToDate(String input) {
        // Define the desired date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Get the current date (assuming today is 24/01/2025)
        LocalDate today = LocalDate.of(2025, 1, 24);

        // Normalize input to lowercase for consistent comparison
        String normalizedInput = input.trim().toLowerCase(Locale.ROOT);

        switch (normalizedInput) {
            case "today":
                return today.format(formatter);
            case "yesterday":
                return today.minusDays(1).format(formatter);
            case "sunday":
            case "monday":
            case "tuesday":
            case "wednesday":
            case "thursday":
            case "friday":
            case "saturday":
                // Calculate the date for the specified day of the week
                DayOfWeek targetDay = DayOfWeek.valueOf(normalizedInput.toUpperCase(Locale.ROOT));
                LocalDate targetDate = today.with(targetDay);
                // If the target day is after today, subtract a week to get the previous occurrence
                if (targetDate.isAfter(today)) {
                    targetDate = targetDate.minusWeeks(1);
                }
                return targetDate.format(formatter);
            default:
                // Attempt to parse the input as an existing date in dd/MM/yyyy format
                try {
                    LocalDate parsedDate = LocalDate.parse(input, formatter);
                    return parsedDate.format(formatter);
                } catch (DateTimeParseException e) {
                    // Handle invalid input
                    return "Invalid date input: " + input;
                }
        }
    }
    // Read all text from a message
    private static String readMessageText(WebElement messageElement) {
        // Locate both message and heading span elements within the messageElement
        List<WebElement> spanElements = messageElement.findElements(By.cssSelector("span.selectable-text, span._ao3e"));

        StringBuilder messageTextBuilder = new StringBuilder();
        for (int i = 0; i < spanElements.size(); i++) {
            String spanText = spanElements.get(i).getText().trim(); // Trim spaces from individual span
            messageTextBuilder.append(spanText);
            if (i < spanElements.size() - 1) {
                messageTextBuilder.append(" \n"); // Add a single space between spans
            }
        }
        return messageTextBuilder.toString(); // Return the formatted text
    }


    // Read the timestamp of a message
    private static String readMessageTimestamp(WebElement messageElement) {
        // Find the last <span> element inside the message container
        List<WebElement> spanElements = messageElement.findElements(By.tagName("span"));

        // Traverse the span elements in reverse order to get the last <span> element
        for (int i = spanElements.size() - 1; i >= 0; i--) {
            WebElement span = spanElements.get(i);

            // If the span has a class related to the timestamp, return its text
            if (span.getAttribute("class").contains("x1rg5ohu") && span.getAttribute("class").contains("x16dsc37")) {
                return span.getText().trim();
            }
        }

        // If no timestamp span is found, return a default value (could also throw an exception or log the issue)
        return "Timestamp not found";
    }


    // Display notifications
    private static void displayNotifications(String contactName, List<String> notifications) {

        System.out.println("=========================================");
        System.out.println(" Notifications for Contact: " + contactName);
        System.out.println("=========================================");

        if (notifications.isEmpty()) {
            System.out.println("No notifications available.");
        } else {
            int count = 1;
            for (String msg : notifications) {
                System.out.println("Notification " + count + ":");
                System.out.println(msg);
                System.out.println("----------------------------------------------------------------------------------");
                count++;
            }
        }
        System.out.println("==================================================================================");
    }

}

