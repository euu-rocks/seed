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

import static org.seed.core.form.layout.LayoutElementAttributes.*;
import static org.seed.core.form.layout.LayoutUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.data.Order;
import org.seed.core.data.SystemField;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.NestedEntity;
import org.seed.core.form.AbstractFormField;
import org.seed.core.form.Form;
import org.seed.core.form.FormField;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormLayout;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormService;
import org.seed.core.form.FormSettings;
import org.seed.core.form.RelationForm;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormField;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;
import org.seed.core.form.layout.visit.CollectIdVisitor;
import org.seed.core.form.layout.visit.DecoratingVisitor;
import org.seed.core.form.layout.visit.FindElementVisitor;
import org.seed.core.form.layout.visit.SearchDecoratingVisitor;
import org.seed.core.form.layout.visit.UndecoratingVisitor;
import org.seed.core.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

@Service
public class LayoutServiceImpl implements LayoutService, LayoutProvider {
	
	private static final Logger log = LoggerFactory.getLogger(LayoutServiceImpl.class);
	
	private static final String PATH_LIST   = "/list";		//NOSONAR
	private static final String PATH_DETAIL = "/detail/";	//NOSONAR
	private static final String PATH_EDIT   = "/edit/";		//NOSONAR
	private static final String PATH_SEARCH = "/search/";	//NOSONAR
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private LayoutValidator layoutValidator;
	
	private final LayoutParser layoutParser = new LayoutParser();
	
	private final LayoutBuilder layoutBuilder = new LayoutBuilder();
	
	private final Map<String, LayoutElement> editLayoutMap = new ConcurrentHashMap<>();
	
	@Override
	public void registerEditLayout(Form form, String username, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(username, C.USERNAME);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		decorateLayout(form, layoutRoot);
		editLayoutMap.put(username, layoutRoot);
	}
	
	@Override
	public String getLayout(String path, FormSettings settings) {
		Assert.notNull(path, C.PATH);
		
		String content = null;
		if (path.startsWith(PATH_LIST)) {
			final Long formId = Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
			content = buildListFormLayout(getForm(formId), settings);
		}
		else if (path.startsWith(PATH_DETAIL)) {
			final Long formId = Long.parseLong(path.substring(PATH_DETAIL.length()));
			content = getForm(formId).getLayout().getContent();
		}
		else if (path.startsWith(PATH_EDIT)) {
			final String username = path.substring(PATH_EDIT.length(), path.lastIndexOf('/'));
			final LayoutElement layout = getEditLayout(username);
			if (layout != null) {
				content = buildLayout(layout);
			}
		}
		else if (path.startsWith(PATH_SEARCH)) {
			final Long formId = Long.parseLong(path.substring(PATH_SEARCH.length()));
			final LayoutElement layout = getSearchLayout(getForm(formId));
			if (layout != null) {
				content = buildLayout(layout);
			}
		}
		else {
			throw new UnsupportedOperationException(path);
		}
		if (log.isDebugEnabled()) {
			log.debug("{}\n{}", path, content);
		}
		return content;
	}
	
