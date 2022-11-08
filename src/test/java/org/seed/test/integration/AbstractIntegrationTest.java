/**
 * Seed
 * Copyright (C) 2021 EUU⛰ROCKS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.seed.test.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class AbstractIntegrationTest {
	
	private static final String TEST_BROWSER = "firefox";
	
	private static final long DELAY_AFTER_LOGIN			= 1000;
	private static final long DELAY_AFTER_CLICK_MENU	= 300;
	private static final long DELAY_AFTER_OPEN_MENU		= 100;
	private static final long DELAY_AFTER_CLICK_BUTTON	= 100;
	private static final long DELAY_AFTER_DRAG_AND_DROP	= 100;
	
	private static final String SEED_URL  = "http://localhost:8080"; //NOSONAR
	private static final String SEED_NAME = "Seed";
	private static final String SEED_USER = "seed";
	private static final String SEED_PWD  = "seed";
	
	private static WebDriverManager manager = WebDriverManager.getInstance(TEST_BROWSER);
	
	private WebDriver driver;
	
	@BeforeEach
    void setup() {
		driver = manager.create();
		login();
	}
	
	@AfterEach
    void quit() {
		driver.quit();
	}
	
	protected void clickButton(WebElement parent, String className) {
		parent.findElement(By.className(className + "-button")).click();
		pause(DELAY_AFTER_CLICK_BUTTON);
	}
	
	protected void clickListItem(WebElement parent, String className) {
		parent.findElement(By.className(className + "-listitem")).click();
	}
	
	protected void clickOptionCheckbox(WebElement parent, String className) {
		parent.findElement(By.className(className + "-field")).click();
	}
	
	protected void clickTab(WebElement parent, String className) {
		parent.findElement(By.className(className + "-tab")).click();
	}
	
	protected void clickMenu(String className) {
		findByClass(className + "-navi").click();
		pause(DELAY_AFTER_CLICK_MENU);
	}
	
	protected void openMenu(String className) {
		final WebElement tdElement = findByClass(className + "-navi");
		final WebElement divElement = tdElement.findElement(By.className("z-treecell-content"));
		final WebElement spanElement = divElement.findElement(By.className("z-tree-icon"));
		spanElement.click();
		pause(DELAY_AFTER_OPEN_MENU);
	}
	
	protected WebElement findWindow(String className) {
		return findByClass(className + "-win");
	}
	
	protected WebElement findWindowHeader(WebElement window) {
		return window.findElement(By.className("z-window-header")); 
	}
	
	protected WebElement findTab(String className) {
		final WebElement liElement = findByClass(className + "-tab");
		final WebElement divElement = liElement.findElement(By.className("z-tab-content"));
		final WebElement spanElement = divElement.findElement(By.className("z-tab-text"));
		return spanElement;
	}
	
	protected WebElement findTab(WebElement parent, String className) {
		final WebElement liElement = parent.findElement(By.className(className + "-tab"));
		final WebElement divElement = liElement.findElement(By.className("z-tab-content"));
		final WebElement spanElement = divElement.findElement(By.className("z-tab-text"));
		return spanElement;
	}
	
	protected WebElement findTabpanel(String className) {
		return findByClass(className + "-tabpanel");
	}
	
	protected WebElement findTabpanel(WebElement parent, String className) {
		return parent.findElement(By.className(className + "-tabpanel"));
	}
	
	protected WebElement findDatebox(WebElement parent, String className) {
		final WebElement tdElement = parent.findElement(By.className(className + "-fieldcell"));
		final WebElement inputElement = tdElement.findElement(By.className("z-datebox-input"));
		return inputElement;
	}
	
	protected WebElement findTextbox(WebElement parent, String className) {
		final WebElement tdElement = parent.findElement(By.className(className + "-fieldcell"));
		final WebElement inputElement = tdElement.findElement(By.className("z-textbox"));
		return inputElement;
	}
	
	protected WebElement findCodeMirror(WebElement parent, String className, int line) {
		final WebElement divElement = parent.findElement(By.className(className + "-field"));
		final WebElement codeMirror = divElement.findElement(By.className("CodeMirror"));
		codeMirror.findElements(By.className("CodeMirror-line")).get(line-1).click();
		return codeMirror.findElement(By.cssSelector("textarea"));
	}
	
	protected WebElement findOptionTextbox(WebElement parent, String className) {
		final WebElement divElement = parent.findElement(By.className(className + "-field"));
		final WebElement inputElement = divElement.findElement(By.className("z-textbox"));
		return inputElement; 
	}
	
	protected WebElement findOptionIntbox(WebElement parent, String className) {
		final WebElement divElement = parent.findElement(By.className(className + "-field"));
		final WebElement inputElement = divElement.findElement(By.className("z-intbox"));
		return inputElement; 
	}
	
	protected WebElement findOptionCombobox(WebElement parent, String className) {
		final WebElement divElement = parent.findElement(By.className(className + "-field"));
		final WebElement inputElement = divElement.findElement(By.className("z-combobox-input"));
		return inputElement; 
	}
	
	protected WebElement findSuccessMessage() {
		return findByClass("z-icon-info-circle");
	}
	
	protected WebElement findValidationMessage() {
		return findByClass("z-icon-exclamation-circle");
	}
	
	protected void dragAndDrop(WebElement parent, String itemName, String listName) {
		final WebElement itemElement = parent.findElement(By.className(itemName + "-item"));
		final WebElement listElement = parent.findElement(By.className(listName + "-items"));
		new Actions(driver).dragAndDrop(itemElement, listElement).build().perform();
		pause(DELAY_AFTER_DRAG_AND_DROP);
	}
	
	protected void pause(long ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private WebElement findByClass(String className) {
		return driver.findElement(By.className(className));
	}
	
	private WebElement findById(String id) {
		return driver.findElement(By.id(id));
	}
	
	private void login() {
		driver.get(SEED_URL);
		assertEquals("Please sign in", driver.getTitle());
		
		findById("username").sendKeys(SEED_USER);
        findById("password").sendKeys(SEED_PWD);
        findByClass("btn").click();
        
        pause(DELAY_AFTER_LOGIN);
        assertEquals(SEED_NAME, driver.getTitle());
	}
	
}
