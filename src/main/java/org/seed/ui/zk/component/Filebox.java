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

import javax.annotation.Nullable;

import org.seed.Seed;
import org.seed.core.form.layout.LayoutElementAttributes;
import org.seed.core.util.Assert;
import org.seed.ui.zk.FileTypeIcons;
import org.seed.ui.zk.UIUtils;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.annotation.ComponentAnnotation;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Inputgroup;
import org.zkoss.zul.Textbox;

public class Filebox extends Inputgroup implements EventListener<Event> {
	
	private static final long serialVersionUID = -8712100989950712695L;

	private final A elemIcon;
	
	private final Textbox elemFilename;
	
	private final Button buttonDelete;
	
	private final Button buttonUpload;
	
	private byte[] content;
	
	private String contentType;
	
	private String fileName;
	
	private boolean readonly;
	
	private boolean mandatory;
	
	private boolean disabled;
	
	public Filebox() {
		// file icon
		elemIcon = new A();
		elemIcon.setStyle("color:black");
		elemIcon.setTooltiptext(Seed.getLabel("label.downloadfile"));
		elemIcon.setIconSclass("z-icon-file-o alpha-icon-lg");
		elemIcon.addEventListener(Events.ON_CLICK, this);
		
		// filename textbox
		elemFilename = new Textbox();
		elemFilename.setHflex(LayoutElementAttributes.V_1);
		elemFilename.setReadonly(true);
		elemFilename.setStyle("cursor:pointer");
		elemFilename.setPlaceholder(Seed.getLabel("label.uploadfile"));
		elemFilename.addEventListener(Events.ON_CLICK, this);
		
		// delete button
		buttonDelete = new Button();
		buttonDelete.setTooltiptext(Seed.getLabel("label.deletefile"));
		buttonDelete.setIconSclass("z-icon-remove alpha-icon-lg");
		buttonDelete.addEventListener(Events.ON_CLICK, this);
		
		// upload button
		buttonUpload = new Button();
		buttonUpload.setUpload(LayoutElementAttributes.V_TRUE);
		buttonUpload.setTooltiptext(Seed.getLabel("label.uploadfile"));
		buttonUpload.setIconSclass("z-icon-arrow-up alpha-icon-lg");
		buttonUpload.addEventListener(Events.ON_UPLOAD, this);
		
		appendChild(elemIcon);
		appendChild(elemFilename);
		appendChild(buttonDelete);
		appendChild(buttonUpload);
		enableButtons();
	}
	
	@ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=onChange)")
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
		if (mandatory) {
			updateMandatoryStatus();
		}
		enableButtons();
	}
	
	@ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=onChange)")
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
		elemIcon.setIconSclass(contentType != null 
								? FileTypeIcons.getIcon(contentType) 
								: "z-icon-file-o alpha-icon-lg");
	}
	
	@ComponentAnnotation("@ZKBIND(ACCESS=both, SAVE_EVENT=onChange)")
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
		elemFilename.setText(fileName);
	}
	
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
		enableButtons();
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
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		enableButtons();
	}

	@Override
	public void onEvent(Event event) throws Exception {
		if (disabled) {
			return;
		}
		// download
		if (event.getTarget() == elemIcon || event.getTarget() == elemFilename) {
			if (content != null) {
				Filedownload.save(content, contentType, fileName);
			}
		}
		// upload
		else if (event.getTarget() == buttonUpload) {
			initContent(((UploadEvent) event).getMedia());
		}
		// delete
		else if (event.getTarget() == buttonDelete) {
			initContent(null);
		}
	}
	
	private void initContent(@Nullable Media media) {
		Assert.state(!readonly, "filebox is readonly");
		
		setContent(UIUtils.getBytes(media));
		setContentType(media != null ? media.getContentType() : null);
		setFileName(media != null ? trimFileName(media.getName()) : null);
		postOnChangeEvent(this, content);
	}
	
	private void enableButtons() {
		buttonDelete.setVisible(!readonly && content != null);
		buttonDelete.setDisabled(disabled);
		buttonUpload.setDisabled(disabled || readonly);
	}
	
	private void updateMandatoryStatus() {
		setStyle(content != null ?  null : STYLE_MANDATORY); 
	}
	
	private static String trimFileName(String fileName) {
		return fileName != null && fileName.length() > 255
				? fileName.substring(fileName.length() - 255)
				: fileName;
	}
	
}