	@Override
	public LayoutElement getEditLayout(String username) {
		Assert.notNull(username, C.USERNAME);
		
		return editLayoutMap.get(username);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void resetEditLayout(String username) {
		Assert.notNull(username, C.USERNAME);
		
		editLayoutMap.remove(username);
	}
	
	@Override
	public LayoutElement parseLayout(FormLayout layout) {
		Assert.notNull(layout, "layout");
		Assert.notNull(layout.getContent(), "layout content");
		
		try {
			return layoutParser.parse(layout.getContent());
		} 
		catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public String buildLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		return layoutBuilder.build(layoutRoot);
	}
	
	@Override
	public void decorateLayout(Form form, LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		layoutRoot.accept(new DecoratingVisitor(form));
	}
	
	@Override
	public void undecorateLayout(Form form, LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		layoutRoot.accept(new UndecoratingVisitor(form));
	}
	
	@Override
	public LayoutElement getElementByContextId(LayoutElement layoutRoot, String contextId) {
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		Assert.notNull(contextId, "contextId");
		
		final FindElementVisitor visitor = new FindElementVisitor(contextId);
		layoutRoot.accept(visitor);
		final LayoutElement element = visitor.getElement();
		Assert.state(element != null, "element not found for contextId: " + contextId);
		return element;
	}
	
	@Override
	public Set<String> getIdSet(FormLayout formLayout) {
		Assert.notNull(formLayout, C.FORMLAYOUT);
		
		if (formLayout.getContent() != null) {
			final CollectIdVisitor visitor = new CollectIdVisitor();
			parseLayout(formLayout).accept(visitor);
			return visitor.getIdSet();
		}
		return Collections.emptySet();
	}
	
	@Override
	public boolean containsField(FormLayout formLayout, EntityField entityField) {
		Assert.notNull(formLayout, C.FORMLAYOUT);
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return getIdSet(formLayout).contains(entityField.getUid());
	}
	
	@Override
	public boolean containsRelation(FormLayout formLayout, EntityRelation relation) {
		Assert.notNull(formLayout, C.FORMLAYOUT);
		Assert.notNull(relation, C.RELATION);
		
		return getIdSet(formLayout).contains(LayoutElementAttributes.PRE_RELATION + relation.getUid());
	}
	
	@Override
	public List<EntityField> getAvailableEntityFields(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		final List<EntityField> fields = new ArrayList<>();
		final CollectIdVisitor visitor = new CollectIdVisitor();
		layoutRoot.accept(visitor);
		for (EntityField entityField : form.getEntity().getAllFields()) {
			if (!visitor.getIdSet().contains(entityField.getUid())) {
				fields.add(entityField);
			}
		}
		return fields;
	}
	
	@Override
	public List<NestedEntity> getAvailableNesteds(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		final List<NestedEntity> nesteds = new ArrayList<>();
		if (form.getEntity().hasAllNesteds()) {
			for (NestedEntity nested : form.getEntity().getAllNesteds()) {
				if (form.getSubFormByNestedEntityId(nested.getId()) == null) {
					nesteds.add(nested);
				}
			}
		}
		return nesteds;
	}
	
	@Override
	public List<EntityRelation> getAvailableRelations(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		final List<EntityRelation> relations = new ArrayList<>();
		final CollectIdVisitor visitor = new CollectIdVisitor();
		layoutRoot.accept(visitor);
		if (form.getEntity().hasAllRelations()) {
			for (EntityRelation relation : form.getEntity().getAllRelations()) {
				if (!visitor.getIdSet().contains(LayoutElementAttributes.PRE_RELATION + relation.getUid())) {
					relations.add(relation);
				}
			}
		}
		return relations;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement createGridLayout(Integer columns, Integer rows) throws ValidationException {
		layoutValidator.validateNewGrid(columns, rows);
		
		final LayoutElement elemZK = createZK();
		elemZK.addChild(createGrid(columns, rows, null));
		return elemZK;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement createBorderLayout(BorderLayoutProperties layoutProperties) {
		final LayoutElement elemZK = createZK();
		elemZK.addChild(LayoutUtils.createBorderLayout(layoutProperties));
		return elemZK;
	}
				
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addText(Form form, String text, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateText(text);
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeAttribute(A_ALIGN);
		elemCell.removeAttribute(C.TEXT);
		elemCell.addChild(createLabel(text));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeText(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addField(Form form, EntityField entityField, LabelProperties labelProperties, 
						 String width, String height, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateEntityField(entityField);
		if (entityField.getType().isBinary()) {
			layoutValidator.validateBinaryField(width, height);
		}
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemField = createFormField(entityField);
		elemCell.addChild(elemField);
		
		// image field
		if (entityField.getType().isBinary()) {
			elemField.setAttribute(A_WIDTH, width);
			elemField.setAttribute(A_HEIGHT, height);
		}
		
		// add label
		if (labelProperties.orient != null) {
			final LayoutElement neighborCell = elemCell.getCellNeighbor(labelProperties.orient);
			if (neighborCell != null && !neighborCell.hasChildren()) {
				neighborCell.setOrRemoveAttribute(A_ALIGN, labelProperties.align);
				neighborCell.setOrRemoveAttribute(A_VALIGN, labelProperties.valign);
				neighborCell.addChild(createLabel(entityField.getName()));
			}
		}
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeField(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemField = getElementByContextId(layoutRoot, contextId);
		elemField.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement replaceCombobox(Form form, EntityField entityField, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemField = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemParent = elemField.getParent();
		elemField.removeFromParent();
		final LayoutElement elemReplacement = elemField.is(LayoutElement.COMBOBOX)
												? createBandbox(entityField)
												: createFormField(entityField);
		elemParent.addChild(elemReplacement);
		redecorateLayout(form, layoutRoot);
		return elemReplacement;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addGrid(Form form, Integer columns, Integer rows, String title, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateNewGrid(columns, rows);
		
		final LayoutElement element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createGrid(columns, rows, title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeGrid(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemGrid = getElementByContextId(layoutRoot, contextId).getGrid();
		if (elemGrid.parentIs(LayoutElement.GROUPBOX)) {
			elemGrid.getParent().removeFromParent();
		}
		else {
			elemGrid.removeFromParent();
		}
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addTabbox(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateText(title, "label.title");
		
		final LayoutElement element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createTabbox(title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addTab(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateText(title, "label.title");
		
		final LayoutElement elemTab = getElementByContextId(layoutRoot, contextId);
		elemTab.getParent().addChild(createTab(title));
		elemTab.getParent().getParent().getChild(LayoutElement.TABPANELS).addChild(createTabpanel());
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeTab(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemTab = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemTabbox = elemTab.getParent().getParent();
		if (elemTab.getParent().getChildren().size() == 1) { // last tab -> remove tabbox
			elemTabbox.removeFromParent();
		}
		else {
			final int index = elemTab.getParent().getChildIndex(elemTab);
			elemTab.getParent().removeChildAt(index);
			elemTabbox.getChild(LayoutElement.TABPANELS).removeChildAt(index);
		}
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addBorderLayout(Form form, BorderLayoutProperties layoutProperties,
						  		LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutProperties, "layoutProperties is null");
		
		final LayoutElement elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.addChild(LayoutUtils.createBorderLayout(layoutProperties));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayout(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemLayout = getElementByContextId(layoutRoot, contextId).getParent();
		elemLayout.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addSubForm(Form form, NestedEntity nested, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		final SubForm subForm = formService.addSubForm(form, nested);
		buildSubForm(subForm, getElementByContextId(layoutRoot, contextId));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addRelationForm(Form form, EntityRelation relation, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		formService.addRelationForm(form, relation);
		buildRelationForm(relation, getElementByContextId(layoutRoot, contextId));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeSubForm(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemListbox = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemBorderlayout = elemListbox.getParent().getParent();
		final String nestedEntityUid = elemBorderlayout.getId().substring(LayoutElementAttributes.PRE_SUBFORM.length());
		final SubForm subForm = form.getSubFormByNestedEntityUid(nestedEntityUid);
		form.removeSubForm(subForm);
		elemBorderlayout.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeRelationForm(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemListbox = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemBorderlayout = elemListbox.getParent().getParent();
		final RelationForm relationForm = form.getRelationFormByUid(elemBorderlayout.getId().substring(LayoutElementAttributes.PRE_RELATION.length()));
		form.removeRelationForm(relationForm);
		elemBorderlayout.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void newColumnLeft(Form form, LayoutElement layoutRoot, String contextId) {
		newColumn(form, layoutRoot, contextId, false);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void newColumnRight(Form form, LayoutElement layoutRoot, String contextId) {
		newColumn(form, layoutRoot, contextId, true);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void newRowAbove(Form form, LayoutElement layoutRoot, String contextId) {
		newRow(form, layoutRoot, contextId, false);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void newRowBelow(Form form, LayoutElement layoutRoot, String contextId) {
		newRow(form, layoutRoot, contextId, true);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeColumn(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemRows = elemCell.getParent().getParent();
		final int index = elemCell.getParent().getChildIndex(elemCell);
		for (LayoutElement elemRow : elemRows.getChildren()) {
			final LayoutElement cell = elemRow.getChildAt(index);
			elemRow.removeChild(cell);
		}
		elemRows.getParent().getChild(LayoutElement.COLUMNS).removeChildAt(index);
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeRow(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		elemRow.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayoutArea(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyProperties(LayoutElement element, LayoutElementAttributes properties) throws ValidationException {
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		
		layoutValidator.validateProperties(element, properties);
		properties.applyTo(element);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyBorderLayoutAreaProperties(LayoutElement element, LayoutAreaProperties properties) throws ValidationException {
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		
		layoutValidator.validateBorderLayoutAreaProperties(properties);
		properties.applyTo(element);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyBorderLayoutProperties(Form form, LayoutElement layoutRoot, 
											LayoutElement element, BorderLayoutProperties properties) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		Assert.state(element.is(LayoutElement.BORDERLAYOUT), "element is no borderlayout");
		
		layoutValidator.validateBorderLayoutProperties(properties);
		// check areas visibility state change
		boolean visibilityChanged = false;
		for (LayoutAreaProperties areaProperties : properties.getLayoutAreaProperties()) {
			final String areaName = areaProperties.getLayoutArea().name;
			// area becomes visible
			if (areaProperties.isVisible() && !element.existChild(areaName)) {
				element.addChild(new LayoutElement(areaName));
				visibilityChanged = true;
			}
			// area becomes not visible
			else if (!areaProperties.isVisible() && element.existChild(areaName)) {
				element.removeChildren(areaName);
				visibilityChanged = true;
			}
		}
		properties.applyTo(element);
		if (visibilityChanged) {
			redecorateLayout(form, layoutRoot);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applySubFormProperties(Form form, LayoutElement layoutRoot,
									   LayoutElement element, SubFormProperties properties) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		Assert.state(element.is(LayoutElement.LISTBOX), "element is no listbox");
		
		properties.applyToSubForm();
		final LayoutElement elemArea = element.getParent().getParent().getParent();
		elemArea.removeChildren();
		buildSubForm(properties.subForm, elemArea);
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void setLabelText(LayoutElement element, String text) throws ValidationException {
		Assert.notNull(element, C.ELEMENT);
		Assert.state(element.is(LayoutElement.LABEL), "element is no label");
		
		layoutValidator.validateText(text);
		if (text.contains("\n")) {
			element.setAttribute(A_PRE, V_TRUE);
			element.removeAttribute(A_VALUE);
			if (element.hasChildren()) {
				element.getChildAt(0).setText(text);
			}
			else {
				element.addChild(createLabelAttribute(text)); 
			}
		}
		else {
			element.removeChildren();
			element.removeAttribute(A_PRE);
			element.setAttribute(A_VALUE, text);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void setGridTitle(Form form, LayoutElement layoutRoot, LayoutElement element, String title) {
		Assert.notNull(element, C.ELEMENT);
		Assert.state(element.is(LayoutElement.GRID), "element is no grid");
		
		boolean hasStructureChanged = false;
		final boolean hasGroupbox = element.parentIs(LayoutElement.GROUPBOX);
		if (StringUtils.hasText(title)) {
			if (hasGroupbox) {
				element.getParent().getChild(LayoutElement.CAPTION)
								   .setAttribute(A_LABEL, title);
			}
			else {
				final LayoutElement parent = element.getParent();
				parent.removeChild(element);
				parent.addChild(createGroupbox(title, element));
				hasStructureChanged = true;
			}
		}
		else if (hasGroupbox) {
			final LayoutElement elemGroupbox = element.getParent();
			final LayoutElement elemContainer = elemGroupbox.getParent();
			elemContainer.removeChild(elemGroupbox);
			elemContainer.addChild(element);
			hasStructureChanged = true;
		}
		if (hasStructureChanged) {
			redecorateLayout(form, layoutRoot);
		}
	}
	
	@Override
	public String buildAutoLayout(Form form) {
		return buildLayout(createAutoLayout(form));
	}
	
	@Override
	public LayoutElement createAutoLayout(Form form) {
		Assert.notNull(form, C.FORM);
		
		// analyze and init field and field groups
		final Entity entity = form.getEntity();
		List<EntityField> fieldsWithoutGroup = new ArrayList<>();
		Set<EntityFieldGroup> usedFieldGroups = new HashSet<>();
		if (entity.hasAllFields()) {
			analyzeAutoLayoutFields(form, fieldsWithoutGroup, usedFieldGroups);
		}
		
		// build field group grids
		final LayoutElement elemMainGrid = buildAutoLayoutFieldGroupGrids(entity, fieldsWithoutGroup, usedFieldGroups);
		
		// build layout
		((FormMetadata) form).clearSubForms();
		final LayoutElement elemZK = createZK();
		if (entity.hasAllNesteds() || entity.hasAllRelations()) {
			buildAutoLayoutSubForms(form, elemZK, elemMainGrid);
		}
		else if (elemMainGrid != null) {
			elemZK.addChild(elemMainGrid);
		}
		return elemZK;
	}
	
	private Form getForm(Long formId) {
		final Form form = formService.getObject(formId);
		Assert.stateAvailable(form, C.FORM + ' ' + formId);
		return form;
	}
	
	private LayoutElement getSearchLayout(Form form) {
		Assert.notNull(form, C.FORM);
		
		if (form.getLayout() != null) {
			final LayoutElement layoutRoot = parseLayout(form.getLayout());
			layoutRoot.accept(new SearchDecoratingVisitor(form));
			return layoutRoot;
		}
		return null;
	}
	
	private void analyzeAutoLayoutFields(Form form, List<EntityField> fieldsWithoutGroup, Set<EntityFieldGroup> usedFieldGroups) {
		for (EntityField entityField : form.getEntity().getAllFields()) {
			if (entityField.getFieldGroup() != null) {
				usedFieldGroups.add(entityField.getFieldGroup());
			}
			else {
				fieldsWithoutGroup.add(entityField);
			}
			if (entityField.getType().isReference()) {
				final List<Form> forms = formService.findForms(entityField.getReferenceEntity());
				if (!forms.isEmpty()) {
					FormFieldExtra fieldExtra = form.getFieldExtra(entityField);
					if (fieldExtra == null) {
						fieldExtra = new FormFieldExtra();
						fieldExtra.setEntityField(entityField);
						form.addFieldExtra(fieldExtra);
					}
					fieldExtra.setDetailForm(forms.get(0));
				}
			}
		}
	}
	
	private LayoutElement buildAutoLayoutFieldGroupGrids(Entity entity, List<EntityField> fieldsWithoutGroup, Set<EntityFieldGroup> usedFieldGroups) {
		LayoutElement elemMainGrid = null;
		int numFieldGroups = usedFieldGroups.size();
		if (!fieldsWithoutGroup.isEmpty()) {
			numFieldGroups++;
		}
		if (numFieldGroups > 0) {
			final int numGridRows = numFieldGroups / 2 + numFieldGroups % 2;
			elemMainGrid = createGrid(2, numGridRows, null);
			int col = 0;
			int row = 0;
			// fields without group
			if (!fieldsWithoutGroup.isEmpty()) {
				elemMainGrid.getGridCell(0, 0)
							.setValign(V_TOP)
							.addChild(buildFieldGrid(fieldsWithoutGroup, entity.getName()));
				col++;
			}
			// field groups
			if (!usedFieldGroups.isEmpty()) {
				final List<EntityFieldGroup> fieldGroups = new ArrayList<>(usedFieldGroups);
				Order.sort(fieldGroups);
				for (EntityFieldGroup fieldGroup : fieldGroups) {
					elemMainGrid.getGridCell(col, row)
								.setValign(V_TOP)
								.addChild(buildFieldGrid(entity.getAllFieldsByGroup(fieldGroup), fieldGroup.getName()));
					if (++col > 1) {
						col = 0;
						row++;
					}
				}
			}
		}
		return elemMainGrid;
	}
	
	private void buildAutoLayoutSubForms(Form form, LayoutElement elemZK, LayoutElement elemMainGrid) {
		final LayoutElement elemLayout = elemZK.addChild(new LayoutElement(LayoutElement.BORDERLAYOUT));
		if (elemMainGrid != null) {
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.NORTH)).addChild(elemMainGrid);
		}
		final LayoutElement elemTabbox = new LayoutElement(LayoutElement.TABBOX);
		elemTabbox.setAttribute(A_HFLEX, V_1);
		elemTabbox.setAttribute(A_VFLEX, V_1);
		elemTabbox.setClass(LayoutElementClass.TABBOX);
		final LayoutElement elemTabs = elemTabbox.addChild(new LayoutElement(LayoutElement.TABS));
		final LayoutElement elemPanels = elemTabbox.addChild(new LayoutElement(LayoutElement.TABPANELS));
		if (form.getEntity().hasAllNesteds()) {
			for (NestedEntity nested : form.getEntity().getAllNesteds()) {
				try {
					final SubForm subForm = formService.addSubForm(form, nested);
					elemTabs.addChild(createTab(nested.getName()));
					buildSubForm(subForm, elemPanels.addChild(createTabpanel()));
				}
				catch (ValidationException ve) {
					throw new InternalException(ve);
				}
			}
		}
		if (form.getEntity().hasAllRelations()) {
			for (EntityRelation relation : form.getEntity().getAllRelations()) {
				elemTabs.addChild(createTab(relation.getName()));
				buildRelationForm(relation, elemPanels.addChild(createTabpanel()));
			}
		}
		
		elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER)).addChild(elemTabbox);
	}
	
	@Override
	public void rebuildLayout(Form form) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(form.getLayout() != null, "form has no layout");
		
		final FormLayout formLayout = form.getLayout();
		final LayoutElement layoutRoot = parseLayout(formLayout);
		decorateLayout(form, layoutRoot);
		undecorateLayout(form, layoutRoot);
		formLayout.setContent(buildLayout(layoutRoot));
	}
	
	private void newColumn(Form form, LayoutElement layoutRoot, String contextId, boolean right) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemRow = elemCell.getParent();
		final int index = elemRow.getChildIndex(elemCell) + (right ? 1 : 0);
		for (LayoutElement row : elemRow.getParent().getChildren()) {
			row.addChild(createCell(), index);
		}
		elemRow.getParent().getParent().getChild(LayoutElement.COLUMNS).addChild(createColumn(), index);
		redecorateLayout(form, layoutRoot);
	}
	
	private void newRow(Form form, LayoutElement layoutRoot, String contextId, boolean below) {
		Assert.notNull(form, C.FORM);
		
		final LayoutElement elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		final LayoutElement elemRows = elemRow.getParent();
		elemRows.addChild(createRow(elemRow.getChildCount()), 
						  elemRows.getChildIndex(elemRow) + (below ? 1 : 0));
		redecorateLayout(form, layoutRoot);
	}
	
	private String buildListFormLayout(Form form, FormSettings formSettings) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(formSettings, "formSettings");
		
		final LayoutElement elemListbox = createListFormList();
		final LayoutElement elemListhead = elemListbox.addChild(createListHead(true));
		final LayoutElement elemTemplate = elemListbox.addChild(createTemplate(A_MODEL, "obj"));
		final LayoutElement elemListitem = elemTemplate.addChild(createListItem("'callAction',action=vm.editAction,elem=self"));
		if (form.hasFields()) {
			formSettings.sortFields(form.getFields());
			for (FormField field : form.getFields()) {
				// check visibility
				if (!formSettings.isFormFieldVisible(field)) {
					continue;
				}
				// header
				final LayoutElement elemListheader = createListHeader(field.getName(), 
																	  field.getHflex() != null 
																	  	? field.getHflex() 
																	  	: V_1, 
																	  field.getLabelStyle());
				elemListheader.setAttribute(A_STYLE, "cursor:pointer");
				elemListheader.setAttribute(A_ICONSCLASS, load("vm.getSortIcon(" + field.getId() + ")"));
				elemListheader.setAttribute(A_ONCLICK, command("'sort',fieldId=" + field.getId()));
				elemListhead.addChild(elemListheader);
				// field
				elemListitem.addChild(buildListFormField(field));
			}
		}
		return buildLayout(elemListbox);
	}
	
	private static LayoutElement buildListFormField(FormField field) {
		// system field
		if (field.isSystem()) {
			if (field.getSystemField() == SystemField.ENTITYSTATUS) {
				return createListCell(load(listPropertyName(field) + ".numberAndName"), null, field.getStyle());
			}
			else {
				return createListCell(load(listPropertyName(field)), null, field.getStyle());
			}
		}
		// reference field
		else if (field.getEntityField().getType().isReference()) {
			return createListCell(load("vm.getIdentifier(" + listPropertyName(field) + ')'), null, field.getStyle());
		}
		// binary field
		else if (field.getEntityField().getType().isBinary()) {
			final String converter = field.getThumbnailWidth() != null
										? "vm.getThumbnailConverter(" + field.getId() + ')'
										: "vm.valueConverter";
			return createImageListCell(load(listPropertyName(field)) + ' ' + converter(converter));
		}
		// every other field
		else {
			final String icon = field.getEntityField().getType().isFile() 
								 ? load(listPropertyName(field) + ".contentType") + ' ' + 
								   	  converter("vm.fileIconConverter")
								 : null;
			final String converter = field.getEntityField().getType().isDateTime()
										? "vm.dateTimeConverter" 
										: "vm.valueConverter";
			return createListCell(load(listPropertyName(field)) + ' ' + converter(converter), icon, field.getStyle());
		}
	}
	
	private static LayoutElement buildFieldGrid(List<EntityField> fields, String name) {
		Assert.notNull(fields, "fields");
		Assert.notNull(name, C.NAME);
		
		final LayoutElement elemGrid = new LayoutElement(LayoutElement.GRID);
		final LayoutElement elemRows = elemGrid.addChild(new LayoutElement(LayoutElement.ROWS));
		final LayoutElement elemColumns = createColumns(2);
		elemColumns.getChildAt(0).setAttribute(A_HFLEX, V_MIN);
		elemGrid.setClass(LayoutElementClass.NO_BORDER).addChild(elemColumns);
		for (EntityField entityField : fields) {
			if (entityField.getType().isBinary()) {
				continue;
			}
			final LayoutElement elemRow = elemRows.addChild(new LayoutElement(LayoutElement.ROW));
			// label column
			LayoutElement elemCell = elemRow.addChild(createCell());
			elemCell.setAlign(V_RIGHT)
					.setValign(V_TOP)
					.addChild(createLabel(entityField.getName()));
			// field column
			elemCell = elemRow.addChild(createCell());
			elemCell.addChild(createFormField(entityField));
		}
		return createGroupbox(name, elemGrid);
	}
	
	private static void buildRelationForm(EntityRelation relation, LayoutElement elemArea) {
		Assert.notNull(relation, C.RELATION);
		Assert.notNull(elemArea, "elemArea");
		
		final LayoutElement elemLayout = elemArea.addChild(new LayoutElement(LayoutElement.BORDERLAYOUT));
		elemLayout.setAttribute(A_ID, PRE_RELATION + relation.getUid());
		final LayoutElement elemCenter = elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		final LayoutElement elemListbox = elemCenter.addChild(createListBox());
		final LayoutElement elemListhead = elemListbox.addChild(createListHead(true));
		elemListbox.setClass(LayoutElementClass.NO_BORDER);
		elemListbox.setAttribute(A_HFLEX, V_1);
		elemListbox.setAttribute(A_VFLEX, V_1);
		final LayoutElement header =  elemListhead.addChild(
				createListHeader(relation.getRelatedEntity().getName(), V_1, null));
		header.setAttribute(A_SORT, "sort(" + relation.getRelatedEntity().getName() + ')');
	}
	
	private static void buildSubForm(SubForm subForm, LayoutElement elemArea) {
		Assert.notNull(subForm, C.SUBFORM);
		Assert.notNull(elemArea, "elemArea");
		
		final LayoutElement elemLayout = elemArea.addChild(new LayoutElement(LayoutElement.BORDERLAYOUT));
		elemLayout.setAttribute(A_ID, PRE_SUBFORM + subForm.getNestedEntity().getUid());
		final LayoutElement elemCenter = elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		final LayoutElement elemListbox = elemCenter.addChild(createListBox());
		final LayoutElement elemListhead = elemListbox.addChild(createListHead(true));
		elemListbox.setClass(LayoutElementClass.NO_BORDER);
		elemListbox.setAttribute(A_HFLEX, V_1);
		elemListbox.setAttribute(A_VFLEX, V_1);
		
		if (subForm.hasFields()) {
			for (SubFormField field : subForm.getFields()) {
				final LayoutElement header =  elemListhead.addChild(
						createListHeader(field.getName(), V_1, field.getLabelStyle()));
				header.setAttribute(A_SORT, getSortExpression(field));
			}
		}
	}
	
	private void redecorateLayout(Form form, LayoutElement layoutRoot) {
		undecorateLayout(form, layoutRoot);
		decorateLayout(form, layoutRoot);
	}
	
	private static String getSortExpression(AbstractFormField formField) {
		return "auto(" + formField.getEntityField().getInternalName() + ')';
	}
	
	private static String listPropertyName(FormField field) {
		return "obj." + (field.isSystem() 
							? field.getSystemField().property 
							: field.getEntityField().getInternalName());
	}
	
}
