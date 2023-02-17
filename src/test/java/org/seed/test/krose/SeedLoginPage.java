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
package org.seed.test.krose;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SeedLoginPage {
	
	private WebDriver driver;
	
	@FindBy(id = "username")
	private WebElement inputBenutzername;
	
	@FindBy(id = "password")
	private WebElement inputPasswort;
	
	@FindBy(className = "btn")
	private WebElement btnAnmeldung;
	
	@FindBy(xpath = "//span[contains(@class,'z-treecell-text')and contains(text(),'Abmelden')]")
	private WebElement btnAbmeldung;
	
	@FindBy(xpath = "//div[contains(@class,'z-hlayout-inner')]")
	private WebElement btnlogoffBestaetigung;
	
	@FindBy(className = "alert")
	private WebElement statusMeldung;


	public SeedLoginPage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	
	}
	
	public void zugangsdatenEingeben(String benutzername, String passwort) {
		inputBenutzername.sendKeys(benutzername);
		inputPasswort.sendKeys(passwort);
			
	}
	
	public void loginButton() {
		btnAnmeldung.click();
	}
	
	public void logoffButton() {
		btnAbmeldung.click();
	
	}
	
	public void logoffBestaetigung() {
	
		btnlogoffBestaetigung.click();
		
	}
	
	public String statusMeldungAuslesen() {
		return statusMeldung.getText();
	}
}
