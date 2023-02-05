package org.seed.test.integration.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class DeleteJobTest extends AbstractJobTest {
	
	@Test
	@Order(1)
	void testRemoveNotification() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Benachrichtigungen", findTab(tabpanel, "notifications").getText());
		clickTab(tabpanel, "notifications");
		WebElement tabpanelNotifications = findTabpanel(tabpanel, "notifications");
		clickListItem(tabpanelNotifications, "testusernew");
		clickButton(tabpanelNotifications, "remove");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveParameter() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Parameter", findTab(tabpanel, "parameters").getText());
		clickTab(tabpanel, "parameters");
		WebElement tabpanelParameters = findTabpanel(tabpanel, "parameters");
		clickListItem(tabpanelParameters, "testparameter");
		clickButton(tabpanelParameters, "remove");
		saveJob(tabpanel);
	}
	
	@Test
	@Order(3)
	void testDeleteJob() {
		WebElement tabpanel = showJob("testjobnew");
		assertEquals("Jobs", findTab("jobs").getText());
		clickButton(tabpanel, "delete");
		
		WebElement dialogConfirm = findConfirmDialog();
		confirm(dialogConfirm);
		waitConfirmDialogDisappear();
		waitTabDisappear("jobruns");
	}
	
}
