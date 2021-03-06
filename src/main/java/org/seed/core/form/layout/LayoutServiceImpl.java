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

import static org.seed.core.form.layout.LayoutUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.seed.core.data.Order;
import org.seed.core.data.SystemField;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityFieldGroup;
import org.seed.core.entity.NestedEntity;
import org.seed.core.form.Form;
import org.seed.core.form.FormField;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.FormLayout;
import org.seed.core.form.FormMetadata;
import org.seed.core.form.FormService;
import org.seed.core.form.FormSettings;
import org.seed.core.form.SubForm;
import org.seed.core.form.SubFormField;
import org.seed.core.form.layout.BorderLayoutProperties.LayoutAreaProperties;
import org.seed.core.form.layout.visit.CollectFieldIdVisitor;
import org.seed.core.form.layout.visit.DecoratingVisitor;
import org.seed.core.form.layout.visit.FindElementVisitor;
import org.seed.core.form.layout.visit.SearchDecoratingVisitor;
import org.seed.core.form.layout.visit.UndecoratingVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

@Service
public class LayoutServiceImpl implements LayoutService, LayoutProvider {
	
	private final static Logger log = LoggerFactory.getLogger(LayoutServiceImpl.class);
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private LayoutValidator layoutValidator;
	
	private final Map<String, LayoutElement> editLayoutMap = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public void registerEditLayout(Form form, String username, LayoutElement layoutRoot) {
		Assert.notNull(form, "form is null");
		Assert.notNull(username, "username is null");
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
		decorateLayout(form, layoutRoot);
		editLayoutMap.put(username, layoutRoot);
	}
	
	@Override
	public String getLayout(String path, FormSettings settings) {
		Assert.notNull(path, "path is null");
		
		String content = null;
		if (path.startsWith("/edit/")) {
			final String username = path.substring(6, path.lastIndexOf('/'));
			final LayoutElement layout = getEditLayout(username);
			if (layout != null) {
				content = buildLayout(layout);
			}
		}
		else if (path.startsWith("/search/")) {
			final Long formId = Long.parseLong(path.substring(8));
			final Form form = formService.getObject(formId);
			final LayoutElement layout = getSearchLayout(form);
			if (layout != null) {
				content = buildLayout(layout);
			}
		}
		else if (path.startsWith("/list")) {
			final Long formId = Long.parseLong(path.substring(path.indexOf('/', 1) + 1));
			final Form form = formService.getObject(formId);
			content = buildListFormLayout(form, settings);
		}
		else if (path.startsWith("/detail/")) {
			final Long formId = Long.parseLong(path.substring(8));
			final Form form = formService.getObject(formId);
			content = form.getLayout().getContent();
		}
		else {
			throw new UnsupportedOperationException(path);
		}
		if (log.isDebugEnabled()) {
			log.debug(path + '\n' + content);
		}
		return content;
	}
	
	private LayoutElement getSearchLayout(Form form) {
		Assert.notNull(form, "form is null");
		
		if (form.getLayout() != null) {
			final LayoutElement layoutRoot = parseLayout(form.getLayout());
			layoutRoot.accept(new SearchDecoratingVisitor(form));
			return layoutRoot;
		}
		return null;
	}
	
	@Override
	public LayoutElement getEditLayout(String username) {
		Assert.notNull(username, "username is null");
		
		return editLayoutMap.get(username);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void resetEditLayout(String username) {
		Assert.notNull(username, "username is null");
		
		editLayoutMap.remove(username);
	}
	
	@Override
	public LayoutElement parseLayout(FormLayout layout) {
		Assert.notNull(layout, "layout is null");
		Assert.notNull(layout.getContent(), "layout content is null");
		
		try {
			return LayoutParser.parse(layout.getContent());
		} 
		catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new RuntimeException(layout.getContent(), ex);
		}
	}
	
	@Override
	public String buildLayout(LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
		return LayoutBuilder.build(layoutRoot);
	}
	
	@Override
	public void decorateLayout(Form form, LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
		layoutRoot.accept(new DecoratingVisitor(form));
	}
	
