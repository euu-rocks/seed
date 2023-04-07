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

/**
 * <code>MailProvider</code> provides access to e-mail functionalities.
 * 
 * @author seed-master
 *
 */
public interface MailProvider {
	
	/**
	 * Checks if mailing is enabled
	 * @return <code>true</code> if mailing is enabled
	 */
	boolean isMailingEnabled();
	
	/**
	 * Creates a new {@link Mail} template.
	 * @return a new mail template
	 */
	Mail createMail();
	
	/**
	 * Sends an email
	 * @param mail the mail to send
	 */
	void sendMail(Mail mail);
	
}
