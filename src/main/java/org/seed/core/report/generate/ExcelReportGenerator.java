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
import java.sql.Types;
import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.seed.InternalException;
import org.seed.core.data.datasource.ColumnMetadata;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;

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
	
	private static void setCellValue(ColumnMetadata column, Cell cell, Object columnValue) {
		switch (column.type) {
			case Types.BLOB:
				final byte[] bytes = (byte[]) columnValue;
				if (bytes != null) {
					cell.setCellValue(bytes.length + " bytes");
				}
				break;
			case Types.BOOLEAN:
				cell.setCellValue((boolean) columnValue);
				break;
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.DOUBLE:
				final Double doubleValue = (Double) columnValue;
				if (doubleValue != null) {
					cell.setCellValue(doubleValue);
				}
				break;
			case Types.DECIMAL:
				final BigDecimal decimal = (BigDecimal) columnValue;
				if (decimal != null) {
					cell.setCellValue(decimal.doubleValue());
				}
				break;
			case Types.DATE:
				final Date dateValue = (Date) columnValue;
				if (dateValue != null) {
					cell.setCellValue((Date) columnValue);
				}
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

}
