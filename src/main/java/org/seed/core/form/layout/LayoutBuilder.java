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

import org.seed.core.util.Assert;

class LayoutBuilder {
	
	String build(LayoutElement rootElement) {
		Assert.notNull(rootElement, "rootElement");
		
		final var buf = new StringBuilder();
		build(buf, rootElement, 0);
		return buf.toString();
	}
	
	private static void build(StringBuilder buf, LayoutElement element, int depth) {
		indent(buf, depth);
		buf.append('<').append(element.getName());
		if (element.hasAttributes()) {
			for (var entry : element.getAttributes().entrySet()) {
				buf.append(' ').append(entry.getKey()).append("=\"")
				   .append(entry.getValue())
				   .append('\"');
			}
		}
		if (!element.isEmpty()) {
			buf.append('>');
			if (element.hasChildren()) {
				buf.append(System.lineSeparator());
				for (var child : element.getChildren()) {
					build(buf, child, depth + 1);
				}
				indent(buf, depth);
			}
			else if (element.getText() != null) {
				buf.append(element.getText());
			}
			buf.append("</").append(element.getName()).append('>')
			   .append(System.lineSeparator());
		}
		else {
			buf.append("/>").append(System.lineSeparator());
		}
	}
	
	private static void indent(StringBuilder buf, int depth) {
		for (int i = 0; i < depth; i++) {
			buf.append("    ");
		}
	}
	
}
