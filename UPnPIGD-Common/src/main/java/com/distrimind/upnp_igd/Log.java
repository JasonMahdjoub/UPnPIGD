/*
 *
 * DM-FlexiLogXML (package com.distrimind.flexilogxml)
 * Copyright (C) 2024 Jason Mahdjoub (author, creator and contributor) (Distrimind)
 * The project was created on January 11, 2025
 *
 * jason.mahdjoub@distri-mind.fr
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * /
 */

package com.distrimind.upnp_igd;

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.flexilogxml.log.DMLogger;
import org.slf4j.event.Level;


import java.time.format.DateTimeFormatter;

public class Log {
	public static DMLogger getLogger(Class<?> clazz)
	{
		return getLogger(clazz.getName());
	}
	public static DMLogger getLogger(Class<?> clazz, Level level)
	{
		return getLogger(clazz.getName(), level);
	}
	public static DMLogger getLogger(String name)
	{
		return getLogger(name, Level.INFO);
	}
	public static DMLogger getLogger(String name, Level level)
	{
		return FlexiLogXML.getLoggerInstance("["+name+"]", 10, DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"), level);
	}
}
