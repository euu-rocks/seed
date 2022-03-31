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

import static org.seed.ui.zk.component.ComponentUtils.*;

import org.seed.ui.zk.UIUtils;

import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;

@SuppressWarnings("serial")
public class RichTextArea extends Div implements EventListener<Event> {
	
	private static final String PATH_EDITOR = "/component/richtexteditor.zul";  //NOSONAR
	
	private Html html;
	
	private A aEditMode;
	
	private boolean readonly;
	
	private boolean mandatory;
	
	public RichTextArea() {
		setVflex("1");
		
		html = new Html();
		appendChild(html);
		
		final Div div = new Div();
		div.setClass("alpha-richtext-button");
		aEditMode = new A();
		aEditMode.setIconSclass("z-icon-wrench");
		aEditMode.setLabel(' ' + getLabel("label.edit"));
		aEditMode.addEventListener(Events.ON_CLICK, this);
		div.appendChild(aEditMode);
		appendChild(div);
	}
	
	@ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=onChange)")
	public String getValue() {
		return html.getContent();
	}

	public void setValue(String value) {
		html.setContent(value);
		if (mandatory) {
			updateMandatoryStatus();
		}
	}
	
	public void setValueFromEditor(String value) {
		setValue(value);
		postOnChangeEvent(this, getValue());
	}
	
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
		aEditMode.setVisible(!readonly);
	}
	
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
		if (mandatory) {
			updateMandatoryStatus();
		}
		else {
			setStyle(null);
		}
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		if (!readonly) {
			UIUtils.showDialog(PATH_EDITOR, this);
		}
	}
	
	private void updateMandatoryStatus() {
		setStyle(StringUtils.hasText(getValue()) ?  null : STYLE_MANDATORY); 
	}
	
}
