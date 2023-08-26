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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.seed.C;
import org.seed.InternalException;
import org.seed.core.config.SystemLog;
import org.seed.core.util.Assert;

import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MailBuilder {
	
	private abstract class AbstractMail implements Mail {

		private final String[] toAddresses;
		
		private final String subject;

		AbstractMail(String subject, String[] toAddresses) {
			this.toAddresses = toAddresses;
			this.subject = subject;
		}
		
		@Override
		public String[] getToAddresses() {
			return toAddresses;
		}
		
		@Override
		public String getSubject() {
			return subject;
		}
		
	}
	
	private class SimpleMail extends AbstractMail {
		
		private final SimpleMailMessage simpleMessage;
		
		private SimpleMail(SimpleMailMessage simpleMessage, String subject, String[] toAddresses) {
			super(subject, toAddresses);
			this.simpleMessage = simpleMessage;
		}
		
		@Override
		public MimeMessage getMessage() {
			return null;
		}
		
		@Override
		public SimpleMailMessage getSimpleMessage() {
			return simpleMessage;
		}
	}
	
	private class MimeMail extends AbstractMail {
		
		private final MimeMessage message;
		
		private MimeMail(MimeMessage message, String subject, String[] toAddresses) {
			super(subject, toAddresses);
			this.message = message;
		}
		
		@Override
		public MimeMessage getMessage() {
			return message;
		}
		
		@Override
		public SimpleMailMessage getSimpleMessage() {
			return null;
		}
	}
	
	private class Attachment {
		
		final String fileName;
		final String contentType;
		final InputStreamSource contentStream;
		
		private Attachment(String fileName, String contentType, 
				   InputStreamSource contentStream) {
			this.fileName = fileName;
			this.contentType = contentType;
			this.contentStream = contentStream;
		}
	}
	
	private final JavaMailSender mailSender;
	
	private final String fromAddress;
	
	private List<Attachment> attachments;
	
	private String[] toAddress;
	
	private String[] ccAddress;
	
	private String subject;
	
	private String text;

	MailBuilder(JavaMailSender mailSender, String fromAddress) {
		Assert.notNull(mailSender, "mailSender");
		Assert.notNull(fromAddress, "fromAddress");
		
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
	}

	public MailBuilder setToAddress(String ...toAddress) {
		Assert.notNull(toAddress, "toAddress");
		
		this.toAddress = toAddress;
		return this;
	}
	
	public MailBuilder setCcAddress(String ...ccAddress) {
		Assert.notNull(ccAddress, "ccAddress");
		
		this.ccAddress = ccAddress;
		return this;
	}
	
	public MailBuilder setSubject(String subject) {
		Assert.notNull(subject, "subject");
		
		this.subject = subject;
		return this;
	}

	public MailBuilder setText(String text) {
		Assert.notNull(text, C.TEXT);
		
		this.text = text;
		return this;
	}

	public MailBuilder addAttachment(String fileName, String contentType, 
									 final InputStream inputStream) {
		Assert.notNull(fileName, "fileName");
		Assert.notNull(contentType, "contentType");
		Assert.notNull(inputStream, "inputStream");
		
		final Attachment attachment = new Attachment(fileName, contentType, 
			new InputStreamSource() {
				
				@Override
				public InputStream getInputStream() throws IOException {
					return inputStream;
				}
				
		});
		if (attachments == null) {
			attachments = new ArrayList<>();
		}
		attachments.add(attachment);
		return this;
	}
	
	public Mail build() {
		Assert.state(toAddress != null, "toAddress");
		Assert.state(subject != null, "subject");
		Assert.state(text != null, C.TEXT);
		try {
			if (attachments != null) {
				final var message = mailSender.createMimeMessage();
				final var messageHelper = new MimeMessageHelper(message, true);
				messageHelper.setFrom(fromAddress);
				messageHelper.setTo(toAddress);
				messageHelper.setCc(ccAddress);
				messageHelper.setSubject(subject);
				messageHelper.setText(text);
				for (Attachment attachment : attachments) {
					messageHelper.addAttachment(attachment.fileName, 
												attachment.contentStream, 
												attachment.contentType);
				}
				return new MimeMail(message, subject, toAddress);
			}
			else {
				final var message = new SimpleMailMessage();
				message.setFrom(fromAddress);
				message.setTo(toAddress);
				message.setCc(ccAddress);
				message.setSubject(subject);
				message.setText(text);
				return new SimpleMail(message, subject, toAddress);
			}
		}
		catch (MessagingException mex) {
			SystemLog.logError(mex);
			throw new InternalException(mex);
		}
	}
	
}