	@Override
	public void undecorateLayout(Form form, LayoutElement layoutRoot) {
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
		layoutRoot.accept(new UndecoratingVisitor(form));
	}
	
	@Override
	public LayoutElement getElementByContextId(LayoutElement layoutRoot, String contextId) {
		Assert.notNull(layoutRoot, "layoutRoot is null");
		Assert.notNull(contextId, "contextId is null");
		
		final FindElementVisitor visitor = new FindElementVisitor(contextId);
		layoutRoot.accept(visitor);
		final LayoutElement element = visitor.getElement();
		Assert.state(element != null, "element not found for contextId: " + contextId);
		return element;
	}
	
	@Override
	public List<String> getFieldIdList(FormLayout formLayout) {
		Assert.notNull(formLayout, "formLayout is null");
		
		if (formLayout.getContent() != null) {
			final CollectFieldIdVisitor visitor = new CollectFieldIdVisitor();
			parseLayout(formLayout).accept(visitor);
			return visitor.getFieldIdList();
		}
		return Collections.emptyList();
	}
	
	@Override
	public boolean containsField(FormLayout formLayout, EntityField entityField) {
		Assert.notNull(formLayout, "formLayout is null");
		Assert.notNull(entityField, "entityField is null");
		
		return getFieldIdList(formLayout).contains(entityField.getUid());
	}
	
