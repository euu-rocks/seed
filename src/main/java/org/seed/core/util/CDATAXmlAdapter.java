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
package org.seed.core.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.springframework.util.Assert;

public class CDATAXmlAdapter extends XmlAdapter<String, String> {
	
	private final static String CDATA_BEGIN = "<![CDATA[";
	private final static String CDATA_END 	= "]]>";
	
	@Override
	public String marshal(String value) throws Exception {
		return value != null ? CDATA_BEGIN + value + CDATA_END : null;
	}

	@Override
	public String unmarshal(String value) throws Exception {
		Assert.state(value == null || 
					(value.startsWith(CDATA_BEGIN) && value.endsWith(CDATA_END)), "expected CDATA");
		
		return value != null 
				? value.substring(CDATA_BEGIN.length(), value.length() - CDATA_END.length()) 
				: null;
	}

}
