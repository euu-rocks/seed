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

import java.time.Duration;
import java.util.Arrays;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

public abstract class AbstractIntegrationTest {
	
	private static final String TEST_BROWSER = "firefox";
	
	private static final long DELAY_AFTER_CLICK_MENU	 = 300;
	private static final long DELAY_AFTER_DRAG_AND_DROP  = 100;
	private static final long DELAY_AFTER_CLEAR_FIELD	 = 100;
	private static final long DELAY_AFTER_CLICK_LISTITEM = 100;
	
	private static final long MAX_WAIT_ELEMENT			 = 1000;
	private static final long MAX_WAIT_SUCCESS			 = 3000;
	private static final long MAX_WAIT_DISAPPEAR		 = 3000;
	
	private static final String SEED_URL  = "http://localhost:8080"; //NOSONAR
	private static final String SEED_NAME = "Seed";
	private static final String SEED_USER = "seed";
	private static final String SEED_PWD  = "seed";
	
	private static WebDriverManager driverManager;
	
	private WebDriver driver;
	
	protected AbstractIntegrationTest() {}
	
	@BeforeAll
	static void init() {
		driverManager = WebDriverManager.getInstance(TEST_BROWSER);
	}
	
	@AfterAll
	static void exit() {
		driverManager = null;
	}
	
	@BeforeEach
    void setup() {
		driver = driverManager.create();
		login();
	}
	
	@AfterEach
    void quit() {
		driver.quit();
		driver = null;
	}
	