	@Override
	public List<EntityField> getAvailableEntityFields(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, "form is null");
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
		final List<EntityField> fields = new ArrayList<>();
		final CollectFieldIdVisitor visitor = new CollectFieldIdVisitor();
		layoutRoot.accept(visitor);
		for (EntityField entityField : form.getEntity().getAllFields()) {
			if (!visitor.getFieldIdList().contains(entityField.getUid())) {
				fields.add(entityField);
			}
		}
		return fields;
	}
	
	@Override
	public List<NestedEntity> getAvailableNesteds(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, "form is null");
		Assert.notNull(layoutRoot, "layoutRoot is null");
		
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
		Assert.notNull(form, "form is null");
		layoutValidator.validateText(text);
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeAttribute("align");
		elemCell.removeAttribute("text");
		elemCell.addChild(createLabel(text));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeText(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addField(Form form, EntityField entityField, Orientation labelOrient, 
						 Alignment labelAlign, Alignment labelValign, String width, String height,
						 LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, "form is null");
		layoutValidator.validateEntityField(entityField);
		if (entityField.getType().isBinary()) {
			layoutValidator.validateBinaryField(width, height);
		}
		
		final LayoutElement elemCell = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemField = createFormField(entityField);
		elemCell.addChild(elemField);
		
		// image field
		if (entityField.getType().isBinary()) {
			elemField.setAttribute("width", width);
			elemField.setAttribute("height", height);
		}
		
		// add label
		if (labelOrient != null) {
			final LayoutElement neighborCell = elemCell.getCellNeighbor(labelOrient);
			if (neighborCell != null && !neighborCell.hasChildren()) {
				neighborCell.setOrRemoveAttribute("align", labelAlign);
				neighborCell.setOrRemoveAttribute("valign", labelValign);
				neighborCell.addChild(createLabel(entityField.getName()));
			}
		}
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeField(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemField = getElementByContextId(layoutRoot, contextId);
		elemField.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement replaceCombobox(Form form, EntityField entityField, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		layoutValidator.validateNewGrid(columns, rows);
		
		final LayoutElement element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createGrid(columns, rows, title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeGrid(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		layoutValidator.validateText(title, "label.title");
		
		final LayoutElement element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createTabbox(title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addTab(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, "form is null");
		layoutValidator.validateText(title, "label.title");
		
		final LayoutElement elemTab = getElementByContextId(layoutRoot, contextId);
		elemTab.getParent().addChild(createTab(title));
		elemTab.getParent().getParent().getChild(LayoutElement.TABPANELS).addChild(createTabpanel());
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeTab(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		Assert.notNull(layoutProperties, "layoutProperties is null");
		
		final LayoutElement elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.addChild(LayoutUtils.createBorderLayout(layoutProperties));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayout(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemLayout = getElementByContextId(layoutRoot, contextId).getParent();
		elemLayout.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addSubForm(Form form, NestedEntity nested, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, "form is null");
		
		final SubForm subForm = formService.addSubForm(form, nested);
		buildSubForm(subForm, getElementByContextId(layoutRoot, contextId));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeSubForm(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemListbox = getElementByContextId(layoutRoot, contextId);
		final LayoutElement elemBorderlayout = elemListbox.getParent().getParent();
		final SubForm subForm = form.getSubFormByNestedEntityId(Long.valueOf(elemBorderlayout.getId()));
		form.removeSubForm(subForm);
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
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		elemRow.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayoutArea(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyProperties(LayoutElement element, LayoutElementProperties properties) throws ValidationException {
		Assert.notNull(element, "element is null");
		Assert.notNull(properties, "properties is null");
		
		layoutValidator.validateProperties(element, properties);
		properties.applyTo(element);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyBorderLayoutAreaProperties(LayoutElement element, LayoutAreaProperties properties) throws ValidationException {
		Assert.notNull(element, "element is null");
		Assert.notNull(properties, "properties is null");
		
		layoutValidator.validateBorderLayoutAreaProperties(properties);
		properties.applyTo(element);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyBorderLayoutProperties(Form form, LayoutElement layoutRoot, 
											LayoutElement element, BorderLayoutProperties properties) throws ValidationException {
		Assert.notNull(form, "form is null");
		Assert.notNull(layoutRoot, "layoutRoot is null");
		Assert.notNull(element, "element is null");
		Assert.notNull(properties, "properties is null");
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
		Assert.notNull(form, "form is null");
		Assert.notNull(layoutRoot, "layoutRoot is null");
		Assert.notNull(element, "element is null");
		Assert.notNull(properties, "properties is null");
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
		Assert.notNull(element, "element is null");
		Assert.state(element.is(LayoutElement.LABEL), "element is no label");
		
		layoutValidator.validateText(text);
		if (text.contains("\n")) {
			element.setAttribute("pre", "true");
			element.removeAttribute("value");
			if (element.hasChildren()) {
				element.getChildAt(0).setText(text);
			}
			else {
				element.addChild(createLabelAttribute(text)); 
			}
		}
		else {
			element.removeChildren();
			element.removeAttribute("pre");
			element.setAttribute("value", text);
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void setGridTitle(Form form, LayoutElement layoutRoot, LayoutElement element, String title) {
		Assert.notNull(element, "element is null");
		Assert.state(element.is(LayoutElement.GRID), "element is no grid");
		
		boolean hasStructureChanged = false;
		final boolean hasGroupbox = element.parentIs(LayoutElement.GROUPBOX);
		if (StringUtils.hasText(title)) {
			if (hasGroupbox) {
				element.getParent().getChild(LayoutElement.CAPTION).setAttribute("label", title);
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
		Assert.notNull(form, "form is null");
		
		// analyze and init field and field groups
		final Entity entity = form.getEntity();
		List<EntityField> fieldsWithoutGroup = null;
		Set<EntityFieldGroup> usedFieldGroups = null;
		if (entity.hasAllFields()) {
			for (EntityField entityField : entity.getAllFields()) {
				if (entityField.getFieldGroup() != null) {
					if (usedFieldGroups == null) {
						usedFieldGroups = new HashSet<>();
					}
					usedFieldGroups.add(entityField.getFieldGroup());
				}
				else {
					if (fieldsWithoutGroup == null) {
						fieldsWithoutGroup = new ArrayList<>();
					}
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
		
		// build field group grids
		LayoutElement elemMainGrid = null;
		final int numFieldGroups = usedFieldGroups != null 
				? usedFieldGroups.size() + (fieldsWithoutGroup != null ? 1 : 0) 
				: fieldsWithoutGroup != null ? 1 : 0;
		if (numFieldGroups > 0) {
			final int numGridRows = numFieldGroups / 2 + numFieldGroups % 2;
			elemMainGrid = createGrid(2, numGridRows, null);
			int col = 0, row = 0;
			// fields without group
			if (fieldsWithoutGroup != null) {
				elemMainGrid.getGridCell(0, 0)
							.setValign("top")
							.addChild(buildFieldGrid(fieldsWithoutGroup, entity.getName()));
				col = 1;
			}
			// field groups
			if (usedFieldGroups != null) {
				final List<EntityFieldGroup> fieldGroups = new ArrayList<>(usedFieldGroups);
				fieldGroups.sort(Order.COMPARATOR);
				for (EntityFieldGroup fieldGroup : fieldGroups) {
					elemMainGrid.getGridCell(col, row)
								.setValign("top")
								.addChild(buildFieldGrid(entity.getAllFieldsByGroup(fieldGroup), fieldGroup.getName()));
					if (++col > 1) {
						col = 0;
						row++;
					}
				}
			}
		}
		
		// build layout
		((FormMetadata) form).clearSubForms();
		final LayoutElement elemZK = createZK();
		if (entity.hasAllNesteds()) {
			final LayoutElement elemLayout = elemZK.addChild(new LayoutElement(LayoutElement.BORDERLAYOUT));
			if (elemMainGrid != null) {
				elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.NORTH)).addChild(elemMainGrid);
			}
			final LayoutElement elemTabbox = new LayoutElement(LayoutElement.TABBOX);
			elemTabbox.setAttribute("vflex", "1");
			elemTabbox.setAttribute("hflex", "1");
			elemTabbox.setClass("alpha-tabbox");
			final LayoutElement elemTabs = elemTabbox.addChild(new LayoutElement(LayoutElement.TABS));
			final LayoutElement elemPanels = elemTabbox.addChild(new LayoutElement(LayoutElement.TABPANELS));
			for (NestedEntity nested : entity.getAllNesteds()) {
				try {
					final SubForm subForm = formService.addSubForm(form, nested);
					elemTabs.addChild(createTab(nested.getName()));
					buildSubForm(subForm, elemPanels.addChild(createTabpanel()));
				}
				catch (ValidationException ve) {
					throw new RuntimeException(ve);
				}
			}
			elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER)).addChild(elemTabbox);
		}
		else if (elemMainGrid != null) {
			elemZK.addChild(elemMainGrid);
		}
		return elemZK;
	}
	
	@Override
	public void rebuildLayout(Form form) {
		Assert.notNull(form, "form is null");
		Assert.notNull(form.getLayout() != null, "form has no layout");
		
		final FormLayout formLayout = form.getLayout();
		final LayoutElement layoutRoot = parseLayout(formLayout);
		decorateLayout(form, layoutRoot);
		undecorateLayout(form, layoutRoot);
		formLayout.setContent(buildLayout(layoutRoot));
	}
	
	private void newColumn(Form form, LayoutElement layoutRoot, String contextId, boolean right) {
		Assert.notNull(form, "form is null");
		
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
		Assert.notNull(form, "form is null");
		
		final LayoutElement elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		final LayoutElement elemRows = elemRow.getParent();
		elemRows.addChild(createRow(elemRow.getChildCount()), 
						  elemRows.getChildIndex(elemRow) + (below ? 1 : 0));
		redecorateLayout(form, layoutRoot);
	}
	
	private static final String OBJECT_LIST = "obj";
	
	private String buildListFormLayout(Form form, FormSettings formSettings) {
		Assert.notNull(form, "form is null");
		Assert.notNull(formSettings, "formSettings is null");
		
		final LayoutElement elemListbox = createListFormList();
		final LayoutElement elemListhead = elemListbox.addChild(createListHead(true));
		final LayoutElement elemTemplate = elemListbox.addChild(createTemplate("model", OBJECT_LIST));
		final LayoutElement elemListitem = elemTemplate.addChild(createListItem("'callAction',action=vm.editAction,elem=self"));
		if (form.hasFields()) {
			// sort fields
			formSettings.sortFields(form.getFields());
			for (FormField field : form.getFields()) {
				// check visibility
				if (!formSettings.isFormFieldVisible(field)) {
					continue;
				}
				// header
				final LayoutElement elemListheader = createListHeader(field.getName(), 
																	  field.getHflex() != null ? field.getHflex() : "1", 
																	  field.getLabelStyle());
				elemListheader.setAttribute("style", "cursor:pointer");
				elemListheader.setAttribute("iconSclass", load("vm.getSortIcon(" + field.getId() + ")"));
				elemListheader.setAttribute("onClick", command("'sort',fieldId=" + field.getId()));
				elemListhead.addChild(elemListheader);
				// system field
				if (field.isSystem()) {
					if (field.getSystemField() == SystemField.ENTITYSTATUS) {
						elemListitem.addChild(createListCell(load(listPropertyName(field) + ".numberAndName"), null, field.getStyle()));
					}
					else {
						elemListitem.addChild(createListCell(load(listPropertyName(field)), null, field.getStyle()));
					}
				}
				// entity field
				else if (field.getEntityField().getType().isReference()) {
					elemListitem.addChild(createListCell(load(listPropertyName(field) + '.' +
							field.getEntityField().getReferenceEntityField().getInternalName()), null,  field.getStyle()));
				}
				else if (field.getEntityField().getType().isBinary()) {
					final String converter = field.getThumbnailWidth() != null
												? "vm.getThumbnailConverter(" + field.getId() + ')'
												: "vm.valueConverter";
					elemListitem.addChild(createImageListCell(load(listPropertyName(field)) + ' ' + 
							  								  converter(converter)));
				}
				else {
					final String icon = field.getEntityField().getType().isFile() 
										 ? load(listPropertyName(field) + ".contentType") + ' ' + 
										   	  converter("vm.fileIconConverter")
										 : null;
					elemListitem.addChild(createListCell(load(listPropertyName(field)) + ' ' + 
												   		 converter("vm.valueConverter"), icon, field.getStyle()));
				}
			}
		}
		return buildLayout(elemListbox);
	}
	
	private LayoutElement buildFieldGrid(List<EntityField> fields, String name) {
		Assert.notNull(fields, "fields is null");
		Assert.notNull(name, "name is null");
		
		final LayoutElement elemGrid = new LayoutElement(LayoutElement.GRID);
		final LayoutElement elemRows = elemGrid.addChild(new LayoutElement(LayoutElement.ROWS));
		final LayoutElement elemColumns = createColumns(2);
		elemColumns.getChildAt(0).setAttribute("hflex", "min");
		elemGrid.setClass("alpha-noborder").addChild(elemColumns);
		for (EntityField entityField : fields) {
			if (entityField.getType().isBinary()) {
				continue;
			}
			final LayoutElement elemRow = elemRows.addChild(new LayoutElement(LayoutElement.ROW));
			// label column
			LayoutElement elemCell = elemRow.addChild(createCell());
			elemCell.setAlign("right")
					.setValign("top")
					.addChild(createLabel(entityField.getName()));
			// field column
			elemCell = elemRow.addChild(createCell());
			elemCell.addChild(createFormField(entityField));
		}
		return createGroupbox(name, elemGrid);
	}
	
	private void buildSubForm(SubForm subForm, LayoutElement elemArea) {
		Assert.notNull(subForm, "subForm is null");
		Assert.notNull(elemArea, "elemArea is null");
		
		final LayoutElement elemLayout = elemArea.addChild(new LayoutElement(LayoutElement.BORDERLAYOUT));
		elemLayout.setAttribute("id", subForm.getNestedEntity().getUid());
		final LayoutElement elemCenter = elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		final LayoutElement elemListbox = elemCenter.addChild(createListBox());
		final LayoutElement elemListhead = elemListbox.addChild(createListHead(true));
		elemListbox.setClass("alpha-noborder");
		elemListbox.setAttribute("vflex", "1");
		elemListbox.setAttribute("hflex", "1");
		
		if (subForm.hasFields()) {
			for (SubFormField field : subForm.getFields()) {
				elemListhead.addChild(createListHeader(field.getName(), "1", field.getLabelStyle()));
			}
		}
	}
	
	private void redecorateLayout(Form form, LayoutElement layoutRoot) {
		undecorateLayout(form, layoutRoot);
		decorateLayout(form, layoutRoot);
	}
	
	private static String listPropertyName(FormField field) {
		return OBJECT_LIST + '.' + (field.isSystem() 
									? field.getSystemField().property 
									: field.getEntityField().getInternalName());
	}
	
}
