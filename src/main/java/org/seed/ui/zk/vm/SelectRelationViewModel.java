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
package org.seed.ui.zk.vm;

import java.util.List;

import org.seed.C;
import org.seed.core.entity.EntityRelation;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class SelectRelationViewModel extends AbstractViewModel {
	
	@Wire("#selectRelationWin")
	private Window window;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private EntityRelation relation;
	
	private ValueObject selectedObject;
	
	private AbstractFormViewModel parentVM;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) SelectRelationParameter param) {
		Assert.notNull(param, C.PARAM);
		
		relation = param.relation;
		parentVM = param.parentVM;
		wireComponents(view);
	}
	
	public String getIdentifier(ValueObject object) {
		return parentVM.getIdentifier(object);
	}
	
	public String getRelatedName() {
		return relation.getRelatedEntity().getName();
	}
	
	public String getTitle() {
		return getLabel("form.title.relation", getRelatedName());
	}

	public EntityRelation getRelation() {
		return relation;
	}

	public ValueObject getSelectedObject() {
		return selectedObject;
	}

	public void setSelectedObject(ValueObject selectedObject) {
		this.selectedObject = selectedObject;
	}
	
	public List<ValueObject> getAvailableRelations() {
		return valueObjectService.getAvailableRelationObjects(parentVM.getObject(), relation);
	}
	
	@Command
	public void addRelation(@BindingParam(C.ELEM) Component elem) {
		parentVM.assingRelation(relation, selectedObject);
		window.detach();
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
}
