package org.seed.test.integration.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditUserTest extends AbstractUserTest {
	
	@Test
	@Order(1)
	void testRenameUser() {
		WebElement tabpanel = showUser("testuser");
		assertEquals("Benutzer", findTab("benutzer").getText());
		clearTextbox(tabpanel, "username");
		findTextbox(tabpanel, "username").sendKeys("TestuserNew");
		saveUser(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveRole() {
		WebElement tabpanel = showUser("testusernew");
		findTab(tabpanel, "usergroups");
		WebElement tabpanelUsergroups = findTabpanel(tabpanel, "usergroups");
		pause(100);
		dragAndDrop(tabpanelUsergroups, "testrole", "available");
		saveUser(tabpanel);
	}
	
}