	protected void clearOptionCombobox(WebElement parent, String className) {
		findOptionCombobox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearOptionTextbox(WebElement parent, String className) {
		findOptionTextbox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearTextbox(WebElement parent, String className) {
		findTextbox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearIntbox(WebElement parent, String className) {
		findIntbox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearLongbox(WebElement parent, String className) {
		findLongbox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearDecimalbox(WebElement parent, String className) {
		findDecimalbox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearDoublebox(WebElement parent, String className) {
		findDoublebox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearDatebox(WebElement parent, String className) {
		findDatebox(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clearNestedField(WebElement parent, String className) {
		findNestedField(parent, className).clear();
		pause(DELAY_AFTER_CLEAR_FIELD);
	}
	
	protected void clickButton(WebElement parent, String className) {
		findByClass(parent, className + "-button").click();
	}
	
	protected void clickCheckbox(WebElement parent, String className) {
		findByClass(parent, className + "-field").click();
	}
	
	protected void clickItem(WebElement parent, String className) {
		final WebElement trElement = findByClass(parent, className + "-item");
		findByClass(trElement, "z-listcell-content").click();
	}
	
	protected void clickListItem(WebElement parent, String className) {
		final WebElement trElement = findByClass(parent, className + "-listitem");
		findByClass(trElement, "z-listcell-content").click();
		pause(DELAY_AFTER_CLICK_LISTITEM);
	}
	
	protected void clickRadioItem(WebElement parent, String className) {
		findByClass(parent, className + "-radio").click();
	}
	
	protected void clickTab(WebElement parent, String className) {
		findByClass(parent, className + "-tab").click();
	}
	
	protected void clickMenu(String className) {
		findByClass(className + "-navi").click();
		pause(DELAY_AFTER_CLICK_MENU);
	}
	
	protected void dragAndDrop(WebElement parent, String itemName, String listName) {
		final WebElement itemElement = findByClass(parent, itemName + "-item");
		final WebElement listElement = findByClass(parent, listName + "-items");
		actions().dragAndDrop(itemElement, listElement).build().perform();
		pause(DELAY_AFTER_DRAG_AND_DROP);
	}
	
	protected WebElement findConfirmDialog() {
		return findByClass("z-messagebox-window");
	}
	
	protected void confirm(WebElement conformDialog) {
		final WebElement buttonElement = findByClass(conformDialog, "z-messagebox-button");
		assertEquals("Ja", buttonElement.getText());
		buttonElement.click();
	}
	
	protected WebElement findWindow(String className) {
		return findByClass(className + "-win");
	}
	
	protected WebElement findWindowHeader(WebElement window) {
		return findByClass(window, "z-window-header");
	}
	
	protected WebElement findTab(String className) {
		final WebElement liElement = findByClass(className + "-tab");
		final WebElement divElement = findByClass(liElement, "z-tab-content");
		final WebElement spanElement = findByClass(divElement, "z-tab-text");
		return spanElement;
	}
	
	protected WebElement findTab(WebElement parent, String className) {
		final WebElement liElement = findByClass(parent, className + "-tab");
		final WebElement divElement = findByClass(liElement, "z-tab-content");
		final WebElement spanElement = findByClass(divElement, "z-tab-text");
		return spanElement;
	}
	
	protected WebElement findTabpanel(String className) {
		return findByClass(className + "-tabpanel");
	}
	
	protected WebElement findTabpanel(WebElement parent, String className) {
		return findByClass(parent, className + "-tabpanel");
	}
	
	protected WebElement findCombobox(WebElement parent, String className) {
		return findField(parent, className, "z-combobox-input");
	}
	
	protected WebElement findDatebox(WebElement parent, String className) {
		return findField(parent, className, "z-datebox-input");
	}
	
	protected WebElement findDecimalbox(WebElement parent, String className) {
		return findField(parent, className, "z-decimalbox");
	}
	
	protected WebElement findDoublebox(WebElement parent, String className) {
		return findField(parent, className, "z-doublebox");
	}
	
	protected WebElement findIntbox(WebElement parent, String className) {
		return findField(parent, className, "z-intbox");
	}
	
	protected WebElement findLongbox(WebElement parent, String className) {
		return findField(parent, className, "z-longbox");
	}
	
	protected WebElement findTextbox(WebElement parent, String className) {
		return findField(parent, className, "z-textbox");
	}
	
	protected WebElement findCodeMirror(WebElement parent, String className, int line) {
		final WebElement divElement = findByClass(parent, className + "-field");
		final WebElement codeMirror = findByClass(divElement, "CodeMirror");
		line = Math.max(line, 1);
		codeMirror.findElements(By.className("CodeMirror-line")).get(line - 1).click();
		return codeMirror.findElement(By.cssSelector("textarea"));
	}
	
	protected void openToolbarCombobox(WebElement parent, String className) {
		final WebElement spanElement = findByClass(parent, className + "-toolbarcombo");
		findByClass(spanElement, "z-combobox-icon").click();
	}
	
	protected String getToolbarComboValue(WebElement parent, String className) {
		final WebElement spanElement = findByClass(parent, className + "-toolbarcombo");
		return findByClass(spanElement, "z-combobox-input").getAttribute("value");
	}
	
	protected void clickToolbarComboItem(String className) {
		findByClass(className + "-comboitem").click();
	}
	
	protected WebElement findNestedField(WebElement parent, String className) {
		return findByClass(parent, className + "-field");
	}
	
	protected WebElement findOptionTextbox(WebElement parent, String className) {
		return findOptionField(parent, className, "z-textbox");
	}
	
	protected WebElement findOptionIntbox(WebElement parent, String className) {
		return findOptionField(parent, className, "z-intbox");
	}
	
	protected WebElement findOptionCombobox(WebElement parent, String className) {
		return findOptionField(parent, className, "z-combobox-input");
	}
	
	protected WebElement findErrorMessage() {
		return findByClass("z-icon-times-circle");
	}
	
	protected WebElement findSuccessMessage() {
		return new WebDriverWait(driver, Duration.ofMillis(MAX_WAIT_SUCCESS))
					.until(driver -> driver.findElement(By.className("z-icon-info-circle")));
	}
	
	protected WebElement findValidationMessage() {
		return findByClass("z-icon-exclamation-circle");
	}
	
	protected void openMenu(String className) {
		final WebElement tdElement = findByClass(className + "-navi");
		final WebElement divElement = findByClass(tdElement, "z-treecell-content");
		final WebElement spanElement = findByClass(divElement, "z-tree-icon");
		spanElement.click();
	}
	
	protected void waitWindowDisappear(String className) {
		waitElementDisappear(className + "-win");
	}
	
	protected void waitTabDisappear(String className) {
		waitElementDisappear(className + "-tab");
	}
	
	protected void waitConfirmDialogDisappear() {
		waitElementDisappear("z-messagebox-window");
	}
	
	protected static Keys[] repeatKey(Keys key, int count) {
		final Keys[] array = new Keys[count];
		Arrays.fill(array, key);
		return array;
	}
	
	protected static void pause(long ms) {
		try {
			Thread.sleep(ms);
		} 
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void waitElementDisappear(String className) {
		new WebDriverWait(driver, Duration.ofMillis(MAX_WAIT_DISAPPEAR))
					.until(ExpectedConditions.invisibilityOfElementLocated(By.className(className)));
	}
	
	private WebElement findByClass(String className) {
		return new WebDriverWait(driver, Duration.ofMillis(MAX_WAIT_ELEMENT))
					.until(driver -> driver.findElement(By.className(className)));
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
        assertEquals(SEED_NAME, driver.getTitle());
	}
	
	private Actions actions() {
		return new Actions(driver);
	}
	
	private static WebElement findByClass(WebElement parent, String className) {
		return parent.findElement(By.className(className));
	}
	
	private static WebElement findField(WebElement parent, String className, String fieldClass) {
		final WebElement tdElement = findByClass(parent, className + "-fieldcell");
		return findByClass(tdElement, fieldClass);
	}
	
	private static WebElement findOptionField(WebElement parent, String className, String fieldClass) {
		final WebElement divElement = findByClass(parent, className + "-field");
		return findByClass(divElement, fieldClass);
	}
	
}
