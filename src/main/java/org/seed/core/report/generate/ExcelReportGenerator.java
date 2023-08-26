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
package org.seed.core.report.generate;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.seed.InternalException;
import org.seed.core.config.SystemLog;
import org.seed.core.data.datasource.ColumnMetadata;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;
import org.seed.core.util.MiscUtils;

class ExcelReportGenerator extends AbstractReportGenerator {
	
	private final Workbook workbook;
	
	ExcelReportGenerator(Report report) {
		super(report);
		workbook = new XSSFWorkbook();
	}
	
	@Override
	public byte[] generate() {
		try {
			if (hasDataSources()) {
				for (ReportDataSource dataSource : getDataSources()) {
					generateSheet(dataSource);
				}
			}
			workbook.write(getOutputStream());
			return getBytes();
		}
		catch (IOException ioex) {
			SystemLog.logError(ioex);
			throw new InternalException(ioex);
		}
		finally {
			try {
				workbook.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	private void generateSheet(ReportDataSource dataSource) {
		int rowIdx = 0;
		int colIdx = 0;
		
		final DataSourceResult result = getDataSourceResult(dataSource);
		final Sheet sheet = workbook.createSheet(dataSource.getName());
		final Row header = sheet.createRow(rowIdx++);
		
		// create header 
		for (ColumnMetadata column : result.getColumns()) {
			final Cell cell = header.createCell(colIdx++);
			cell.setCellValue(column.name);
		}
		
		// create rows
		for (Object rowData : result.getResultList()) {
			final Object[] columnValues = getColumnValues(rowData);
			final Row row = sheet.createRow(rowIdx++);
			colIdx = 0;
			
			// create columns
			for (ColumnMetadata column : result.getColumns()) {
				final Cell cell = row.createCell(colIdx);
				setCellValue(column, cell, columnValues[colIdx]);
				colIdx++;
			}
		}
	}
	
	private void setCellValue(ColumnMetadata column, Cell cell, Object columnValue) {
		if (columnValue == null) {
			return;
		}
		switch (column.type) {
			case Types.BLOB:
				final byte[] bytes = (byte[]) columnValue;
				cell.setCellValue(MiscUtils.formatMemorySize(bytes.length));
				break;
			
			case Types.BIT:
			case Types.BOOLEAN:
				cell.setCellValue((Boolean) columnValue);
				break;
				
			case Types.SMALLINT:
			case Types.INTEGER:
				cell.setCellValue((Integer) columnValue);
				break;
				
			case Types.BIGINT:
				cell.setCellValue(((BigInteger) columnValue).doubleValue());
				break;
				
			case Types.DOUBLE:
				cell.setCellValue((Double) columnValue);
				break;
				
			case Types.DECIMAL:
				cell.setCellValue(((BigDecimal) columnValue).doubleValue());
				break;
				
			case Types.DATE:
				formatCell(cell, CELL_FORMAT_DATE);
				cell.setCellValue((Date) columnValue);
				break;
			
			case Types.TIMESTAMP:
				formatCell(cell, CELL_FORMAT_DATETIME);
				cell.setCellValue(((Timestamp) columnValue).toLocalDateTime());
				break;
				
			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				cell.setCellValue(columnValue.toString());
				break;
				
			default:
				throw new IllegalStateException("unhandled type " + column.type);
		}
	}
	
	private void formatCell(Cell cell, short format) {
		final CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(format);
		cell.setCellStyle(cellStyle);
	}
	
	private static final short CELL_FORMAT_DATE 	= 14;
	private static final short CELL_FORMAT_DATETIME = 22;
	
	/*  0 = "General"
		1 = "0"
		2 = "0.00"
		3 = "#,##0"
		4 = "#,##0.00"
		5 = ""$"#,##0_);("$"#,##0)"
		6 = ""$"#,##0_);Red"
		7 = ""$"#,##0.00_);("$"#,##0.00)"
		8 = ""$"#,##0.00_);Red"
		9 = "0%"
		10 = "0.00%"
		11 = "0.00E+00"
		12 = "# ?/?"
		13 = "# ??/??"
		14 = "m/d/yy"
		15 = "d-mmm-yy"
		16 = "d-mmm"
		17 = "mmm-yy"
		18 = "h:mm AM/PM"
		19 = "h:mm:ss AM/PM"
		20 = "h:mm"
		21 = "h:mm:ss"
		22 = "m/d/yy h:mm"
		23-36 = reserved
		37 = "#,##0_);(#,##0)"
		38 = "#,##0_);Red"
		39 = "#,##0.00_);(#,##0.00)"
		40 = "#,##0.00_);Red"
		41 = "(* #,##0);(* (#,##0);(* "-");(@)"
		42 = "("$"* #,##0_);("$"* (#,##0);("$"* "-");(@)"
		43 = "(* #,##0.00_);(* (#,##0.00);(* "-"??);(@)"
		44 = "("$"* #,##0.00_);("$"* (#,##0.00);("$"* "-"??);(@_)"
		45 = "mm:ss"
		46 = "[h]:mm:ss"
		47 = "mm:ss.0"
		48 = "##0.0E+0"
		49 = "@" 
	*/
	
}
