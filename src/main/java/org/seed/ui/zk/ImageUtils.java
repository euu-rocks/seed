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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.util.Assert;
import org.zkoss.image.AImage;

abstract class ImageUtils {
	
	private static final Font PLACEHOLDER_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	
	static byte[] createPlaceholderImage(String text, int width, int height) {
		Assert.notNull(text, "text is null");
		
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = image.createGraphics();
		final FontMetrics metrics = g2d.getFontMetrics(PLACEHOLDER_FONT);
		final int x = (width - metrics.stringWidth(text)) / 2;
		final int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
		
		g2d.setFont(PLACEHOLDER_FONT);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRect(0, 0, width - 1, height - 1);
		
		g2d.setColor(Color.BLACK);
		g2d.drawString(text, x, y);
		return getBytes(image);
	}
	
	static AImage createThumbnail(byte[] bytes, int thumbnailWidth) {
		Assert.notNull(bytes, "bytes is null");
		
		final BufferedImage image = getImage(bytes);
		final double ratio = ((double) thumbnailWidth) / image.getWidth(); 
		final int thumbnailHeight = (int) (image.getHeight() * ratio);
		final BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, 
														  BufferedImage.TYPE_INT_RGB);
		thumbnail.createGraphics().drawImage(
				image.getScaledInstance(thumbnailWidth, thumbnailHeight, 
										java.awt.Image.SCALE_SMOOTH), 
				0, 0, null);
		return getAImage(thumbnail);
	}
	
	private static byte[] getBytes(BufferedImage image) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(image, "png", baos);
			return baos.toByteArray(); 
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static BufferedImage getImage(byte[] bytes) {
		try {
			return ImageIO.read(new ByteArrayInputStream(bytes));
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static AImage getAImage(BufferedImage image) {
		try {
			return new AImage(null, getBytes(image)); 
		} 
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
