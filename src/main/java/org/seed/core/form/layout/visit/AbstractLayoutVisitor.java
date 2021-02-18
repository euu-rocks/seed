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

import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.entity.EntityField;
import org.seed.core.form.Form;
import org.seed.core.form.FormFieldExtra;
import org.seed.core.form.SubForm;
import org.seed.core.form.layout.LayoutElement;
import org.seed.core.form.layout.LayoutService;
import org.seed.core.form.layout.LayoutUtils;
import org.seed.core.form.layout.LayoutVisitor;
import org.seed.core.util.TinyId;

import org.springframework.util.Assert;

abstract class AbstractLayoutVisitor extends LayoutUtils implements LayoutVisitor {
	
	private final TinyId tinyId = new TinyId("c");
	
	protected final Form form;
	
	private LayoutService layoutService;
	
	private LayoutElement rootElement;

	AbstractLayoutVisitor(Form form) {
		Assert.notNull(form, "form is null");
		
		this.form = form;
	}
	
	protected LayoutElement getRootElement() {
		Assert.state(rootElement != null, "root element not available");
		
		return rootElement;
	}

	protected void setRootElement(LayoutElement rootElement) {
		Assert.notNull(rootElement, "rootElement is null");
		Assert.state(this.rootElement == null, "multiple zk elements");
		
		this.rootElement = rootElement;
		this.rootElement.removeChildren(LayoutElement.MENUPOPUP);
	}
	
	protected void addToRoot(LayoutElement element) {
		Assert.notNull(element, "element is null");
		
		getRootElement().addChild(element);
	}

	protected String newContextId() {
		return tinyId.next();
	}
	
	protected EntityField getEntityField(LayoutElement element) {
		Assert.notNull(element, "element is null");
		
		return getEntityField(getId(element));
	}
	
	protected EntityField getEntityField(String uid) {
		Assert.notNull(uid, "uid is null");
		
		final EntityField field = form.getEntity().getFieldByUid(uid);
		Assert.state(field != null, "entity field not found: " + uid);
		return field;
	}
	
	protected FormFieldExtra getFieldExtra(EntityField entityField) {
		Assert.notNull(entityField, "entityField is null");
		
		return form.getFieldExtra(entityField);
	}
	
	protected SubForm getSubForm(LayoutElement element) {
		Assert.notNull(element, "element is null");
		
		return form.getSubFormByNestedEntityUid(getId(element));
	}
	
	protected LayoutService getLayoutService() {
		if (layoutService == null) {
			layoutService = ApplicationContextProvider.getBean(LayoutService.class);
		}
		return layoutService;
	}
	
	protected String getId(LayoutElement element) {
		final String id = element.getId();
		Assert.state(id != null, "element has no id");
		return id;
	}
	
}
