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
                    openChat(driver, contactName);
                    processMessages(driver, contactName);
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
        return 1; // Fixed to 1 for now. Adjust as needed.
    }

    // Setup WebDriver with a unique profile
    private static WebDriver setupWebDriver(int instanceIndex) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=C:/Users/tejpr/AppData/Local/Google/Chrome/User Data/WhatsAppProfile" + instanceIndex);
        options.addArguments("--profile-directory=Profile" + instanceIndex);
        return new ChromeDriver(options);
    }

    // Navigate to WhatsApp Web
    private static void navigateToWhatsAppWeb(WebDriver driver) {
        driver.get("https://web.whatsapp.com/");
    }

    // Wait for user login via QR code
    private static void waitForLogin(WebDriver driver, int instanceIndex) throws Exception {
        System.out.println("Please scan the QR code for instance #" + instanceIndex + " if required and press Enter...");
        System.in.read(); // Wait for user input
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
    private static void openChat(WebDriver driver, String contactName) throws InterruptedException {
        String sendertag = "//div[div[div[span[@title='" + contactName + "' and @class='x1iyjqo2 x6ikm8r x10wlt62 x1n2onr6 xlyipyv xuxw1ft x1rg5ohu _ao3e']]]]";
        WebElement senderdiv = driver.findElement(By.xpath(sendertag));
        senderdiv.click();
        Thread.sleep(3000);

        String day = senderdiv.findElement(By.cssSelector("div[class='_ak8i']")).getText();
        System.out.println("Last message from " + contactName + " is on " + day);
    }

    // Process messages in the chat
    private static void processMessages(WebDriver driver, String contactName) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.message-in")));

        List<WebElement> validIncomingMsgs = driver.findElements(By.xpath("//div[contains(@class, 'message-in') and .//span[contains(@class, 'selectable-text')]]"));

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
        Date currentTime = new Date();
        Date fiveMinutesAgo = new Date(currentTime.getTime() - 20 * 60 * 1000);

        List<String> notifications = new ArrayList<>();

        for (WebElement messageElement : validIncomingMsgs) {
            String messageText = readMessageText(messageElement);
            String time = readMessageTimestamp(messageElement);

            if (!messageText.isEmpty()) {
                notifications.add("Message from " + contactName + ": " + messageText + " at Timestamp: " + time);
            }
        }

        displayNotifications(notifications);
    }

    // Read all text from a message
    private static String readMessageText(WebElement messageElement) {
        List<WebElement> spanElements = messageElement.findElements(By.cssSelector("span.selectable-text"));
        StringBuilder messageTextBuilder = new StringBuilder();
        for (WebElement span : spanElements) {
            messageTextBuilder.append(span.getText().trim());
        }
        return messageTextBuilder.toString().trim();
    }

    // Read the timestamp of a message
    private static String readMessageTimestamp(WebElement messageElement) {
        WebElement timestampElement = messageElement.findElement(By.cssSelector("span.x1rg5ohu.x16dsc37"));
        return timestampElement.getText().trim();
    }

    // Display notifications
    private static void displayNotifications(List<String> notifications) {
        for (String msg : notifications) {
            System.out.println(msg);
        }
    }
}
