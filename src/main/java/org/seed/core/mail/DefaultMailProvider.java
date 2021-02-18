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

import java.io.InputStream;

import org.seed.core.api.Mail;
import org.seed.core.api.MailProvider;
import org.seed.core.config.ApplicationContextProvider;

import org.springframework.util.Assert;

public class DefaultMailProvider implements MailProvider {
	
	private class DefaultMail implements Mail {
		
		private final MailBuilder mailBuilder;
		
		private final StringBuilder textBuilder = new StringBuilder();

		private DefaultMail(MailBuilder builder) {
			this.mailBuilder = builder;
		}
		
		@Override
		public Mail setToAddress(String... toAddress) {
			mailBuilder.setToAddress(toAddress);
			return this;
		}
		
		@Override
		public Mail setCcAddress(String... ccAddress) {
			mailBuilder.setCcAddress(ccAddress);
			return this;
		}

		@Override
		public Mail setSubject(String subject) {
			mailBuilder.setSubject(subject);
			return this;
		}
		
		@Override
		public Mail addAttachment(String fileName, String contentType, InputStream inputStream) {
			mailBuilder.addAttachment(fileName, contentType, inputStream);
			return this;
		}

		@Override
		public Mail addText(String text) {
			Assert.notNull(text, "text is null");
			
			textBuilder.append(text);
			return this;
		}
		
		private void commitText() {
			mailBuilder.setText(textBuilder.toString());
		}

	}
	
	private final MailService mailService;
	
	public DefaultMailProvider() {
		mailService = ApplicationContextProvider.getBean(MailService.class);
	}
	
	@Override
	public boolean isMailingEnabled() {
		return mailService.isMailingEnabled();
	}

	@Override
	public Mail createMail() {
		return new DefaultMail(mailService.getMailBuilder());
	}

	@Override
	public void sendMail(Mail mail) {
		final DefaultMail mailImpl = (DefaultMail) mail;
		mailImpl.commitText();
		mailService.sendMail(mailImpl.mailBuilder.build());
	}

}
