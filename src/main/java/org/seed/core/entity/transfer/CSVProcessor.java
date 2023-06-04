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

import static org.seed.core.util.CollectionUtils.convertedList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.springframework.util.FastByteArrayOutputStream;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

class CSVProcessor extends AbstractTransferProcessor {
	
	CSVProcessor(TransferService transferService,
				 ValueObjectService valueObjectService,
				 Class<? extends ValueObject> objectClass,
				 LabelProvider labelProvider, Transfer transfer) {
		super(transferService, valueObjectService, objectClass, labelProvider, transfer);
	}
	
	@Override
	public byte[] doExport() {
		try (var out = new FastByteArrayOutputStream()) {
			doExport(out);
			return out.toByteArray();
		}
	}
	
	private void doExport(OutputStream out) {
		final var writer = createWriter(out);
		try {
			// header
			if (getTransfer().isHeader()) {
				final var names = convertedList(getTransfer().getElements(), this::getColumnName);
				writer.writeNext(names.toArray(new String[names.size()]), 
								 getTransfer().isQuoteAll());
			}
			// objects
			while (hasNextObject()) {
				writer.writeNext(exportObject(getNextObject()), 
								 getTransfer().isQuoteAll());
			}
		}
		catch (Exception ex) {
			throw new InternalException(ex);
		}
		finally {
			try {
				writer.close();
			} 
			catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Override
	public TransferResult doImport(ImportOptions options, InputStream inputStream) throws ValidationException {
		Assert.notNull(options, C.OPTIONS);
		Assert.notNull(inputStream, "inputStream");
		final var result = new TransferResult(options);
		final var reader = createReader(inputStream);
		final var objects = new ArrayList<ValueObject>();
		
		int line = 1;
		String[] columns;
	    try {
			if (getTransfer().isHeader()) {
				reader.skip(1);
				line++;
			}
	    	while ((columns = reader.readNext()) != null) {
	    		final var object = importObject(line, columns, result);
	    		if (object != null) {
	    			objects.add(object);
	    		}
	    		line++;
			}
	    	saveObjects(objects, options, result);
		} 
	    catch (Exception ex) {
			throw new InternalException(ex);
		}
		return result;
	}
	
	private ValueObject importObject(int line, String[] columns, TransferResult result) {
		try {
			return importObject(columns);
		}
		catch (ParseException pex) {
			result.addError(pex, line);
			return null;
		}
	}
	
	private String getColumnName(TransferElement element) {
		if (element.getName() != null) {
			return element.getName();
		}
		return element.getEntityField() != null 
				? element.getEntityField().getName() 
				: getEnumLabel(element.getSystemField());
	}
	
	private CSVReader createReader(InputStream in) {
		final var parser = new CSVParserBuilder().withIgnoreLeadingWhiteSpace(true);
		if (getTransfer().getSeparatorChar() != null) {
			parser.withSeparator(getTransfer().getSeparatorChar().charAt(0));
		}
		if (getTransfer().getQuoteChar() != null) {
			parser.withQuoteChar(getTransfer().getQuoteChar().charAt(0));
		}
		if (getTransfer().getEscapeChar() != null) {
			parser.withEscapeChar(getTransfer().getEscapeChar().charAt(0));
		}
		return new CSVReaderBuilder(new InputStreamReader(in, getCharset()))
					.withCSVParser(parser.build())
					.build();
	}
	
	private ICSVWriter createWriter(OutputStream out) {
		final var builder = new CSVWriterBuilder(new PrintWriter(out, false, getCharset()));
		if (getTransfer().getNewline() != null) {
			builder.withLineEnd(getTransfer().getNewline().content);
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
		return builder.build();
	}
	
}
