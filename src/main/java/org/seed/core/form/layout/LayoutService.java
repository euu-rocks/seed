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
package org.seed.core.form.layout;

import java.util.List;
import java.util.Set;

import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.form.Form;
import org.seed.core.form.FormLayout;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;

public interface LayoutService {
	
	String buildLayout(LayoutElement layoutRoot);
	
	LayoutElement getEditLayout(String username);
	
	LayoutElement parseLayout(FormLayout layout);
	
	void registerEditLayout(Form form, String username, LayoutElement layout);
	
	void resetEditLayout(String username);
	
	void decorateLayout(Form form, LayoutElement layoutRoot);
	
	void undecorateLayout(Form form, LayoutElement layoutRoot);
	
	LayoutElement getElementByContextId(LayoutElement layoutRoot, String contextId);
	
	Set<String> getIdSet(FormLayout formLayout);
	
	boolean containsField(FormLayout formLayout, EntityField entityField);
	
	boolean containsRelation(FormLayout formLayout, EntityRelation relation);
	
	List<EntityField> getAvailableEntityFields(Form form, LayoutElement layoutRoot);
	
	List<EntityField> getAvailableRichTextFields(Form form, LayoutElement layoutRoot);
	
	List<NestedEntity> getAvailableNesteds(Form form, LayoutElement layoutRoot);
	
	List<EntityRelation> getAvailableRelations(Form form, LayoutElement layoutRoot);
	
	LayoutElement createGridLayout(Integer columns, Integer rows) throws ValidationException;
	
	LayoutElement createBorderLayout(BorderLayoutProperties layoutProperties);
	
	String buildAutoLayout(Form form);
	
	void rebuildLayout(Form form);
	
	LayoutElement createAutoLayout(Form form);
	
	void addText(Form form, String text, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void removeText(Form form, LayoutElement layoutRoot, String contextId);
	
	void addField(Form form, EntityField entityField, LabelProperties labelProperties, String width, String height, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void removeField(Form form, LayoutElement layoutRoot, String contextId);
	
	void addRichTextField(Form form, EntityField entityField, LabelProperties labelProperties, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	LayoutElement replaceCombobox(Form form, EntityField entityField, LayoutElement layoutRoot, String contextId);
	
	void addGrid(Form form, Integer columns, Integer rows, String title, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void removeGrid(Form form, LayoutElement layoutRoot, String contextId);
	
	void addTabbox(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void addTab(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void removeTab(Form form, LayoutElement layoutRoot, String contextId);
	
	void addSubForm(Form form, NestedEntity nested, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void addRelationForm(Form form, EntityRelation relation, LayoutElement layoutRoot, String contextId) throws ValidationException;
	
	void removeSubForm(Form form, LayoutElement layoutRoot, String contextId);
	
	void removeRelationForm(Form form, LayoutElement layoutRoot, String contextId);
	
	void newColumnLeft(Form form, LayoutElement layoutRoot, String contextId);
	
	void newColumnRight(Form form, LayoutElement layoutRoot, String contextId);
	
	void newRowAbove(Form form, LayoutElement layoutRoot, String contextId);
	
	void newRowBelow(Form form, LayoutElement layoutRoot, String contextId);
	
	void removeColumn(Form form, LayoutElement layoutRoot, String contextId);
	
	void removeRow(Form form, LayoutElement layoutRoot, String contextId);
	
	void addBorderLayout(Form form, BorderLayoutProperties layoutProperties, LayoutElement layoutRoot, String contextId);
	
	void applyProperties(LayoutElement element, LayoutElementAttributes properties) throws ValidationException;
	
	void applyBorderLayoutProperties(Form form, LayoutElement layoutRoot, LayoutElement element, BorderLayoutProperties properties) throws ValidationException;
	
	void applyBorderLayoutAreaProperties(LayoutElement element, LayoutAreaProperties properties) throws ValidationException;
	
	void removeBorderLayoutArea(Form form, LayoutElement layoutRoot, String contextId);
	
	void removeBorderLayout(Form form, LayoutElement layoutRoot, String contextId);
	
	void setLabelText(LayoutElement element, String text) throws ValidationException;
	
	void setGridTitle(Form form, LayoutElement layoutRoot, LayoutElement element, String title);
	
	void applySubFormProperties(Form form, LayoutElement layoutRoot, LayoutElement element, SubFormProperties properties);
	
}
