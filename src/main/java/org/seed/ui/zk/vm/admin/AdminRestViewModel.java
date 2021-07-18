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

import org.seed.core.application.ContentObject;
import org.seed.core.codegen.SourceCode;
import org.seed.core.rest.Rest;
import org.seed.core.rest.RestMapping;
import org.seed.core.rest.RestService;
import org.seed.core.rest.codegen.RestCodeProvider;
import org.seed.core.user.Authorisation;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class AdminRestViewModel extends AbstractAdminViewModel<Rest> {
	
	private static final String MAPPINGS = "mappings";
	
	@WireVariable(value="restServiceImpl")
	private RestService restService;
	
	@WireVariable(value="restCodeProvider")
	private RestCodeProvider restCodeProvider;
	
	private RestMapping mapping;
	
	public AdminRestViewModel() {
		super(Authorisation.ADMIN_REST, "rest",
			  "/admin/rest/restlist.zul", 
			  "/admin/rest/rest.zul");
	}
	
	public RestMapping getMapping() {
		return mapping;
	}

	public void setMapping(RestMapping mapping) {
		this.mapping = mapping;
	}

	@Init
	public void init(@ContextParam(ContextType.VIEW) Component view,
					 @ExecutionArgParam("param") Object object) {
		super.init(object, view);
	}
	
	@Command
	@Override
	public void flagDirty(@BindingParam("notify") String notify, 
						  @BindingParam("object") Object object, 
						  @BindingParam("notifyObject") String notifyObject) {
		super.flagDirty(notify, object, notifyObject);
	}
	
	@Command
	public void back() {
		cmdBack();
	}
	
	@Command
	public void newRest() {
		cmdNewObject();
	}
	
	@Command
	@NotifyChange("mapping")
	public void newMapping() {
		mapping = restService.createMapping(getObject());
		notifyObjectChange(MAPPINGS);
		flagDirty();
	}
	
	@Command
	public void editFunction() {
		if (mapping.getContent() == null) {
			mapping.setContent(restCodeProvider.getFunctionTemplate(mapping));
		}
		showCodeDialog(new CodeDialogParameter(this, mapping));
	}

	@Override
	protected RestService getObjectService() {
		return restService;
	}

	@Override
	protected void resetProperties() {
		mapping = null;
	}

	@Override
	protected SourceCode getSourceCode(ContentObject contentObject) {
		final RestMapping mapping = (RestMapping) contentObject;
		return restCodeProvider.getRestSource(mapping);
	}
	
}
