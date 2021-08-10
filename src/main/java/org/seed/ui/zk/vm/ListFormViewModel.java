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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.seed.core.data.Cursor;
import org.seed.core.data.Sort;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.filter.Filter;
import org.seed.core.entity.filter.FilterService;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.form.FormAction;
import org.seed.core.form.FormActionType;
import org.seed.core.form.FormField;
import org.seed.ui.FormParameter;
import org.seed.ui.SearchParameter;
import org.seed.ui.zk.LoadOnDemandListModel;
import org.seed.ui.zk.convert.ThumbnailConverter;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;

public class ListFormViewModel extends AbstractFormViewModel {
	
	@WireVariable(value="filterServiceImpl")
	private FilterService filterService;
	
	private Map<Long, ThumbnailConverter> thumbnailConverterMap;
	
	private Map<Long, Boolean> sortMap;
	
	private List<Filter> filterList;
	
	private Filter currentFilter;
	
	private FormAction editAction;
	
	private Sort sort;
	
	private String fullTextSearchTerm;
	
	private String fullTextQuery;
	
	private int version;
	
	@Init
	@Override
	public void init(@ExecutionArgParam("param") FormParameter param) {
		super.init(param);
		
		filterList = filterService.findFilters(getForm().getEntity());
		editAction = getForm().getActionByType(FormActionType.DETAIL);
	}
	
	@Override
	public boolean isFullTextSearchAvailable() {
		return super.isFullTextSearchAvailable() &&
				getForm().getEntity().hasFullTextSearchFields();
	}
	
	public String getFullTextSearchTerm() {
		return fullTextSearchTerm;
	}

	public void setFullTextSearchTerm(String fullTextSearchTerm) {
		this.fullTextSearchTerm = fullTextSearchTerm;
	}
	
	public boolean isFiltersAvailable() {
		return !filterList.isEmpty();
	}
	
	public List<Filter> getFilters() {
		return filterList;
	}
	
	public Filter getCurrentFilter() {
		return currentFilter;
	}

	public void setCurrentFilter(Filter currentFilter) {
		this.currentFilter = currentFilter;
	}

	public ListModel<ValueObject> getListModel() {
		final SearchParameter searchParam = getTab().getSearchParameter();
		Cursor<ValueObject> cursor;
		if (searchParam != null) {
			cursor = valueObjectService().createCursor(searchParam.searchObject, searchParam.mapOperators);
		}
		else if (fullTextQuery != null) {
			cursor = valueObjectService().createFullTextSearchCursor(fullTextQuery, getForm().getEntity());
			fullTextQuery = null;
		}
		else {
			if (sort != null) {
				cursor = valueObjectService().createCursor(getForm().getEntity(), currentFilter, sort);
			}
			else {
				cursor = valueObjectService().createCursor(getForm().getEntity(), currentFilter);
			}
		}
		
		return new LoadOnDemandListModel<ValueObject>(cursor, false) {
			private static final long serialVersionUID = 6122960735585906371L;
			
			@Override
			protected List<ValueObject> loadChunk(Cursor<ValueObject> cursor) {
				return valueObjectService().loadChunk(cursor);
			}
		};
	}
	
	public ThumbnailConverter getThumbnailConverter(Long fieldId) {
		ThumbnailConverter converter = null;
		if (thumbnailConverterMap != null) {
			converter = thumbnailConverterMap.get(fieldId);
		}
		else {
			thumbnailConverterMap = new HashMap<>();
		}
		if (converter == null) {
			final FormField field = getForm().getFieldById(fieldId);
			converter = new ThumbnailConverter(field.getThumbnailWidth());
			thumbnailConverterMap.put(fieldId, converter);
		}
		return converter;
	}

	public FormAction getEditAction() {
		return editAction;
	}
	
	public String getSortIcon(Long fieldId) {
		if (sortMap == null) {
			return null;
		}
		final Boolean dirAsc = sortMap.get(fieldId);
		if (dirAsc != null) {
			return dirAsc.booleanValue() 
					? "z-icon-sort-up alpha-icon-lg" 
					: "z-icon-sort-down alpha-icon-lg";
		}
		return null;
	}
	
	public boolean isResultList() {
		return getTab().getSearchParameter() != null;
	}

	@Command
	@NotifyChange({"status", "availableStatusList", "transformers"})
	public void selectObject() {
		if (getForm().getEntity().hasStatus()) {
			setStatus(getObject().getEntityStatus());
		}
	}
	
	@Command
	@NotifyChange("listModel")
	public void selectFilter() {
		// do nothing, just notify
	}
	
	@Command
	@NotifyChange("listModel")
	public void searchFullText() {
		fullTextQuery = fullTextSearchTerm;
	}
	
	@Command
	public void changeStatus(@BindingParam("action") FormAction action,
							 @BindingParam("elem") Component component) {
		confirm("question.status", component, action, getStatus().getNumberAndName());
	}
	
	@Command
	public void callAction(@BindingParam("action") FormAction action,
						   @BindingParam("elem") Component component) {
		
		switch (action.getType()) {
			case NEWOBJECT:
				showDetailForm((Long)null);
				break;
			
			case DETAIL:
				showDetailForm(getObject().getId());
				break;
				
			case SEARCH:
				getTab().clearSearch();
				/* falls through */
			case BACKSEARCH:
				showSearchForm();
				break;
				
			case REFRESH:
				reload();
				break;
				
			case TRANSFORM:
				transformObject();
				break;
				
			case PRINT:
				printObject();
				break;
				
			case DELETE:
				confirm("question.delete", component, action);
				break;
				
			case SELECTCOLS:
				showSelectFieldsDialog();
				break;
				
			default:
				throw new UnsupportedOperationException(action.getType().name());
		}
	}
	
	@Command
	@NotifyChange({"listModel", "getSortIcon"})
	public void sort(@BindingParam("fieldId") Long fieldId) {
		if (sortMap == null) {
			sortMap = new HashMap<>();
		}
		Boolean directionAscending = sortMap.get(fieldId);
		if (directionAscending != null) {
			directionAscending = !directionAscending;
		}
		else {
			directionAscending = true;
			sortMap.clear();
		}
		sortMap.put(fieldId, directionAscending);
		sort = new Sort(getForm().getFieldById(fieldId).getEntityField().getInternalName(), 
						directionAscending);
	}
	
	@Override
	protected void confirmed(boolean confirmed, Component component, Object confirmParam) {
		final FormAction action = (FormAction) confirmParam;
		switch (action.getType()) {
			case DELETE:
				try {
					deleteObject();
					reload();
				}
				catch (ValidationException vex) {
					showValidationErrors(component, "form.action.deletefail", vex.getErrors());
				}
				break;
				
			case STATUS:
				if (confirmed) {
					try {
						valueObjectService().changeStatus(getObject(), getStatus());
					}
					catch (ValidationException vex) {
						showValidationErrors(component, "form.action.statusfail", vex.getErrors());
					}
				}
				setStatus(getObject().getEntityStatus());
				notifyChange("status", "availableStatusList", "transformers");
				break;
			
			default:
				throw new UnsupportedOperationException(action.getType().name());
		}
	}
	
	@Override
	protected String getLayoutPath() {
		return "/list" + (version++);
	}
	
	private void reload() {
		notifyChange("listModel");
	}
	
 }
