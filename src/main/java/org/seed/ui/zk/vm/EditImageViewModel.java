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

import org.seed.C;
import org.seed.core.entity.value.ValueObjectService;
import org.seed.core.util.Assert;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.Image;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

public class EditImageViewModel extends AbstractApplicationViewModel {
	
	@Wire("#editImageWin")
	private Window window;
	
	@WireVariable(value="valueObjectServiceImpl")
	private ValueObjectService valueObjectService;
	
	private EditImageParameter parameter;
	
	private byte[] image;
	
	@Init
    public void init(@ContextParam(ContextType.VIEW) Component view,
    				 @ExecutionArgParam(C.PARAM) EditImageParameter param) {
		Assert.notNull(param, C.PARAM);
		parameter = param;
		image = valueObjectService.getValue(parameter.valueObject, parameter.entityField);
		wireComponents(view);
	}
	
	public byte[] getImage() {
		return image;
	}
	
	@Command
	public void cancel() {
		window.detach();
	}
	
	@Command
	public void apply() {
		window.detach();
		valueObjectService.setValue(parameter.valueObject, parameter.entityField, image);
		parameter.parentVM.notifyPropertyChange(parameter.valueObject, parameter.entityField);
	}
	
	@Command
	@NotifyChange("image")
	public void removeImage() {
		image = null;
	}
	
	@Command
	@NotifyChange("image")
	public void uploadImage(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		final Event event = ctx.getTriggerEvent();
		if (event instanceof UploadEvent) {
			final Media media = ((UploadEvent) event).getMedia();
			if (media instanceof Image) {
				image = media.getByteData();
			}
		}
	}
	
}
