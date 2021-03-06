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
package org.seed.core.form.layout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

abstract class LayoutParser {
	
	private final static SAXParserFactory PARSER_FACTORY = SAXParserFactory.newInstance();
	
	public static LayoutElement parse(String content) throws SAXException, IOException, ParserConfigurationException {
		Assert.notNull(content, "content is null");
		return parse(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	}
	
	public static LayoutElement parse(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {
		Assert.notNull(inputStream, "inputStream is null");
		
		final LayoutHandler layoutHandler = new LayoutHandler();
		PARSER_FACTORY.newSAXParser().parse(inputStream, layoutHandler);
		return layoutHandler.rootElement;
	}
	
	private static class LayoutHandler extends DefaultHandler {
		
		private final Deque<LayoutElement> stack = new ArrayDeque<>();
		
		private LayoutElement rootElement;
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) 
				throws SAXException {
			final LayoutElement element = new LayoutElement(qName, attributes);
			if (rootElement == null) {
				rootElement = element;
			}
			else {
				stack.peek().addChild(element);
			}
			stack.push(element);
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			final String text = String.valueOf(ch, start, length);
			if (StringUtils.hasText(text)) {
				stack.peek().setText(text);
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			stack.pop();
		}
		
	}
	
}
