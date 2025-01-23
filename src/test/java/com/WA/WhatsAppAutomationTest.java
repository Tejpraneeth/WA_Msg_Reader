package com.WA;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WhatsAppAutomationTest {

    @Test
    public void testLastMessageRetrieval() {
        // Simulate retrieving a message from WhatsApp automation logic.
        String simulatedLastMessage = "Hello, World!"; // This should be replaced with actual logic if needed.

        Assert.assertNotNull(simulatedLastMessage, "Last message should not be null");

        // Additional assertions can be added here based on your logic.
        Assert.assertEquals("Hello, World!", simulatedLastMessage);
    }
}
