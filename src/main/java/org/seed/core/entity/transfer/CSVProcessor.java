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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.springframework.util.FastByteArrayOutputStream;

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
		final FastByteArrayOutputStream out = new FastByteArrayOutputStream();
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
			throw new InternalException(e);
		}
		return out.toByteArray();
	}
	
	@Override
	public TransferResult doImport(ImportOptions options, InputStream inputStream) throws ValidationException {
		Assert.notNull(options, C.OPTIONS);
		Assert.notNull(inputStream, "inputStream");
		final TransferResult result = new TransferResult(options);
		
		try (Reader reader = new InputStreamReader(inputStream, getCharset())) {
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
		catch (IOException ioe) {
			throw new InternalException(ioe);
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
				if (element.getEntityField() != null) {
					fieldNames.add(element.getEntityField().getInternalName());
				}
				else if (element.getSystemField() != null) {
					fieldNames.add(element.getSystemField().property);
				}
			}
			mappingStrategy.setColumnMapping(fieldNames.toArray(new String[fieldNames.size()]));
		}
		return mappingStrategy;
	}
	
	private void writeHeader(PrintWriter writer) {
		if (getTransfer().hasElements()) {
			writeHeaderFields(writer);
			if (getTransfer().getNewline() != null) {
				writer.write(getTransfer().getNewline().content);
			}
			else {
				writer.write(ICSVWriter.DEFAULT_LINE_END);
			}
		}
	}
	
	private void writeHeaderFields(PrintWriter writer) {
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
			if (element.getEntityField() != null) {
				writer.write(element.getEntityField().getName());
			}
			else if (element.getSystemField() != null) {
				writer.write(getEnumLabel(element.getSystemField()));
			}
			if (getTransfer().isQuoteAll()) {
				writer.write(quote);
			}
		}
	}
	
}
