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
package org.seed.core.form.printout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import org.seed.core.entity.Entity;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.FormPrintout;
import org.seed.core.form.LabelProvider;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

class DOCXPrintoutProcessor extends AbstractPrintoutProcessor {

	public DOCXPrintoutProcessor(Entity entity, LabelProvider labelProvider) {
		super(entity, labelProvider); 
	}

	@Override
	public byte[] process(FormPrintout printout, ValueObject valueObject) {
		Assert.notNull(printout, "printout is null");
		Assert.notNull(valueObject, "valueObject is null");
		
		try (XWPFDocument document = new XWPFDocument(getInputStream(printout))) {
			for (XWPFParagraph paragraph : document.getParagraphs()) {
				processParagraph(paragraph, valueObject, null, null);
			}
			for (XWPFTable table : document.getTables()) {
				int rowIdx = 0;
				int removeRowIdx = -1;
				List<XWPFTableRow> newRows = null;
				for (XWPFTableRow row : table.getRows()) {
					final NestedEntity nestedEntity = findNestedProperty(row);
					// row contains nested entity properties
					if (nestedEntity != null) {
						final List<ValueObject> nestedObjects = getNestedObjects(valueObject, nestedEntity);
						// no objects -> remove template row
						if (ObjectUtils.isEmpty(nestedObjects)) {
							removeRowIdx = rowIdx;
						}
						// exacly one object -> replace template row
						else if (nestedObjects.size() == 1) {
							processRow(row, valueObject, nestedEntity, nestedObjects.get(0));
						}
						// multiple objects
						else {
							// remove template row 
							removeRowIdx = rowIdx;
							newRows = new ArrayList<>();
							// create n new rows
							for (ValueObject nestedObject : nestedObjects) {
								final XWPFTableRow newRow = new XWPFTableRow(
										CTRow.Factory.parse(row.getCtRow().newInputStream()), table);
								processRow(newRow, valueObject, nestedEntity, nestedObject);
								newRows.add(newRow);
							}
						}
					}
					// row without nested entity properties
					else {
						processRow(row, valueObject, null, null);
					}
					rowIdx++;
				}
				
				// modify table structure
				if (removeRowIdx != -1) {
					table.removeRow(removeRowIdx);
				}
				if (newRows != null) {
					for (XWPFTableRow row : newRows) {
						table.addRow(row, removeRowIdx++);
					}
				}
			}
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			document.write(baos);
			return baos.toByteArray();
		} 
		catch (IOException | XmlException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	// scan row for nested object property and return NestendEntity
	private NestedEntity findNestedProperty(XWPFTableRow row) {
		for (XWPFTableCell cell : row.getTableCells()) {
			for (XWPFParagraph paragraph : cell.getParagraphs()) {
				for (XWPFRun run : paragraph.getRuns()) {
					final String text = run.getText(0);
					if (text != null) {
						final NestedEntity nested = findNestedProperty(text);
						if (nested != null) {
							return nested;
						}
					}
				}
			}
		}
		return null;
	}
	
	private void processRow(XWPFTableRow row, ValueObject valueObject, NestedEntity nestedEntity, ValueObject nestedObject) {
		for (XWPFTableCell cell : row.getTableCells()) {
			for (XWPFParagraph paragraph : cell.getParagraphs()) {
				processParagraph(paragraph, valueObject, nestedEntity, nestedObject);
			}
		}
	}
	
	private void processParagraph(XWPFParagraph paragraph, ValueObject valueObject, NestedEntity nestedEntity, ValueObject nestedObject) {
		if (paragraph.getRuns() != null) {
			for (XWPFRun run : paragraph.getRuns()) {
				final String text = run.getText(0);
				if (text != null) {
					run.setText(replace(text, valueObject, nestedEntity, nestedObject), 0);
				}
			}
		}
	}
	
}
