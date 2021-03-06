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
package org.seed.core.entity.transfer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.MappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

class CSVProcessor extends AbstractTransferProcessor {
	
	CSVProcessor(ValueObjectService valueObjectService,
				 Class<? extends ValueObject> objectClass,
				 Transfer transfer) {
		super(valueObjectService, objectClass, transfer);
	}
	
	@Override
	public byte[] doExport() {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (PrintWriter writer = new PrintWriter(out, false, getCharset())) {
			final StatefulBeanToCsvBuilder<ValueObject> builder = 
				new StatefulBeanToCsvBuilder<ValueObject>(writer)
					.withMappingStrategy(createMappingStrategy())
					.withApplyQuotesToAll(getTransfer().isQuoteAll());
			if (getTransfer().getNewline() != null) {
				builder.withLineEnd(getTransfer().getNewline().content);
			}
			if (getTransfer().getSeparatorChar() != null) {
				builder.withSeparator(getTransfer().getSeparatorChar().charAt(0));
			}
			if (getTransfer().getQuoteChar() != null) {
				builder.withQuotechar(getTransfer().getQuoteChar().charAt(0));
			}
			if (getTransfer().getEscapeChar() != null) {
				builder.withEscapechar(getTransfer().getEscapeChar().charAt(0));
			}
			
			final StatefulBeanToCsv<ValueObject> beanToCsv = builder.build();
			if (getTransfer().isHeader()) {
				writeHeader(writer);
			}
			while (hasNextObject()) {
				beanToCsv.write(getNextObject());
			}
		} 
		catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}
	
	@Override
	public TransferResult doImport(ImportOptions options, InputStream inputStream) throws ValidationException {
		final TransferResult result = new TransferResult(options);
		Reader reader = null;
		try {
			reader = new InputStreamReader(inputStream, getCharset());
			final CsvToBeanBuilder<ValueObject> builder  =
					new CsvToBeanBuilder<ValueObject>(reader)
						.withType(getObjectClass())
						.withMappingStrategy(createMappingStrategy())
						.withIgnoreLeadingWhiteSpace(true)
						.withIgnoreEmptyLine(true);
			if (getTransfer().isHeader()) {
				builder.withSkipLines(1);
			}
			if (getTransfer().getSeparatorChar() != null) {
				builder.withSeparator(getTransfer().getSeparatorChar().charAt(0));
			}
			if (getTransfer().getQuoteChar() != null) {
				builder.withQuoteChar(getTransfer().getQuoteChar().charAt(0));
			}
			if (getTransfer().getEscapeChar() != null) {
				builder.withEscapeChar(getTransfer().getEscapeChar().charAt(0));
			}
			saveObjects(builder.build().parse(), options, result);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} 
			catch (Exception ex) {}
		}
		return result;
	}
	
	private MappingStrategy<ValueObject> createMappingStrategy() {
		final ColumnPositionMappingStrategy<ValueObject> mappingStrategy = 
				new ColumnPositionMappingStrategy<>();
		mappingStrategy.setType(getObjectClass());
		if (getTransfer().hasElements()) {
			final List<String> fieldNames = new ArrayList<>();
			for (TransferElement element : getTransfer().getElements()) {
				fieldNames.add(element.getEntityField().getInternalName());
			}
			mappingStrategy.setColumnMapping(fieldNames.toArray(new String[fieldNames.size()]));
		}
		return mappingStrategy;
	}
	
	private void writeHeader(PrintWriter writer) {
		if (getTransfer().hasElements()) {
			final char separator = getTransfer().getSeparatorChar() != null 
										? getTransfer().getSeparatorChar().charAt(0)
										: ICSVWriter.DEFAULT_SEPARATOR;
			final char quote = getTransfer().getQuoteChar() != null 
										? getTransfer().getQuoteChar().charAt(0)
										: ICSVWriter.DEFAULT_QUOTE_CHARACTER;
			boolean first = true;
			for (TransferElement element : getTransfer().getElements()) {
				if (first) {
					first = false;
				}
				else {
					writer.write(separator);
				}
				if (getTransfer().isQuoteAll()) {
					writer.write(quote);
				}
				writer.write(element.getEntityField().getInternalName());
				if (getTransfer().isQuoteAll()) {
					writer.write(quote);
				}
			}
			if (getTransfer().getNewline() != null) {
				writer.write(getTransfer().getNewline().content);
			}
			else {
				writer.write(CSVWriter.DEFAULT_LINE_END);
			}
		}
	}
	
}
