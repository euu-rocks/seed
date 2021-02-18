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
package org.seed.ui.zk;

import java.util.HashMap;
import java.util.Map;

public abstract class FileTypeIcons {
	
	private final static Map<String, String> contentTypeMap = new HashMap<>();
	
	static {
		contentTypeMap.put("text/html", "file-code-o");
		contentTypeMap.put("text/xml", "file-code-o");
		contentTypeMap.put("application/pdf", "file-pdf-o");
		contentTypeMap.put("application/msword", "file-word-o");
		contentTypeMap.put("application/vnd.ms-word", "file-word-o");
		contentTypeMap.put("application/vnd.oasis.opendocument.text", "file-word-o");
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml", "file-word-o");
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "file-word-o");
		contentTypeMap.put("application/vnd.ms-excel", "file-excel-o");
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml", "file-excel-o");
		contentTypeMap.put("application/vnd.oasis.opendocument.spreadsheet", "file-excel-o");
		contentTypeMap.put("application/vnd.ms-powerpoint", "file-powerpoint-o");
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml", "file-powerpoint-o");
		contentTypeMap.put("application/vnd.oasis.opendocument.presentation", "file-powerpoint-o");
		contentTypeMap.put("application/vnd.ms-powerpoint", "file-powerpoint-o");
		contentTypeMap.put("application/gzip", "file-archive-o");
		contentTypeMap.put("application/zip", "file-archive-o");
		contentTypeMap.put("application/x-jar", "file-archive-o");
	}
	
	public static String getIcon(String contentType) {
		String icon = null;
		if (contentType != null) {
			if (contentType.startsWith("image")) {
				icon = "file-image-o";
			}
			else if (contentType.startsWith("audio")) {
				icon = "file-audio-o";
			}
			else if (contentType.startsWith("video")) {
				icon = "file-video-o";
			}
			else {
				icon = contentTypeMap.get(contentType);
			}
			if (icon == null) {
				if (contentType.startsWith("text")) {
					icon = "file-text-o";
				}
				else if (contentType.contains("archive")) {
					icon = "file-archive-o";
				}
				else {
					icon = "file-o";
				}
			}
			icon = "z-icon-" + icon + " alpha-icon-lg";
		}
		return icon;
	}
}
