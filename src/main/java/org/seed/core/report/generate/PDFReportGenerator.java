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

import org.seed.core.data.datasource.ColumnMetadata;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

class PDFReportGenerator extends AbstractReportGenerator {
	
	private final Document document;
	
	PDFReportGenerator(Report report) {
		super(report);
		document = new Document(new PdfDocument(new PdfWriter(getOutputStream())));
	}
	
	@Override
	public byte[] generate() {
		if (hasDataSources()) {
			for (ReportDataSource dataSource : getDataSources()) {
				generateTable(dataSource);
			}
		}
		document.close();
		return getBytes();
	}
	
	private void generateTable(ReportDataSource dataSource)  {
		final DataSourceResult result = getDataSourceResult(dataSource);
		final Table table = new Table(result.getColumns().size());
		final Paragraph caption = new Paragraph(dataSource.getName());
		table.setFontSize(8);
		caption.setMarginTop(10.0f);
		caption.setMarginBottom(10.0f);
		document.add(caption);
		
		// create header 
		for (ColumnMetadata column : result.getColumns()) {
			final Cell cell = new Cell();
			cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
			cell.add(new Paragraph(column.name));
			table.addCell(cell);
		}
		
		// create rows
		for (Object rowData : result.getResultList()) {
			final Object[] columnValues = getColumnValues(rowData);
			// create columns
			int colIdx = 0;
			for (ColumnMetadata column : result.getColumns()) {
				table.addCell(formatValue(columnValues[colIdx++], column));
			}
		}
		table.setMarginBottom(10.0f);
		document.add(table);
	}

}
