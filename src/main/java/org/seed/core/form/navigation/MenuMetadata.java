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
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.builder.EqualsBuilder;

import org.seed.C;
import org.seed.core.application.AbstractApplicationEntity;
import org.seed.core.form.Form;
import org.seed.core.form.FormMetadata;
import org.seed.core.util.Assert;

@javax.persistence.Entity
@Table(name = "sys_menu")
public class MenuMetadata extends AbstractApplicationEntity implements Menu {
	
	@OneToMany(mappedBy = "parent",
			   cascade = CascadeType.ALL,
			   orphanRemoval = true,
			   fetch = FetchType.LAZY)
	private List<MenuMetadata> children;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
	private MenuMetadata parent;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id")
	private FormMetadata form;
	
	private String icon;
	
	@Transient
	private String formUid;
	
	@Override
	public boolean hasParent() {
		return parent != null;
	}
	
	@Override
	@XmlTransient
	public Menu getParent() {
		return parent;
	}

	public void setParent(Menu parent) {
		this.parent = (MenuMetadata) parent;
	}
	
	@Override
	@XmlTransient
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = (FormMetadata) form;
	}
	
	@Override
	@XmlAttribute
	public String getIcon() {
		return icon;
	}
	
	@Override
	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	@XmlAttribute
	public String getFormUid() {
		return form != null ? form.getUid() : formUid;
	}

	public void setFormUid(String formUid) {
		this.formUid = formUid;
	}

	@Override
	public boolean hasSubMenus() {
		return notEmpty(getChildren());
	}
	
	@Override
	public List<Menu> getSubMenus() {
		return hasSubMenus() ? Collections.unmodifiableList(getChildren()) : null;
	}
	
	@Override
	public void addSubMenu(Menu menu) {
		Assert.notNull(menu, C.MENU);
		
		MenuMetadata child = (MenuMetadata) menu;
		if (children == null) {
			children = new ArrayList<>();
		}
		child.setParent(this);
		children.add(child);
	}
	
	@Override
	public void removeSubMenu(Menu menu) {
		Assert.notNull(menu, C.MENU);
		
		getChildren().remove(menu);
	}
	
	@XmlElement(name="child")
	@XmlElementWrapper(name="children")
	public List<MenuMetadata> getChildren() {
		return children;
	}

	public void setChildren(List<MenuMetadata> children) {
		this.children = children;
	}
	
	@Override
	public Menu getChildByUid(String childUid) {
		Assert.notNull(childUid, "childUid");
		
		return getObjectByUid(getChildren(), childUid);
	}
	
	@Override
	public void removeNewObjects() {
		removeNewObjects(getChildren());
	}
	
	@Override
	public boolean isEqual(Object other) {
		if (!isInstance(other)) {
			return false;
		}
		if (other == this) {
			return true;
		}
		final Menu otherMenu = (Menu) other;
		if (!new EqualsBuilder()
			.append(formUid, otherMenu.getFormUid())
			.append(icon, otherMenu.getIcon())
			.isEquals()) {
			return false;
		}
		return isEqualSubMenus(otherMenu);
	}
	
	private boolean isEqualSubMenus(Menu otherMenu) {
		return !(anyMatch(getSubMenus(), subMenu -> !subMenu.isEqual(otherMenu.getChildByUid(subMenu.getUid()))) ||
				 anyMatch(otherMenu.getSubMenus(), subMenu -> getChildByUid(subMenu.getUid()) == null));
	}
	
	@Override
	public void initUid() {
		super.initUid();
		initUids(getChildren());
	}

}
