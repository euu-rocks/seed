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
package org.seed.ui.zk.vm.admin;

import java.util.ArrayList;
import java.util.List;

import org.seed.core.data.SystemField;
import org.seed.core.data.SystemObject;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.EntityStatus;
import org.seed.core.entity.NestedEntity;
import org.seed.core.entity.filter.CriterionOperator;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterCriterion;
import org.seed.core.entity.filter.FilterElement;
import org.seed.core.entity.filter.FilterMetadata;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.user.Authorisation;
import org.seed.core.util.Assert;
import org.seed.ui.ListFilter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class AdminFilterViewModel extends AbstractAdminViewModel<Filter> {
	
	private static final String CRITERIA = "criteria";
	
	@Wire("#newFilterWin")
	private Window window;
	
	@WireVariable(value="filterServiceImpl")
	private FilterService filterService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private NestedEntity nested;
	
	private FilterCriterion criterion;
	
	private boolean hqlInput;
	
	public AdminFilterViewModel() {
		super(Authorisation.ADMIN_ENTITY, "filter",
			  "/admin/filter/filterlist.zul", 
			  "/admin/filter/filter.zul",
			  "/admin/filter/newfilter.zul");
	}
	
	public boolean isHqlInput() {
		return hqlInput;
	}

	public void setHqlInput(boolean hqlInput) {
		this.hqlInput = hqlInput;
	}

	public NestedEntity getNested() {
		return nested;
	}

	public void setNested(NestedEntity nested) {
		this.nested = nested;
	}

	public FilterCriterion getCriterion() {
		return criterion;
	}

	public void setCriterion(FilterCriterion criterion) {
		this.criterion = criterion;
	}
	
	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Override
	protected void initObject(Filter filter) {
		if (filter.getHqlQuery() != null) {
			hqlInput = true;
		}
		else if (filter.hasCriteria()) {
			for (FilterCriterion filterCriterion : filter.getCriteria()) {
				initCriterion(filter, filterCriterion);
			}
		}
	}
	
	private void initCriterion(Filter filter, FilterCriterion criterion) {
		final FilterElement element = new FilterElement();
		criterion.setElement(element);
		
		// entity field
		if (criterion.getEntityField() != null) {
			element.setEntityField(criterion.getEntityField());
			// reference field
			if (criterion.getReferenceId() != null) {
				criterion.setReference(
					valueObjectService.getObject(criterion.getEntityField().getReferenceEntity(), 
					  	   						 criterion.getReferenceId()));
			}
		}
		
		// system field
		else if (criterion.getSystemField() != null) {
			element.setSystemField(criterion.getSystemField());
			// status field
			if (criterion.getSystemField() == SystemField.ENTITYSTATUS) {
				criterion.setReference(filter.getEntity().getStatusById(criterion.getReferenceId()));
			}
		}
		else {
			Assert.state(false, "either entity or system field");
		}
	}
	
	@Override
	protected void initFilters() {
		final ListFilter<Filter> filterEntity = getFilter(FILTERGROUP_LIST, "entity");
		filterEntity.setValueFunction(o -> o.getEntity().getName());
		for (Filter filter : getObjectList()) {
			filterEntity.addValue(filter.getEntity().getName());
		}
	}

	@Override
	protected FilterService getObjectService() {
		return filterService;
	}
	
	public List<Entity> getEntities() {
		return entityService.findNonGenericEntities();
	}
	
	public String getElementName(FilterElement element) {
		if (element != null) {
			return element.getEntityField() != null
							? element.getEntityField().getName()
							: getEnumLabel(element.getSystemField());
		}
		return null;
	}
	
	public String getFieldName(FilterElement element) {
		String name = null;
		if (element != null) {
			if (element.getSystemField() != null) {
				name = getEnumLabel(element.getSystemField());
			}
			else {
				name = element.getEntityField().getName();
				if (!element.getEntityField().getEntity().equals(getObject().getEntity())) {
					name += " (" + element.getEntityField().getEntity().getName() + ')';
				}
			}
		}
		return name;
	}
	
	public List<FilterElement> getFilterElements(NestedEntity nested) {
		return filterService.getFilterElements(getObject(), nested);
	}
	
	public Object getReferenceValue(SystemObject object, FilterElement element) {
		if (object != null && element != null) {
			if (object instanceof EntityStatus) {
				return ((EntityStatus) object).getNumberAndName();
			}
			return valueObjectService.getIdentifier((ValueObject) object);
		}
		return null;
	}
	
	public Object getReferenceValue(FilterCriterion criterion) {
		return criterion != null 
				? getReferenceValue(criterion.getReference(), criterion.getElement())
				: null;
	}
	
	public List<SystemObject> getReferenceValues(FilterElement element) {
		final List<SystemObject> result = new ArrayList<>();
		if (element != null) {
			if (element.getSystemField() == SystemField.ENTITYSTATUS) {
				result.addAll(getObject().getEntity().getStatusList());
			}
			else if (element.getEntityField() != null && 
					 element.getEntityField().getReferenceEntity() != null) {
				result.addAll(valueObjectService.getAllObjects(element.getEntityField().getReferenceEntity()));
			}
		}
		return result;
	}
	
	public CriterionOperator[] getOperators(FilterElement element) {
		return element != null 
				? CriterionOperator.getOperators(element.getType()) 
				: null;
	}
	
	@Command
	@NotifyChange("criterion")
	public void selectNested() {
		criterion.setElement(null);
		criterion.setOperator(null);
		flagDirty();
	}
	
	@Command
	@NotifyChange("nested")
	public void selectCriterion() {
		if (criterion.getEntityField() != null && getObject().getEntity().hasNesteds()) {
			for (NestedEntity nestedEntity : getObject().getEntity().getNesteds()) {
				if (nestedEntity.getNestedEntity().containsField(criterion.getEntityField())) {
					nested = nestedEntity;
					return;
				}
			}
		}
		nested = null;
	}
	
	@Command
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, null, notifyObject);
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newFilter() {
		cmdNewObjectDialog();
	}
	
	@Command
	public void editFilter() {
		cmdEditObject();
	}
	
	@Command
	public void createFilter(@BindingParam("elem") Component elem) {
		final FilterMetadata filter = (FilterMetadata) getObject();
		filter.setHqlInput(hqlInput);
		cmdInitObject(elem, window);
	}
	
	@Command
	public void refreshFilter(@BindingParam("elem") Component component) {
		cmdRefresh();
	}
	
	@Command
	public void deleteFilter(@BindingParam("elem") Component component) {
		cmdDeleteObject(component);
	}
	
	@Command
	public void saveFilter(@BindingParam("elem") Component component) {
		((FilterMetadata) getObject()).setHqlInput(hqlInput);
		cmdSaveObject(component);
	}

	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	@NotifyChange({"criterion", "nested"})
	public void newCriterion() {
		criterion = new FilterCriterion();
		criterion.setBooleanValue(Boolean.FALSE);
		nested = null;
		getObject().addCriterion(criterion);
		notifyObjectChange(CRITERIA);
		flagDirty();
	}
	
	@Command
	@NotifyChange("criterion")
	public void removeCriterion() {
		getObject().removeCriterion(criterion);
		criterion = null;
		notifyObjectChange(CRITERIA);
		flagDirty();
	}
	
	@GlobalCommand
	public void globalRefreshObject(@BindingParam("param") Long objectId) {
		refreshObject(objectId);
	}

	@Override
	protected void resetProperties() {
		criterion = null;
		nested = null;
		hqlInput = false;
	}
	
}
