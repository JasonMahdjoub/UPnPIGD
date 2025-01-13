package com.distrimind.upnp_igd.android;

import com.distrimind.upnp_igd.android.test.transport.JDKServerJDKClientTest;
import com.distrimind.upnp_igd.android.test.transport.JDKServerJettyClientTest;
import com.distrimind.upnp_igd.android.test.transport.JettyServerJDKClientTest;
import com.distrimind.upnp_igd.android.test.transport.JettyServerJettyClientTest;
import com.distrimind.upnp_igd.android.test.transport.StreamServerClientTest;
import com.distrimind.xmllib.TestGroup;
import com.distrimind.xmllib.Tests;

import java.util.List;

public class AllTests extends com.distrimind.upnp_igd.test.AllTests {
    public static final int DEFAULT_THREAD_COUNT=12;
    static
    {
        com.distrimind.upnp_igd.test.AllTests.additionalTestGroups= List.of(
                new TestGroup("testTransport", List.of(
                        JDKServerJDKClientTest.class,
                        JDKServerJettyClientTest.class,
                        JettyServerJDKClientTest.class,
                        JettyServerJettyClientTest.class,
                        StreamServerClientTest.class

                ))
        );
    }
    public static Tests getTests()
    {
        return com.distrimind.upnp_igd.test.AllTests.getTests();
    }
}
