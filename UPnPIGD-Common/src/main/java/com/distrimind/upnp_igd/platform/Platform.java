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

package com.distrimind.upnp_igd.platform;

import com.distrimind.upnp_igd.model.ModelUtil;

/**
 * @author Jason Mahdjoub
 * @since 1.2.0
 */
public enum Platform {
	ANDROID,
	DESKTOP;

	public PlatformUpnpServiceConfiguration getInstance()
	{
		return PlatformUpnpServiceConfiguration.getInstance(this);
	}
	public static Platform getDefault()
	{
		if (ModelUtil.ANDROID_RUNTIME || ModelUtil.ANDROID_EMULATOR)
			return ANDROID;
		else
			return DESKTOP;
	}
}
