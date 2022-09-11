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
package org.seed.core.form.navigation;

import static org.seed.core.util.CollectionUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import org.seed.C;
import org.seed.InternalException;
import org.seed.LabelProvider;
import org.seed.core.application.AbstractApplicationEntityService;
import org.seed.core.application.ApplicationEntity;
import org.seed.core.application.ApplicationEntityService;
import org.seed.core.application.module.ImportAnalysis;
import org.seed.core.application.module.Module;
import org.seed.core.application.module.TransferContext;
import org.seed.core.data.QueryParameter;
import org.seed.core.data.SystemEntityRepository;
import org.seed.core.data.SystemEntityValidator;
import org.seed.core.data.ValidationException;
import org.seed.core.entity.EntityAccess;
import org.seed.core.form.Form;
import org.seed.core.form.FormChangeAware;
import org.seed.core.form.FormDependent;
import org.seed.core.form.FormOptions;
import org.seed.core.form.FormService;
import org.seed.core.user.User;
import org.seed.core.util.Assert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl extends AbstractApplicationEntityService<Menu>
	implements MenuService, FormDependent<Menu>, FormChangeAware {
	
	private static final String LABEL_DEFAULT_MENU = "label.defaultmenu";
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private MenuRepository menuRepository;
	
	@Autowired
	private MenuValidator menuValidator;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Override
	public String getDefaultMenuName() {
		return labelProvider.getLabel(LABEL_DEFAULT_MENU);
	}
	
	@Override
	public List<Menu> getTopLevelMenus() {
		return menuRepository.find(queryParam(C.PARENT, QueryParameter.IS_NULL));
	}
	
	@Override
	public List<Menu> getCustomTopLevelMenus() {
		return filterDefaultMenu(getTopLevelMenus());
	}
	
	@Override
	public List<Menu> findObjectsWithoutModule() {
		return menuRepository.find(queryParam(C.PARENT, QueryParameter.IS_NULL),
								   queryParam(C.MODULE, QueryParameter.IS_NULL));
	}
	
	@Override
	public boolean existCustomMenus() {
		return !getCustomTopLevelMenus().isEmpty();
	}
	
	@Override
	public List<Menu> findCustomMenusWithoutModule() {
		return filterDefaultMenu(findObjectsWithoutModule());
	}
	
	@Override
	public List<Menu> getMenus(User user) {
		Assert.notNull(user, C.USER);
		
		return subList(getTopLevelMenus(), menu -> 
						menu.getForm() == null || 
						menu.getForm().getEntity().checkPermissions(user, EntityAccess.READ));
	}
	
	@Override
	@Secured("ROLE_ADMIN_MENU")
	public Menu createMenuEntry(Menu menu, Form form) {
		Assert.notNull(menu, C.MENU);
		Assert.notNull(form, C.FORM);
		
		final MenuMetadata entry = new MenuMetadata();
		entry.setName(form.getName());
		entry.setForm(form);
		entry.setParent(menu);
		return entry;
	}
	
	@Override
	public List<Menu> findUsage(Form form) {
		Assert.notNull(form, C.FORM);
		
		return menuRepository.find(queryParam(C.FORM, form));
	}
	
	@Override
	public void notifyCreate(Form form, Session session) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(session, C.SESSION);
		try {
			final FormOptions formOptions = form.getOptions();
			if (formOptions != null) {
				Menu parentMenu = formOptions.getMenu();
				if (parentMenu == null) {
					final String defaultMenuName = getDefaultMenuName();
					parentMenu = findByName(defaultMenuName, session);
					if (parentMenu == null) {
						parentMenu = createInstance(null);
						parentMenu.setName(defaultMenuName);
						((MenuMetadata)parentMenu).setModule(formOptions.getModule());
						saveObject(parentMenu, session);
					}
				}
				final MenuMetadata entry = (MenuMetadata) createInstance(null);
				entry.setName(form.getName());
				entry.setForm(form);
				entry.setParent(parentMenu);
				saveObject(entry, session);
			}
		}
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	@Override
	public void notifyChange(Form object, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyDelete(Form form, Session session) {
		Assert.notNull(form, C.FORM);
		Assert.notNull(session, C.SESSION);
		
		for (Menu menu : menuRepository.find(session, queryParam(C.FORM, form))) {
			session.delete(menu);
		}
	}

	@Override
	protected SystemEntityRepository<Menu> getRepository() {
		return menuRepository;
	}

	@Override
	protected SystemEntityValidator<Menu> getValidator() {
		return menuValidator;
	}
	
	@Override
	protected void analyzeNextVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (analysis.getModule().getMenus() != null) {
			for (Menu menu : analysis.getModule().getMenus()) {
				if (currentVersionModule == null) {
					analysis.addChangeNew(menu);
				}
				else {
					final Menu currentVersionMenu = 
						currentVersionModule.getMenuByUid(menu.getUid());
					if (currentVersionMenu == null) {
						analysis.addChangeNew(menu);
					}
					else if (!menu.isEqual(currentVersionMenu)) {
						analysis.addChangeModify(menu);
					}
				}
			}
		}
	}
	
	@Override
	protected void analyzeCurrentVersionObjects(ImportAnalysis analysis, Module currentVersionModule) {
		if (currentVersionModule.getMenus() != null) {
			for (Menu currentVersionMenu : currentVersionModule.getMenus()) {
				if (analysis.getModule().getMenuByUid(currentVersionMenu.getUid()) == null) {
					analysis.addChangeDelete(currentVersionMenu);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<ApplicationEntity>>[] getImportDependencies() {
		return new Class[] { FormService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, C.CONTEXT);
		Assert.notNull(session, C.SESSION);
		try {
			if (context.getModule().getMenus() != null) {
				for (Menu menu : context.getModule().getMenus()) {
					MenuMetadata menuMeta = (MenuMetadata) menu;
					menuMeta.setModule(context.getModule());
					if (menu.getFormUid() != null) {
						menuMeta.setForm(formService.findByUid(session, menu.getFormUid()));
					}
					final Menu currentVersionMenu = findByUid(session, menu.getUid());
					if (currentVersionMenu != null) {
						((MenuMetadata) currentVersionMenu).copySystemFieldsTo(menu);
						session.detach(currentVersionMenu);
					}
					initMenu(menuMeta, currentVersionMenu, session);
					saveObject(menu, session);	
				}
			}
		}
		catch (ValidationException vex) {
			throw new InternalException(vex);
		}
	}
	
	private void initMenu(MenuMetadata menu, Menu currentVersionMenu, Session session) {
		if (menu.getChildren() != null) {
			for (MenuMetadata child : menu.getChildren()) {
				child.setParent(menu);
				if (child.getFormUid() != null) {
					child.setForm(formService.findByUid(session, child.getFormUid()));
				}
				final Menu currentVersionChild =
					currentVersionMenu != null ? currentVersionMenu.getChildByUid(child.getUid()) : null;
				if (currentVersionChild != null) {
					((MenuMetadata) currentVersionChild).copySystemFieldsTo(child);
				}
			}
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, C.MODULE);
		Assert.notNull(currentVersionModule, "currentVersionModule");
		Assert.notNull(session, C.SESSION);
		
		if (currentVersionModule.getMenus() != null) {
			for (Menu currentVersionMenu : currentVersionModule.getMenus()) {
				if (module.getMenuByUid(currentVersionMenu.getUid()) == null) {
					session.delete(currentVersionMenu);
				}
				// delete sub menus for deleted forms
				else if (currentVersionMenu.hasSubMenus() && 
						 removeSubMenus(currentVersionModule, currentVersionMenu)) {
							session.update(currentVersionMenu);
				}
			}
		}
	}
	
	@Override
	@Secured("ROLE_ADMIN_MENU")
	public void saveObject(Menu menu) throws ValidationException {
		super.saveObject(menu);
	}
	
	@Override
	public void saveObject(Menu menu, Session session) throws ValidationException {
		Assert.notNull(menu, C.MENU);
		
		if (menu.getParent() != null && menu.getModule() != null) {
			throw new IllegalStateException("child menu must not belong to a module");
		}
		super.saveObject(menu, session);
	}
	
	@Override
	@Secured("ROLE_ADMIN_MENU")
	public void deleteObject(Menu menu) throws ValidationException {
		super.deleteObject(menu);
	}
	
	private List<Menu> filterDefaultMenu(List<Menu> menus) {
		return subList(menus, menu -> !menu.getName().equals(getDefaultMenuName()));
	}
	
	private boolean removeSubMenus(Module module, Menu menu) {
		boolean subMenuChanged = false;
		for (Menu subMenu : new ArrayList<>(menu.getSubMenus())) {
			if (subMenu.getForm() != null &&
				module.getFormByUid(subMenu.getFormUid()) == null) {
					menu.removeSubMenu(subMenu);
					subMenuChanged = true;
			}
		}
		return subMenuChanged;
	}
	
}
