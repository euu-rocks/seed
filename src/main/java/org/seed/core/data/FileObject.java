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
package org.seed.core.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.util.ObjectUtils;

@Entity
@Table(name = "sys_file")
public class FileObject extends AbstractSystemEntity {
	
	private String contentType;
	
	private byte[] content;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public boolean isEmpty() {
		return ObjectUtils.isEmpty(content);
	}
	
	public FileObject copy() {
		final FileObject copy = new FileObject();
		copy.setName(getName());
		copy.contentType = contentType;
		if (content != null) {
			copy.content = new byte[content.length];
			System.arraycopy(content, 0, copy.content, 0, content.length);
		}
		return copy;
	}
	
}
