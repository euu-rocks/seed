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
package org.seed.core.api;

import java.io.InputStream;

/**
 * A <code>Mail</code>  is a template that can be used to create an email.
 * 
 * @author seed-master
 *
 */
public interface Mail {
	
	/**
	 * Set on ore more TO adresses
	 * @param toAddress on ore more TO adresses
	 * @return this mail
	 */
	Mail setToAddress(String ...toAddress);
	
	/**
	 * Set on ore more CC adresses
	 * @param toAddress on ore more CC adresses
	 * @return this mail
	 */
	Mail setCcAddress(String ...ccAddress);
	
	/**
	 * Set the subject of the mail
	 * @param subject the subject of the mail
	 * @return this mail
	 */
	Mail setSubject(String subject);
	
	/**
	 * Set the text of the mail
	 * @param text the text of the mail
	 * @return this mail
	 */
	Mail addText(String text);
	
	/**
	 * Adds a file attachment as <code>InputStream</code>
	 * @param fileName the file name
	 * @param contentType the content type
	 * @param inputStream the input stream
	 * @return this mail
	 */
	Mail addAttachment(String fileName, String contentType, final InputStream inputStream);
	
}
