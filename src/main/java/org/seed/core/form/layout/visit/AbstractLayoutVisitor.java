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
package org.seed.core.form.layout.visit;

import org.seed.C;
import org.seed.Seed;
import org.seed.core.entity.EntityField;
import org.seed.core.entity.EntityRelation;
import org.seed.core.form.Form;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.SubForm;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutElementAttributes;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.form.layout.LayoutUtils;
import org.seed.core.form.layout.LayoutVisitor;
import org.seed.core.util.Assert;
import org.seed.core.util.TinyId;

abstract class AbstractLayoutVisitor extends LayoutUtils 
	implements LayoutVisitor {
	
	protected static final String LABEL_EMPTY = "label.empty";
	
	private final TinyId tinyId = new TinyId("c");
	
	private final Form form;
	
	private LayoutService layoutService;
	
	private LayoutElement rootElement;

	AbstractLayoutVisitor(Form form) {
		Assert.notNull(form, C.FORM);
		Assert.state(!form.isExpertMode(), "export mode layout cannot be visited");
		
		this.form = form;
	}
	
	protected Form getForm() {
		return form;
	}
	
	protected LayoutElement getRootElement() {
		Assert.stateAvailable(rootElement, "root element");
		
		return rootElement;
	}

	protected void setRootElement(LayoutElement rootElement) {
		Assert.notNull(rootElement, "rootElement");
		Assert.state(this.rootElement == null, "multiple zk elements");
		
		this.rootElement = rootElement;
		this.rootElement.removeChildren(LayoutElement.MENUPOPUP);
	}
	
	protected void addRootChild(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		getRootElement().addChild(element);
	}

	protected String newContextId() {
		return tinyId.next();
	}
	
	protected EntityField getEntityField(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		return getEntityField(getElementId(element));
	}
	
	protected EntityField getEntityField(String uid) {
		Assert.notNull(uid, C.UID);
		
		final EntityField field = form.getEntity().getFieldByUid(uid);
		Assert.stateAvailable(field, "entity field " + uid);
		
		return field;
	}
	
	protected FormFieldExtra getFieldExtra(EntityField entityField) {
		Assert.notNull(entityField, C.ENTITYFIELD);
		
		return form.getFieldExtra(entityField);
	}
	
	protected EntityRelation getRelation(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		return form.getEntity().getRelationByUid(
			getElementId(element).substring(LayoutElementAttributes.PRE_RELATION.length()));
	}
	
	protected SubForm getSubForm(LayoutElement element) {
		Assert.notNull(element, C.ELEMENT);
		
		return form.getSubFormByNestedEntityUid(
			getElementId(element).substring(LayoutElementAttributes.PRE_SUBFORM.length()));
	}
	
	protected LayoutService getLayoutService() {
		if (layoutService == null) {
			layoutService = Seed.getBean(LayoutService.class);
		}
		return layoutService;
	}
	
	protected static String getElementId(LayoutElement element) {
		final String id = element.getId();
		Assert.stateAvailable(id, "element id");
		
		return id;
	}
	
}
