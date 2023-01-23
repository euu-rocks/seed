package org.seed.test.integration.user;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.WebElement;

@TestMethodOrder(OrderAnnotation.class)
public class EditUserGroupTest extends AbstractUserGroupTest {
	
	@Test
	@Order(1)
	void testRenameUserGroup() {
		WebElement tabpanel = showGroup("testrole");
		clearTextbox(tabpanel, "name");
		findTextbox(tabpanel, "name").sendKeys("TestroleNew");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(2)
	void testRemoveUser() {
		WebElement tabpanel = showGroup("testrolenew");
		findTab(tabpanel, "users");
		WebElement tabpanelUsers = findTabpanel(tabpanel, "users");
		pause(100);
		dragAndDrop(tabpanelUsers, "seed", "available");
		saveGroup(tabpanel);
	}
	
	@Test
	@Order(3)
	void testRemovePermission() {
		WebElement tabpanel = showGroup("testrolenew");
		clickTab(tabpanel, "permissions");
		WebElement tabpanelPermissions = findTabpanel(tabpanel, "permissions");
		pause(100);
		dragAndDrop(tabpanelPermissions, "admin-menu", "available");
		saveGroup(tabpanel);
	}
	
}
