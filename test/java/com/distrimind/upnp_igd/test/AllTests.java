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


package com.distrimind.upnp_igd.test;

import com.distrimind.flexilogxml.TestGroup;
import com.distrimind.flexilogxml.Tests;
import com.distrimind.upnp_igd.test.control.*;
import com.distrimind.upnp_igd.test.gena.EventXMLProcessingTest;
import com.distrimind.upnp_igd.test.gena.IncomingSubscriptionLifecycleTest;
import com.distrimind.upnp_igd.test.gena.OutgoingSubscriptionFailureTest;
import com.distrimind.upnp_igd.test.gena.OutgoingSubscriptionLifecycleTest;
import com.distrimind.upnp_igd.test.local.*;
import com.distrimind.upnp_igd.test.model.*;
import com.distrimind.upnp_igd.test.model.profile.DeviceDetailsProviderTest;
import com.distrimind.upnp_igd.test.protocol.ProtocolFactoryTest;
import com.distrimind.upnp_igd.test.resources.*;
import com.distrimind.upnp_igd.test.ssdp.*;
import example.binarylight.BinaryLightTest;
import example.controlpoint.*;
import example.igd.ConnectionInfoTest;
import example.igd.PortMappingTest;
import example.localservice.*;
import example.registry.RegistryBrowseTest;
import example.registry.RegistryListenerTest;

import java.util.ArrayList;
import java.util.List;

public class AllTests {
	public static List<TestGroup> additionalTestGroups=null;
	public static Tests getTests() {
		List<TestGroup> testGroups=List.of(
				new TestGroup("testControl", List.of(
						ActionInvokeIncomingTest.class,
						ActionInvokeOutgoingTest.class,
						ActionXMLProcessingTest.class,
						InvalidActionXMLProcessingTest.class
				)),
				new TestGroup("testGena", List.of(
						EventXMLProcessingTest.class,
						IncomingSubscriptionLifecycleTest.class,
						InvalidActionXMLProcessingTest.class,
						OutgoingSubscriptionFailureTest.class,
						OutgoingSubscriptionLifecycleTest.class
				)),
				new TestGroup("testLocal", List.of(
						LocalActionInvocationCSVTest.class,
						LocalActionInvocationDatatypesTest.class,
						LocalActionInvocationEnumTest.class,
						LocalActionInvocationNullTest.class,
						LocalDeviceBindingAdvertisementTest.class
				)),
				new TestGroup("testModel", List.of(
						DeviceDetailsProviderTest.class,
						DatagramParsingTest.class,
						DatatypesTest.class,
						DeviceGraphTest.class,
						HeaderParsingTest.class,
						IconTest.class,
						IncompatibilityTest.class,
						LocalServiceBindingDatatypesTest.class,
						UtilTest.class
				)),
				new TestGroup("testProtocol", List.of(
						ProtocolFactoryTest.class
				)),
				new TestGroup("testResources", List.of(
						DeviceDescriptorRetrievalTest.class,
						InvalidUDA10DeviceDescriptorParsingTest.class,
						ServiceDescriptorRetrievalTest.class,
						UDA10DeviceDescriptorParsingTest.class,
						UDA10ServiceDescriptorParsingTest.class
				)),
				new TestGroup("testSSDP", List.of(
						AdvertisementTest.class,
						NotifyAliveConcurrentTest.class,
						NotifyTest.class,
						RegistryExpirationTest.class,
						SearchReceivedTest.class,
						SearchResponseTest.class
				)),
				new TestGroup("binary_light", List.of(
						BinaryLightTest.class
				)),
				new TestGroup("control_point", List.of(
						ActionCancellationTest.class,
						ActionInvocationTest.class,
						EventSubscriptionTest.class,
						SearchExecuteTest.class,
						SwitchPowerWithInterruption.class
				)),
				new TestGroup("igd", List.of(
						ConnectionInfoTest.class,
						PortMappingTest.class
				)),
				new TestGroup("local_service", List.of(
						AllowedValueRangeTest.class,
						AllowedValueTest.class,
						BasicBindingTest.class,
						EnumTest.class,
						EventProviderTest.class,
						RemoteClientInfoTest.class,
						StringConvertibleTest.class
				)),
				new TestGroup("registry", List.of(
						RegistryBrowseTest.class,
						RegistryListenerTest.class
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
}
