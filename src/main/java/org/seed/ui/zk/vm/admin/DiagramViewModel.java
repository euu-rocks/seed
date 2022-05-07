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

import org.seed.C;
import org.seed.core.entity.doc.DocumentationService;
import org.seed.core.util.Assert;
import org.seed.core.util.MiscUtils;
import org.seed.ui.zk.vm.AbstractApplicationViewModel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;

public class DiagramViewModel extends AbstractApplicationViewModel {
	
	@WireVariable(value="documentationServiceImpl")
	private DocumentationService documentationService;
	
	private String title;
	
	private String plantUML;
	
	public String getTitle() {
		return title;
	}

	public String getSvg() {
		return documentationService.createSVG(plantUML);
	}
	
	@Init
    public void init(@ExecutionArgParam(C.PARAM) DiagramParameter param) {
		Assert.notNull(param, C.PARAM);
		
		title = param.title;
		plantUML = param.plantUml;
	}
	
	@Command
	public void downloadPlantUml() {
		Filedownload.save(plantUML.getBytes(MiscUtils.CHARSET), "text/plain", 
						  title + '_' + MiscUtils.getTimestampString() + ".plantuml");
	}
	
}
