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
package org.seed.core.mail;

import java.util.Properties;

import org.hibernate.Session;
import org.seed.core.application.setting.ApplicationSetting;
import org.seed.core.application.setting.ApplicationSettingService;
import org.seed.core.application.setting.Setting;
import org.seed.core.application.setting.SettingChangeAware;
import org.seed.core.config.ApplicationContextProvider;
import org.seed.core.util.MiscUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class MailServiceImpl implements MailService, SettingChangeAware {
	
	private final static Logger log = LoggerFactory.getLogger(MailService.class);
	
	@Autowired
	private ApplicationContextProvider applicationContext;
	
	private JavaMailSender mailSender;
	
	@Override
	public boolean isMailingEnabled() {
		return getSettingService().hasMailSettings();
	}
	
	@Override
	public MailBuilder getMailBuilder() {
		return new MailBuilder(getMailSender(), "noreply@seed.org");
	}
	
	@Async
	@Override
	public void sendMail(Mail ...mails) {
		Assert.notNull(mails, "mails is null");
		
		if (!isMailingEnabled()) {
			return;
		}
		for (Mail mail : mails) {
			try {
				if (mail.getMessage() != null) {
					getMailSender().send(mail.getMessage());
				}
				else {
					getMailSender().send(mail.getSimpleMessage());
				}
			}
			catch (MailException mex) {
				log.warn("Mail could not be sent: " + MiscUtils.printArray(mail.getToAddresses()) + 
						 ' ' + mail.getSubject());
			}
		}
	}
	
	@Override
	public void notifyCreate(ApplicationSetting setting, Session session) { 
		// do nothing
	}
	
	@Override
	public void notifyChange(ApplicationSetting setting, Session session) {
		mailSender = null;
	}
	
	@Override
	public void notifyDelete(ApplicationSetting setting, Session session) { 
		// do nothing
	}
	
	private JavaMailSender getMailSender() {
		if (mailSender == null) {
			final ApplicationSettingService settingService = getSettingService();
			Assert.state(settingService.hasMailSettings(), "mail settings not available");
			
			final boolean useAuth = "true".equals(settingService.getSettingOrNull(Setting.MAIL_SERVER_USE_AUTH));
			final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
			final Properties properties = mailSender.getJavaMailProperties();
			properties.put("mail.transport.protocol", "smtp");
			mailSender.setHost(settingService.getSetting(Setting.MAIL_SERVER_HOST));
			mailSender.setPort(settingService.getIntSetting(Setting.MAIL_SERVER_PORT));
			if (useAuth) {
				mailSender.setUsername(settingService.getSettingOrNull(Setting.MAIL_SERVER_USER));
				mailSender.setPassword(settingService.getSettingOrNull(Setting.MAIL_SERVER_PWD));
				properties.put("mail.smtp.auth", "true");
			}
			this.mailSender = mailSender;
			log.info("SMPT mail sender created.");
		}
		return mailSender;
	}
	
	@SuppressWarnings("static-access")
	private ApplicationSettingService getSettingService() {
		return applicationContext.getBean(ApplicationSettingService.class);
	}
	
}
