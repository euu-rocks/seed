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
 * <code>RestProvider</code> provides access to {@link RestClient} instances.
 * 
 * @author seed-master
 *
 */
public interface RestProvider {
	
	/**
	 * Creates a new {@link RestClient} for the given url
	 * @param url the url to use
	 * @return the new <code>RestClient</code>
	 */
	RestClient getClient(String url);
	
}
