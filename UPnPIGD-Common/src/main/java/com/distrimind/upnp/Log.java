/*
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.distrimind.upnp;

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.log.Level;

public class Log {

	public static DMLogger getLogger(Class<?> clazz)
	{
		return FlexiLogXML.getLoggerInstance(clazz);
	}
	public static DMLogger getLogger(Class<?> clazz, Level level)
	{
		return FlexiLogXML.getLoggerInstance(clazz, level);
	}
	public static DMLogger getLogger(String name)
	{
		return FlexiLogXML.getLoggerInstance(name);
	}
	public static DMLogger getLogger(String name, Level level)
	{

		return FlexiLogXML.getLoggerInstance(name, level);
	}
}
