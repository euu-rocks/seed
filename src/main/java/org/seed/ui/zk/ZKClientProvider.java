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
package org.seed.ui.zk;

import org.seed.LabelProvider;
import org.seed.core.api.ClientProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zkoss.zul.Messagebox;

@Component
public class ZKClientProvider implements ClientProvider {
	
	@Autowired
	private LabelProvider labelProvider; 
	
	@Override
	public void showInfoMessage(String message) {
		showInfoMessage(message, labelProvider.getLabel("label.information"));
	}
	
	@Override
	public void showInfoMessage(String message, String title) {
		Messagebox.show(message, title, Messagebox.OK, Messagebox.INFORMATION);
	}
	
	@Override
	public void showWarnMessage(String message) {
		showWarnMessage(message, labelProvider.getLabel("label.warning"));
	}
	
	@Override
	public void showWarnMessage(String message, String title) {
		Messagebox.show(message, title, Messagebox.OK, Messagebox.EXCLAMATION);
	}
	
	@Override
	public void showErrorMessage(String message) {
		showErrorMessage(message, labelProvider.getLabel("label.error"));
	}
	
	@Override
	public void showErrorMessage(String message, String title) {
		Messagebox.show(message, title, Messagebox.OK, Messagebox.ERROR);
	}

}
