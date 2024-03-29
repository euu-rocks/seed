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

import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seed.C;
import org.seed.LabelProvider;
import org.seed.Seed;
import org.seed.core.data.datasource.ColumnMetadata;
import org.seed.core.data.datasource.DataSourceResult;
import org.seed.core.report.Report;
import org.seed.core.report.ReportDataSource;
import org.seed.core.report.ReportGenerator;
import org.seed.core.util.Assert;

import org.springframework.util.FastByteArrayOutputStream;

abstract class AbstractReportGenerator implements ReportGenerator {
	
	private final Map<Long, DataSourceResult> resultMap = new HashMap<>();
	
	private final LabelProvider labelProvider;
	
	private final Report report;
	
	private FastByteArrayOutputStream outputStream;
	
	protected AbstractReportGenerator(Report report) {
		Assert.notNull(report, C.REPORT);
		
		this.report = report;
		labelProvider = Seed.getBean(LabelProvider.class);
	}
	
	@Override
	public void addDataSourceResult(ReportDataSource dataSource, DataSourceResult result) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.notNull(result, C.RESULT);
		
		resultMap.put(dataSource.getId(), result);
	}
	
	protected DataSourceResult getDataSourceResult(ReportDataSource dataSource) {
		Assert.notNull(dataSource, C.DATASOURCE);
		Assert.state(resultMap.containsKey(dataSource.getId()), 
					 "result not available for datasource " + dataSource.getId());
		
		return resultMap.get(dataSource.getId());
	}
	
	protected boolean hasDataSources() {
		return report.hasDataSources();
	}
	
	protected List<ReportDataSource> getDataSources() {
		return report.getDataSources();
	}
	
	protected String formatValue(Object object, ColumnMetadata column) {
		Assert.notNull(column, "column");
		
		switch (column.type) {
			case Types.BLOB:
				final byte[] bytes = (byte[]) object;
				return bytes != null 
						? bytes.length + " bytes"
						: "<empty>";
			case Types.BOOLEAN:
				return labelProvider.formatBoolean((Boolean) object);
			case Types.DECIMAL:
				return labelProvider.formatBigDecimal((BigDecimal) object);
			case Types.DATE:
				return labelProvider.formatDate((Date) object);
			default:
				return object != null ? object.toString() : "";
		}
	}
	
	protected OutputStream getOutputStream() {
		outputStream = new FastByteArrayOutputStream();
		return outputStream;
	}
	
	protected byte[] getBytes() {
		Assert.stateAvailable(outputStream, "output stream");
		return outputStream.toByteArray();
	}
	
	protected static Object[] getColumnValues(Object rowData) {
		return rowData != null && rowData.getClass().isArray() 
				? (Object[]) rowData 
				: new Object[] { rowData };
	}

}
