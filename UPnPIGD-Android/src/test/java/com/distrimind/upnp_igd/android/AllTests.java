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


package com.distrimind.upnp_igd.android;

import com.distrimind.flexilogxml.FlexiLogXML;
import com.distrimind.flexilogxml.TestGroup;
import com.distrimind.flexilogxml.Tests;
import com.distrimind.upnp_igd.android.transport.JDKServerJDKClientTest;
import com.distrimind.upnp_igd.android.transport.JDKServerUndertowClientTest;
import com.distrimind.upnp_igd.android.transport.UndertowServerJDKClientTest;
import com.distrimind.upnp_igd.android.transport.UndertowServerUndertowClientTest;
import com.distrimind.flexilogxml.log.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllTests {
	public static List<TestGroup> additionalTestGroups=null;
	public static Tests getTests() {
		List<TestGroup> testGroups=List.of(
				new TestGroup("TestStreamServerClient", List.of(
						JDKServerJDKClientTest.class,
						UndertowServerUndertowClientTest.class,
						UndertowServerJDKClientTest.class,
						JDKServerUndertowClientTest.class
				))
		);
		if (additionalTestGroups!=null)
		{
			List<TestGroup> l=new ArrayList<>();
			l.addAll(testGroups);
			l.addAll(additionalTestGroups);
			testGroups=l;

		}
		return new Tests(testGroups);
	}
	public static void main(String[] args) throws IOException {
		Tests t=getTests();
		File f=new File("./UPnPIGD-Android/src/test/resources/com/distrimind/upnp_igd/android/AllTestsNG.xml");
		t.saveTestNGToXML(f);
		FlexiLogXML.log(Level.INFO, "XML Test NG file saved into: "+f.getCanonicalPath());
	}
}
