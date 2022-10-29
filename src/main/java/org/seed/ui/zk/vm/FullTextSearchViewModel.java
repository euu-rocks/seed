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
import org.seed.core.data.QueryCursor;
import org.seed.core.entity.Entity;
import org.seed.core.entity.EntityService;
import org.seed.core.entity.value.FullTextResult;
import org.seed.core.entity.value.ValueObject;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.form.Form;
import org.seed.core.form.FormService;
import org.seed.core.util.Assert;
import org.seed.ui.Tab;
import org.seed.ui.zk.LoadOnDemandListModel;

import org.springframework.util.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;

public class FullTextSearchViewModel extends AbstractApplicationViewModel {

	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	@WireVariable(value="entityServiceImpl")
	private EntityService entityService;
	
	@WireVariable(value="formServiceImpl")
	private FormService formService;
	
	private Tab tab;
	
	public String getSearchTerm() {
		return tab.getFullTextSearchTerm();
	}

	public void setSearchTerm(String searchTerm) {
		tab.setFullTextSearchTerm(searchTerm);
	}
	
	@Init
	private void init(@ExecutionArgParam(C.PARAM) Tab tab) {
		Assert.notNull(tab, C.TAB);
		
		this.tab = tab;
	}
	
	public ListModel<FullTextResult> getListModel() {
		if (StringUtils.hasText(getSearchTerm())) {
			
			// get or create cursor
			QueryCursor<FullTextResult> cursor = tab.getFullTextSearchCursor();
			if (cursor == null) {
				cursor = valueObjectService.createFullTextSearchCursor(getSearchTerm());
				tab.setFullTextSearchCursor(cursor);
			}
			
			// create model
			return new LoadOnDemandListModel<FullTextResult>(cursor, false) {
				private static final long serialVersionUID = -7870718083524982606L;
				
				@Override
				protected List<FullTextResult> loadChunk(QueryCursor<FullTextResult> cursor) {
					return valueObjectService.loadFullTextChunk(cursor);
				}
			};
		}
		return null;
	}
	
	@Command
	@NotifyChange("listModel")
	public void search() {
		tab.setFullTextSearchCursor(null);
	}
	
	@Command
	public void clickResult(@BindingParam("entityId") Long entityId,
							@BindingParam("objectId") Long objectId) {
		final Entity entity = entityService.getObject(entityId, currentSession());
		final ValueObject object = valueObjectService.getObject(currentSession(), entity, objectId);
		Assert.state(object != null, "value object not available: " + objectId);
		
		final List<Form> forms = formService.findForms(entity, currentSession());
		if (!forms.isEmpty()) {
			openTab(forms.get(0), object); // use first available form
		}
		else {
			showWarnMessage(getLabel("label.formnotavailable"));
		}
	}
	
}
