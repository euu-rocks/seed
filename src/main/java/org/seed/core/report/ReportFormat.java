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
package org.seed.core.report;

public enum ReportFormat {
	
	EXCEL 	("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
	PDF 	("application/pdf", "pdf");
	
	public final String contentType;
	
	public final String fileType;
	
	private ReportFormat(String contentType, String fileType) {
		this.contentType = contentType;
		this.fileType = fileType;
	}

}
