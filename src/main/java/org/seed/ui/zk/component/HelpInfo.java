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
package org.seed.ui.zk.component;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Separator;

@SuppressWarnings("serial")
public class HelpInfo extends Div {

	private final A elemIcon;
	
	private final Popup popup;
	
	public HelpInfo() {
		popup = new Popup();
		
		elemIcon = new A();
		elemIcon.setIconSclass("z-icon-question-circle alpha-icon-lg");
		elemIcon.setStyle("color:black");
		elemIcon.setTooltip(popup);
		
		appendChild(elemIcon);
		appendChild(popup);
	}
	
	// use | as line separator in zk-label.properties
	public void setKey(String key) {
		Assert.notNull(key, "key is null");
		Assert.state(ObjectUtils.isEmpty(popup.getChildren()), "content has already been set");
		
		final String content = Labels.getLabel(key);
		Assert.state(content != null, key);
		
		boolean first = true;
		for (String line : content.split("\\|")) {
			if (first) {
				first = false;
			}
			else {
				popup.appendChild(new Separator());
			}
			popup.appendChild(new Label(line));
		}
	}
	
}
