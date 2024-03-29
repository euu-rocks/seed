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

public enum Icons {
	
	ADDRESS_BOOK,
	ADDRESS_CARD_O,
	ADJUST,
	BAR_CHART_O,
	BARCODE,
	BED,
	BEER,
	BELL,
	BOOK,
	BOOKMARK,
	BUG,
	BUILDING,
	BULLSEYE,
	BUS,
	CALENDAR,
	CAMERA,
	CAR,
	CHAIN,
	CHECK,
	CHECK_SQUARE_O,
	CIRCLE_O,
	CLOCK_O,
	CLOUD,
	COFFEE,
	COG,
	COMMENT,
	COMMENT_O,
	CREDIT_CARD,
	CUBE,
	CUBES,
	CUTLERY,
	DATABASE,
	DESKTOP,
	DIAMOND,
	DOT_CIRCLE_O,
	EDIT,
	ENVELOPE_O,
	EYE,
	EXCHANGE,
	EXCLAMATION,
	EXTERNAL_LINK,
	FEMALE,
	FILE,
	FILE_O,
	FILE_TEXT,
	FILM,
	FIRE,
	FLAG,
	FLASK,
	FOLDER,
	FOLDER_O,
	GAVEL,
	GEARS,
	GIFT,
	GLASS,
	GLOBE,
	GROUP,
	HEADPHONES,
	HEART,
	HOME,
	IMAGE,
	INBOX,
	INDUSTRY,
	INFO,
	KEY,
	LAPTOP,
	LEAF,
	LIGHTBULB_O,
	LIST_ALT,
	LOCK,
	MAGNET,
	MALE,
	MAP,
	MAP_MARKER,
	MICROPHONE,
	MOBILE,
	MONEY,
	MUSIC,
	NAVICON,
	QUESTION,
	QRCODE,
	PAPERCLIP,
	PENCIL,
	PICTURE_O,
	PLANE,
	PLAY_CIRCLE_O,
	PUZZLE_PIECE,
	RANDOM,
	ROAD,
	ROCKET,
	SHARE_ALT,
	SHARE_SQUARE_O,
	SHIELD,
	SHOPPING_CART,
	SEARCH,
	SERVER,
	SIGNAL,
	SITEMAP,
	SQUARE,
	SQUARE_O,
	STAR,
	STAR_O,
	STETHOSCOPE,
	SUITCASE,
	SUN_O,
	TABLE,
	TAG,
	TAGS,
	THUMB_TACK,
	TICKET,
	TIMES,
	TINT,
	TROPHY,
	TRUCK,
	UMBRELLA,
	USER,
	VIDEO_CAMERA,
	WARNING,
	WRENCH,
	DUMMY_NO_ICON;
	
	public String getIconClass() {
		return "z-icon-" + 
				name().toLowerCase().replace('_', '-') + 
				" z-icon-fw alpha-icon-lg";
	}
	
}
