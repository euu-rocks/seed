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
	
	private static final String TYPE_IMAGE      = "image";
	private static final String TYPE_AUDIO      = "audio";
	private static final String TYPE_VIDEO      = "video";
	private static final String TYPE_TEXT       = "text";
	private static final String TYPE_ARCHIVE    = "archive";
	
	private static final String ICON_ARCHIVE    = "file-archive-o";
	private static final String ICON_AUDIO      = "file-audio-o";
	private static final String ICON_CODE       = "file-code-o";
	private static final String ICON_EXCEL      = "file-excel-o";
	private static final String ICON_FILE       = "file-o";
	private static final String ICON_IMAGE      = "file-image-o";
	private static final String ICON_PDF        = "file-pdf-o";
	private static final String ICON_POWERPOINT = "file-powerpoint-o";
	private static final String ICON_TEXT       = "file-text-o";
	private static final String ICON_VIDEO      = "file-video-o";
	private static final String ICON_WORD       = "file-word-o";
	
	private static final String PRE_ICON        = "z-icon-";
	private static final String SUF_ICON        = " alpha-icon-lg";
	
	private static final Map<String, String> contentTypeMap = new HashMap<>(32);
	
	static {
		contentTypeMap.put("text/html", ICON_CODE);
		contentTypeMap.put("text/xml", ICON_CODE);
		contentTypeMap.put("application/pdf", ICON_PDF);
		contentTypeMap.put("application/msword", ICON_WORD);
		contentTypeMap.put("application/vnd.ms-word", ICON_WORD);
		contentTypeMap.put("application/vnd.oasis.opendocument.text", ICON_WORD);
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml", ICON_WORD);
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ICON_WORD);
		contentTypeMap.put("application/vnd.ms-excel", ICON_EXCEL);
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.spreadsheetml", ICON_EXCEL);
		contentTypeMap.put("application/vnd.oasis.opendocument.spreadsheet", ICON_EXCEL);
		contentTypeMap.put("application/vnd.ms-powerpoint", ICON_POWERPOINT);
		contentTypeMap.put("application/vnd.openxmlformats-officedocument.presentationml", ICON_POWERPOINT);
		contentTypeMap.put("application/vnd.oasis.opendocument.presentation", ICON_POWERPOINT);
		contentTypeMap.put("application/vnd.ms-powerpoint", ICON_POWERPOINT);
		contentTypeMap.put("application/gzip", ICON_ARCHIVE);
		contentTypeMap.put("application/zip", ICON_ARCHIVE);
		contentTypeMap.put("application/x-jar", ICON_ARCHIVE);
	}
	
	private FileTypeIcons() {}
	
	public static String getIcon(String contentType) {
		String icon = null;
		if (contentType != null) {
			if (contentType.startsWith(TYPE_IMAGE)) {
				icon = ICON_IMAGE;
			}
			else if (contentType.startsWith(TYPE_AUDIO)) {
				icon = ICON_AUDIO;
			}
			else if (contentType.startsWith(TYPE_VIDEO)) {
				icon = ICON_VIDEO;
			}
			else {
				icon = contentTypeMap.get(contentType);
			}
			if (icon == null) {
				if (contentType.startsWith(TYPE_TEXT)) {
					icon = ICON_TEXT;
				}
				else if (contentType.contains(TYPE_ARCHIVE)) {
					icon = ICON_ARCHIVE;
				}
				else {
					icon = ICON_FILE;
				}
			}
			icon = PRE_ICON + icon + SUF_ICON;
		}
		return icon;
	}
	
}
