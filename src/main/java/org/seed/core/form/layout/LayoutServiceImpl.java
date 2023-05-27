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
import static org.seed.core.util.CollectionUtils.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.data.SystemField;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityField;
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
import org.seed.core.util.NameUtils;

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
	
	private static final String EDITLAYOUT_UID = "editLayoutUid";
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private LayoutValidator layoutValidator;
	
	@Autowired
	private LabelProvider labelProvider;
	
	private final LayoutParser layoutParser = new LayoutParser();
	
	private final LayoutBuilder layoutBuilder = new LayoutBuilder();
	
	private final LayoutCache editLayoutCache = new LayoutCache();
	
	@Override
	public void registerEditLayout(Form form, String editLayoutUid, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(editLayoutUid, EDITLAYOUT_UID);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		decorateLayout(form, layoutRoot);
		editLayoutCache.registerLayout(editLayoutUid, layoutRoot);
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
			final String editLayoutUid = path.substring(PATH_EDIT.length(), path.lastIndexOf('/'));
			final LayoutElement layout = getEditLayout(editLayoutUid);
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
	public LayoutElement getEditLayout(String editLayoutUid) {
		Assert.notNull(editLayoutUid, EDITLAYOUT_UID);
		
		return editLayoutCache.getLayout(editLayoutUid);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeEditLayout(String editLayoutUid) {
		Assert.notNull(editLayoutUid, EDITLAYOUT_UID);
		
		editLayoutCache.removeLayout(editLayoutUid);
	}
	
	@Override
	public LayoutElement parseLayout(FormLayout layout) {
		Assert.notNull(layout, C.LAYOUT);
		Assert.notNull(layout.getContent(), "layout content");
		
		try {
			return parseLayout(layout.getContent());
		} 
		catch (SAXException | IOException | ParserConfigurationException ex) {
			throw new InternalException(ex);
		}
	}
	
	@Override
	public LayoutElement parseLayout(String content) throws SAXException, IOException, ParserConfigurationException {
		Assert.notNull(content, C.CONTENT);
		
		return layoutParser.parse(content);
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
		
		final var visitor = new FindElementVisitor(contextId);
		layoutRoot.accept(visitor);
		final var element = visitor.getElement();
		Assert.stateAvailable(element, "element contextId: " + contextId);
		return element;
	}
	
	@Override
	public Set<String> getIdSet(FormLayout formLayout) {
		Assert.notNull(formLayout, C.FORMLAYOUT);
		
		if (formLayout.getContent() != null) {
			final var visitor = new CollectIdVisitor();
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
		
		final var visitor = new CollectIdVisitor();
		layoutRoot.accept(visitor);
		return subList(form.getEntity().getAllFields(), 
					   field -> !visitor.containsId(field.getUid()));
	}
	
	@Override
	public List<EntityField> getAvailableRichTextFields(Form form, LayoutElement layoutRoot) {
		return subList(getAvailableEntityFields(form, layoutRoot), 
					   field -> field.getType().isTextLong());
	}
	
	@Override
	public List<NestedEntity> getAvailableNesteds(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		return subList(form.getEntity().getNesteds(), 
					   nested -> form.getSubFormByNestedEntityId(nested.getId()) == null);
	}
	
	@Override
	public List<EntityRelation> getAvailableRelations(Form form, LayoutElement layoutRoot) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(layoutRoot, C.LAYOUTROOT);
		
		final var visitor = new CollectIdVisitor();
		layoutRoot.accept(visitor);
		return subList(form.getEntity().getAllRelations(), 
					   relation -> !visitor.containsId(LayoutElementAttributes.PRE_RELATION + relation.getUid()));
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement createGridLayout(Form form, Integer columns, Integer rows) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateNewGrid(form, columns, rows);
		
		final var elemZK = createZK();
		elemZK.addChild(createGrid(columns, rows, null));
		return elemZK;
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement createBorderLayout(BorderLayoutProperties layoutProperties) {
		final var elemZK = createZK();
		elemZK.addChild(LayoutUtils.createBorderLayout(layoutProperties));
		return elemZK;
	}
				
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addText(Form form, String text, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateText(form, text);
		
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeAttribute(A_ALIGN);
		elemCell.removeAttribute(C.TEXT);
		elemCell.addChild(createLabel(text));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeText(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		elemCell.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addField(Form form, EntityField entityField, LabelProperties labelProperties, 
						 String width, String height, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateEntityField(form, entityField);
		if (entityField.getType().isBinary()) {
			layoutValidator.validateBinaryField(form, width, height);
		}
		
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		final var elemField = createFormField(entityField);
		elemCell.addChild(elemField);
		
		// image field
		if (entityField.getType().isBinary()) {
			elemField.setAttribute(A_WIDTH, width)
					 .setAttribute(A_HEIGHT, height);
		}
		
		// add label
		if (labelProperties.orient != null) {
			addFieldLabel(entityField, labelProperties, elemCell);
		}
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addRichTextField(Form form, EntityField entityField, LabelProperties labelProperties, 
			 					 LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		
		layoutValidator.validateEntityField(form, entityField);
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		final var elemField = createRichTextField(entityField);
		elemCell.addChild(elemField);
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeField(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemField = getElementByContextId(layoutRoot, contextId);
		elemField.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public LayoutElement replaceCombobox(Form form, EntityField entityField, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemField = getElementByContextId(layoutRoot, contextId);
		final var elemParent = elemField.getParent();
		elemField.removeFromParent();
		final var elemReplacement = elemField.is(LayoutElement.COMBOBOX)
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
		layoutValidator.validateNewGrid(form, columns, rows);
		
		final var element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createGrid(columns, rows, title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeGrid(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemGrid = getElementByContextId(layoutRoot, contextId).getGrid();
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
		layoutValidator.validateText(form, title, "label.title");
		
		final var element = getElementByContextId(layoutRoot, contextId);
		element.addChild(createTabbox(title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void addTab(Form form, String title, LayoutElement layoutRoot, String contextId) throws ValidationException {
		Assert.notNull(form, C.FORM);
		layoutValidator.validateText(form, title, "label.title");
		
		final var elemTab = getElementByContextId(layoutRoot, contextId);
		elemTab.getParent().addChild(createTab(title));
		elemTab.getParent().getParent().getChild(LayoutElement.TABPANELS)
									   .addChild(createTabpanel(title));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeTab(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemTab = getElementByContextId(layoutRoot, contextId);
		final var elemTabbox = elemTab.getParent().getParent();
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
		Assert.notNull(layoutProperties, "layout properties");
		
		final var elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.addChild(LayoutUtils.createBorderLayout(layoutProperties));
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayout(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemLayout = getElementByContextId(layoutRoot, contextId).getParent();
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
		
		final var elemListbox = getElementByContextId(layoutRoot, contextId);
		final var elemBorderlayout = elemListbox.getParent().getParent();
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
		
		final var elemListbox = getElementByContextId(layoutRoot, contextId);
		final var elemBorderlayout = elemListbox.getParent().getParent();
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
		
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		final var elemRows = elemCell.getParent().getParent();
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
		
		final var elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		elemRow.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void removeBorderLayoutArea(Form form, LayoutElement layoutRoot, String contextId) {
		Assert.notNull(form, C.FORM);
		
		final var elemArea = getElementByContextId(layoutRoot, contextId);
		elemArea.removeFromParent();
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyProperties(Form form, LayoutElement element, LayoutElementAttributes properties) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		
		layoutValidator.validateProperties(form, element, properties);
		properties.applyTo(element);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void applyBorderLayoutAreaProperties(Form form, LayoutElement element, LayoutAreaProperties properties) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(element, C.ELEMENT);
		Assert.notNull(properties, C.PROPERTIES);
		
		layoutValidator.validateBorderLayoutAreaProperties(form, properties);
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
		
		layoutValidator.validateBorderLayoutProperties(form, properties);
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
		final var elemArea = element.getParent().getParent().getParent();
		elemArea.removeChildren();
		buildSubForm(properties.subForm, elemArea);
		redecorateLayout(form, layoutRoot);
	}
	
	@Override
	@Secured("ROLE_ADMIN_FORM")
	public void setLabelText(Form form, LayoutElement element, String text) throws ValidationException {
		Assert.notNull(form, C.FORM);
		Assert.notNull(element, C.ELEMENT);
		Assert.state(element.is(LayoutElement.LABEL), "element is no label");
		
		layoutValidator.validateText(form, text);
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
				final var parent = element.getParent();
				parent.removeChild(element);
				parent.addChild(createGroupbox(title, element));
				hasStructureChanged = true;
			}
		}
		else if (hasGroupbox) {
			final var elemGroupbox = element.getParent();
			final var elemContainer = elemGroupbox.getParent();
			element.removeParent();
			elemContainer.removeChild(elemGroupbox);
			elemContainer.addChild(element);
			hasStructureChanged = true;
		}
		if (hasStructureChanged) {
			redecorateLayout(form, layoutRoot);
		}
	}
	
	@Override
	public String buildAutoLayout(Entity entity, Form form) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(form, C.FORM);
		
		for (EntityField entityField : entity.getAllFields()) {
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
		return buildLayout(createAutoLayout(entity, form));
	}
	
	@Override
	public LayoutElement createAutoLayout(Entity entity, Form form) {
		Assert.notNull(entity, C.ENTITY);
		Assert.notNull(form, C.FORM);
		
		((FormMetadata) form).clearSubForms();
		if (entity.hasNesteds()) {
			for (NestedEntity nested : form.getEntity().getNesteds()) {
				try {
					formService.addSubForm(form, nested);
				} catch (ValidationException vex) {
					throw new InternalException(vex);
				}
			}
		}
		return new AutolayoutBuilder(entity, form).build();
	}
	
	private Form getForm(Long formId) {
		final Form form = formService.getObject(formId);
		Assert.stateAvailable(form, C.FORM + ' ' + formId);
		return form;
	}
	
	private LayoutElement getSearchLayout(Form form) {
		Assert.notNull(form, C.FORM);
		
		if (form.getLayout() != null) {
			final var layoutRoot = parseLayout(form.getLayout());
			layoutRoot.accept(new SearchDecoratingVisitor(form));
			return layoutRoot;
		}
		return null;
	}
	
	private void addFieldLabel(EntityField entityField, LabelProperties labelProperties, LayoutElement elemCell) {
		final var neighborCell = elemCell.getCellNeighbor(labelProperties.orient);
		if (neighborCell != null && !neighborCell.hasChildren()) {
			neighborCell.setOrRemoveAttribute(A_ALIGN, labelProperties.align);
			neighborCell.setOrRemoveAttribute(A_VALIGN, labelProperties.valign);
			neighborCell.addChild(createLabel(entityField.getName()));
		}
	}
	
	@Override
	public void rebuildLayout(Form form) {
		Assert.notNull(form, C.FORM);
		Assert.stateAvailable(form.getLayout(), C.LAYOUT);
		
		final FormLayout formLayout = form.getLayout();
		final var layoutRoot = parseLayout(formLayout);
		decorateLayout(form, layoutRoot);
		undecorateLayout(form, layoutRoot);
		formLayout.setContent(buildLayout(layoutRoot));
	}
	
	private void newColumn(Form form, LayoutElement layoutRoot, String contextId, boolean right) {
		Assert.notNull(form, C.FORM);
		
		final var elemCell = getElementByContextId(layoutRoot, contextId);
		final var elemRow = elemCell.getParent();
		final int index = elemRow.getChildIndex(elemCell) + (right ? 1 : 0);
		for (LayoutElement row : elemRow.getParent().getChildren()) {
			row.addChild(createCell(), index);
		}
		elemRow.getParent().getParent().getChild(LayoutElement.COLUMNS).addChild(createColumn(), index);
		redecorateLayout(form, layoutRoot);
	}
	
	private void newRow(Form form, LayoutElement layoutRoot, String contextId, boolean below) {
		Assert.notNull(form, C.FORM);
		
		final var elemRow = getElementByContextId(layoutRoot, contextId).getParent();
		final var elemRows = elemRow.getParent();
		elemRows.addChild(createRow(elemRow.getChildCount()), 
						  elemRows.getChildIndex(elemRow) + (below ? 1 : 0));
		redecorateLayout(form, layoutRoot);
	}
	
	private String buildListFormLayout(Form form, FormSettings formSettings) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(formSettings, "form settings");
		
		if (anyMatch(form.getFields(), formSettings::isFormFieldVisible)) {
			final var elemListbox = createListFormList();
			final var elemListhead = elemListbox.addChild(createListHead(true));
			final var elemTemplate = elemListbox.addChild(createTemplate(A_MODEL, "obj"));
			final var elemListitem = elemTemplate.addChild(createListItem("'callAction',action=vm.editAction,elem=self"))
														   .setAttribute(A_SCLASS, init("vm.getListItemTestClass(obj)"));
			formSettings.sortFields(form.getFields());
			for (FormField field : subList(form.getFields(), formSettings::isFormFieldVisible)) {
				// header
				final var elemListheader = createListHeader(field.getName(), 
															field.getHflex() != null 
																? field.getHflex() 
																: V_1, 
															field.getLabelStyle());
				elemListheader.setAttribute(A_STYLE, "cursor:pointer")
							  .setAttribute(A_ICONSCLASS, load("vm.getSortIcon(" + field.getId() + ')'))
							  .setAttribute(A_ONCLICK, command("'sort',fieldId=" + field.getId()));
				elemListhead.addChild(elemListheader);
				// field
				elemListitem.addChild(buildListFormField(field));
			}
			return buildLayout(elemListbox);
		}
		else {
			final var elemLabel = createLabel("\n\t" + labelProvider.getLabel("form.list.fieldsnotavailable"));
			elemLabel.setAttribute(A_STYLE, "color:crimson");
			return buildLayout(elemLabel);
		}
	}
	
	private static LayoutElement buildListFormField(FormField field) {
		final String testClass = new StringBuilder()
				.append(field.getForm().getEntity().getInternalName()).append('-')
				.append(NameUtils.getInternalName(field.getName().trim()))
				.append("-content").toString().replace('_','-').toLowerCase();
		// system field
		if (field.isSystem()) {
			if (field.getSystemField() == SystemField.ENTITYSTATUS) {
				return createListCell(load(listPropertyName(field) + ".numberAndName"), null, field.getStyle(), testClass);
			}
			else {
				return createListCell(load(listPropertyName(field)), null, field.getStyle(), testClass);
			}
		}
		// reference field
		else if (field.getEntityField().getType().isReference()) {
			return createListCell(load("vm.getIdentifier(" + listPropertyName(field) + ')'), null, field.getStyle(), testClass);
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
			return createListCell(load(listPropertyName(field)) + ' ' + converter(converter), icon, field.getStyle(), testClass);
		}
	}
	
	static void buildRelationForm(EntityRelation relation, LayoutElement elemArea) {
		Assert.notNull(relation, C.RELATION);
		Assert.notNull(elemArea, "elemArea");
		
		final var elemLayout = elemArea.addChild(LayoutUtils.createBorderLayout());
		elemLayout.setAttribute(A_ID, PRE_RELATION + relation.getUid());
		final var elemCenter = elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		final var elemListbox = elemCenter.addChild(createListBox());
		final var elemListhead = elemListbox.addChild(createListHead(true));
		elemListbox.setClass(LayoutElementClass.NO_BORDER)
				   .setAttribute(A_HFLEX, V_1)
				   .setAttribute(A_VFLEX, V_1);
		final var header =  elemListhead.addChild(createListHeader(relation.getRelatedEntity().getName(), V_1, null));
		header.setAttribute(A_SORT, "sort(" + relation.getRelatedEntity().getName() + ')');
	}
	
	static void buildSubForm(SubForm subForm, LayoutElement elemArea) {
		Assert.notNull(subForm, C.SUBFORM);
		Assert.notNull(elemArea, "elemArea");
		
		final var elemLayout = elemArea.addChild(LayoutUtils.createBorderLayout());
		elemLayout.setAttribute(A_ID, PRE_SUBFORM + subForm.getNestedEntity().getUid());
		final var elemCenter = elemLayout.addChild(createBorderLayoutArea(BorderLayoutArea.CENTER));
		final var elemListbox = elemCenter.addChild(createListBox());
		final var elemListhead = elemListbox.addChild(createListHead(true));
		elemListbox.setClass(LayoutElementClass.NO_BORDER)
				   .setAttribute(A_HFLEX, V_1)
				   .setAttribute(A_VFLEX, V_1);
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
		return "obj.".concat(field.isSystem() 
								? field.getSystemField().property 
								: field.getEntityField().getInternalName());
	}
	
}
