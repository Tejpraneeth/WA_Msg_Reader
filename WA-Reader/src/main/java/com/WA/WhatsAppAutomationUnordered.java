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
import java.util.Scanner;



public class WhatsAppAutomationUnordered {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Enter the number of WhatsApp instances to run:");
            int instanceCount = 1;

            // Loop through the number of instances
            for (int i = 1; i <=instanceCount; i++) {
                System.out.println("Starting instance #" + i);

                // Set up Chrome driver with a unique user profile
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--user-data-dir=C:/Users/tejpr/AppData/Local/Google/Chrome/User Data/WhatsAppProfile" + i);
                options.addArguments("--profile-directory=Profile" + i);

                // Start the browser with the user profile
                WebDriver driver = new ChromeDriver(options);

                // Open WhatsApp Web
                driver.get("https://web.whatsapp.com/");

                // Wait for user to log in if it's the first time
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
                System.out.println("Please scan the QR code for instance #" + i + " if required and press Enter...");
                System.in.read(); // Wait for user input

                // All the variables and components

                String contactName = "Air India";
                String chatsGrid="//div[@role='grid']";
                String GridScroller="//*[@id=\"pane-side\"]";
                // Wait until the chat list is visible
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(chatsGrid)));
                String sendertag="//div[div[div[span[@title='" + contactName + "' and  @class='x1iyjqo2 x6ikm8r x10wlt62 x1n2onr6 xlyipyv xuxw1ft x1rg5ohu _ao3e']]]]";
                WebElement senderdiv= driver.findElement(By.xpath(sendertag));

                String day=senderdiv.findElement(By.cssSelector("div[class='_ak8i']")).getText();




                // Scroll until the contact is found
                boolean contactFound = false;


                while (!contactFound) {
                    try {
                        // Try to find the contact name
                        driver.findElement(By.xpath(sendertag));
                        contactFound = true;  // If found, exit the loop
                    } catch (Exception e) {
                        // If not found, try scrolling the container via Chat grid using JavaScript
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        js.executeScript("arguments[0].scrollTop = arguments[0].scrollTop + arguments[0].clientHeight",
                                driver.findElement(By.xpath(GridScroller)));

                        // Wait a bit before scrolling again to ensure it loads
                        Thread.sleep(1);

                        // If JavaScript scroll is still not working, use keyboard scroll as a fallback
                        if (!contactFound) {
                            WebDriverWait waitKeyboard = new WebDriverWait(driver, Duration.ofSeconds(2));
                            waitKeyboard.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(chatsGrid)));
                            Actions actions = new Actions(driver);
                            actions.sendKeys(Keys.PAGE_DOWN).perform();
                            Thread.sleep(1000);  // Wait for the list to load
                        }
                    }
                }

                // Open the chat by clicking on the sender
                senderdiv.click();
                Thread.sleep(3000);
                System.out.println("Last message from Air India is on " + day);



                // Read all valid incoming messages (pure text only) from latest to oldest
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.message-in")));

                // Directly find valid incoming messages using XPath
                List<WebElement> validIncomingMsgs = driver.findElements(By.xpath("//div[contains(@class, 'message-in') and .//span[contains(@class, 'selectable-text')]]"));



                SimpleDateFormat sdf = new SimpleDateFormat("h:mm a"); // Format for parsing timestamp
                Date currentTime = new Date(); // Current time
                //int desiredwindow =5;
                Date fiveMinutesAgo = new Date(currentTime.getTime() - 20 * 60 * 1000); // 5 minutes ago

                int messageIndex = validIncomingMsgs.size() - 1;
                List<String> Notifications = new ArrayList<>();
                // Process messages in reverse order (latest to oldest)
                for (; messageIndex >= 0; messageIndex--) {
                    WebElement messageElement = validIncomingMsgs.get(messageIndex);
                    // Find all nested spans within the message
                    List<WebElement> spanElements = messageElement.findElements(By.cssSelector("span.selectable-text"));

// Append the text from each span into a single string
                    StringBuilder messageTextBuilder = new StringBuilder();
                    for (WebElement span : spanElements) {
                        messageTextBuilder.append(span.getText().trim());
                    }

// Combine all the span texts into a single message
                    String messageText = messageTextBuilder.toString().trim();


                    // Extract the timestamp
                    WebElement timestampElement = messageElement.findElement(By.cssSelector("span.x1rg5ohu.x16dsc37"));
                    String time = timestampElement.getText().trim();


                        // Parse the timestamp
//                     Date messageTime = sdf.parse(time);
//                        // Check if the message is outside the last 5 minutes
//                        if (messageTime.before(currentTime) ){
//                            break; // Stop processing older messages
//                        }

                        // Add to notifications if within the range
                        if (!messageText.isEmpty()) {
                            Notifications.add("Message from " + contactName + ": " + messageText + " at Timestamp: " + time);
                        }

                }


                //Got all the notifications into a new list (Notifications)

                for (String msg : Notifications) {
                    System.out.println(msg);
                }


                // Quit the driver after finishing the task for this instance
                driver.quit();



            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }}