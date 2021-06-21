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

import org.seed.InternalException;
import org.seed.core.data.datasource.ColumnMetadata;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PDFReportGenerator extends AbstractReportGenerator {
	
	private final Document document;
	
	PDFReportGenerator(Report report) {
		super(report);
		document = new Document();
	}
	
	@Override
	public byte[] generate() {
		try {
			PdfWriter.getInstance(document, getOutputStream());
			document.open();
			if (hasDataSources()) {
				for (ReportDataSource dataSource : getDataSources()) {
					generateTable(dataSource);
				}
			}
			document.close();
		} 
		catch (DocumentException dex) {
			throw new InternalException(dex);
		}
		return getBytes();
	}
	
	private void generateTable(ReportDataSource dataSource) throws DocumentException {
		final DataSourceResult result = getDataSourceResult(dataSource);
		final PdfPTable table = new PdfPTable(result.getColumns().size());
		final Paragraph caption = new Paragraph(dataSource.getName());
		caption.setSpacingBefore(10.0f);
		caption.setSpacingAfter(10.0f);
		document.add(caption);
		
		// create header 
		for (ColumnMetadata column : result.getColumns()) {
			final PdfPCell headerCell = new PdfPCell();
			headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headerCell.setPhrase(new Phrase(column.name));
	        table.addCell(headerCell);
		}
		
		// create rows
		for (Object rowData : result.getResultList()) {
			final Object[] columnValues = getColumnValues(rowData);
			// create columns
			int colIdx = 0;
			for (ColumnMetadata column : result.getColumns()) {
				final PdfPCell cell = new PdfPCell();
				cell.setPhrase(new Phrase(formatValue(columnValues[colIdx++], column)));
				table.addCell(cell);
			}
		}
		table.setSpacingAfter(10.0f);
		document.add(table);
	}

}
