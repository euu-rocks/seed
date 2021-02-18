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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

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
import org.seed.core.form.LabelProvider;
import org.seed.core.user.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class MenuServiceImpl extends AbstractApplicationEntityService<Menu>
	implements MenuService, FormDependent, FormChangeAware {
	
	@Autowired
	private FormService formService;
	
	@Autowired
	private MenuRepository menuRepository;
	
	@Autowired
	private MenuValidator menuValidator;
	
	@Autowired
	private LabelProvider labelProvider;
	
	@Override
	public List<Menu> getTopLevelMenus() {
		return menuRepository.find(queryParam("parent", QueryParameter.IS_NULL));
	}
	
	@Override
	public List<Menu> findObjectsWithoutModule() {
		return menuRepository.find(queryParam("parent", QueryParameter.IS_NULL),
								   queryParam("module", QueryParameter.IS_NULL));
	}
	
	@Override
	public List<Menu> getMenus(User user) {
		Assert.notNull(user, "user is null");
		
		final List<Menu> result = new ArrayList<>();
		for (Menu menu : getTopLevelMenus()) {
			if (menu.getForm() == null || 
				menu.getForm().getEntity().checkPermissions(user, EntityAccess.READ)) {
				result.add(menu);
			}
		}
		return result;
	}
	
	@Override
	@Secured("ROLE_ADMIN_MENU")
	public Menu createMenuEntry(Menu menu, Form form) {
		Assert.notNull(menu, "menu is null");
		Assert.notNull(form, "form is null");
		
		final MenuMetadata entry = new MenuMetadata();
		entry.setName(form.getName());
		entry.setForm(form);
		entry.setParent(menu);
		return entry;
	}
	
	@Override
	public List<Menu> findUsage(Form form) {
		Assert.notNull(form, "form is null");
		
		return menuRepository.find(queryParam("form", form));
	}
	
	@Override
	public void notifyCreate(Form form, Session session) {
		Assert.notNull(form, "form is null");
		Assert.notNull(session, "session is null");
		try {
			final FormOptions formOptions = form.getOptions();
			if (formOptions != null) {
				Menu parentMenu = formOptions.getMenu();
				if (parentMenu == null) {
					final String defaultMenuName = labelProvider.getLabel("label.defaultmenu");
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
			throw new RuntimeException(vex);
		}
	}
	
	@Override
	public void notifyChange(Form object, Session session) {
		// do nothing
	}
	
	@Override
	public void notifyDelete(Form form, Session session) {
		Assert.notNull(form, "form is null");
		Assert.notNull(session, "session is null");
		
		for (Menu menu : menuRepository.find(session, queryParam("form", form))) {
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
	public void analyzeObjects(ImportAnalysis analysis, Module currentVersionModule) {
		Assert.notNull(analysis, "analysis is null");
		
		if (analysis.getModule().getMenus() != null) {
			for(Menu menu : analysis.getModule().getMenus()) {
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
		if (currentVersionModule != null && currentVersionModule.getMenus() != null) {
			for (Menu currentVersionMenu : currentVersionModule.getMenus()) {
				if (analysis.getModule().getMenuByUid(currentVersionMenu.getUid()) == null) {
					analysis.addChangeDelete(currentVersionMenu);
				}
			}
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[] getImportDependencies() {
		return (Class<? extends ApplicationEntityService<? extends ApplicationEntity>>[]) 
				new Class[] { FormService.class };
	}
	
	@Override
	public void importObjects(TransferContext context, Session session) {
		Assert.notNull(context, "context is null");
		Assert.notNull(session, "session is null");
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
					if (menuMeta.getChildren() != null) {
						for (MenuMetadata child : menuMeta.getChildren()) {
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
					saveObject(menu, session);	
				}
			}
		}
		catch (ValidationException vex) {
			throw new RuntimeException(vex);
		}
	}
	
	@Override
	public void deleteObjects(Module module, Module currentVersionModule, Session session) {
		Assert.notNull(module, "module is null");
		Assert.notNull(currentVersionModule, "currentVersionModule is null");
		Assert.notNull(session, "session is null");
		
		if (currentVersionModule.getMenus() != null) {
			for (Menu currentVersionMenu : currentVersionModule.getMenus()) {
				if (module.getMenuByUid(currentVersionMenu.getUid()) == null) {
					session.delete(currentVersionMenu);
				}
				// delete sub menus for deleted forms
				else if (currentVersionMenu.hasSubMenus())  {
					boolean subMenuChanged = false;
					for (Menu subMenu : new ArrayList<>(currentVersionMenu.getSubMenus())) {
						if (subMenu.getForm() != null &&
							module.getFormByUid(subMenu.getFormUid()) == null) {
							currentVersionMenu.removeSubMenu(subMenu);
							subMenuChanged = true;
						}
					}
					if (subMenuChanged) {
						session.update(currentVersionMenu);
					}
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
		Assert.notNull(menu, "menu is null");
		
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
	
}
