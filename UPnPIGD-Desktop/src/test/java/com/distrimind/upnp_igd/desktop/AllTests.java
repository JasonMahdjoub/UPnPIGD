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


package com.distrimind.upnp_igd.desktop;

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.flexilogxml.Tests;
import org.slf4j.event.Level;

import java.io.File;
import java.io.IOException;

public class AllTests extends com.distrimind.upnp_igd.test.AllTests {
	public static Tests getTests()
	{
		return com.distrimind.upnp_igd.test.AllTests.getTests();
	}
	public static void main(String[] args) throws IOException {
		Tests t=getTests();
		File f=new File("./UPnPIGD-Desktop/src/test/resources/com/distrimind/upnp_igd/desktop/AllTestsNG.xml");
		t.saveTestNGToXML(f);
		FlexiLogXML.log(Level.INFO, "XML Test NG file saved into: "+f.getCanonicalPath());
	}
}

