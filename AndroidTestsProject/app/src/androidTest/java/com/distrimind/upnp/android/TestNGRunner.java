/*
 * DM-XMLLib (package com.distrimind.upnp)
 * Copyright (C) 2024 Jason Mahdjoub (author, creator and contributor) (Distrimind)
 * The project was created on April 1, 2013
 *
 * jason.mahdjoub@distri-mind.fr
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 only (GPL-3.0-only),
 * as published by the Free Software Foundation.
 *
 * *Linking Exception for Jason Mahdjoub and Affiliated Entities **
 * In addition to the terms of the GPL-3.0-only, a linking exception is provided
 * for this code. This exception applies exclusively to Jason Mahdjoub
 * and any affiliated entity, including but not limited to an enterprise,
 * a company, or an association created or co-created by Jason Mahdjoub.
 * The details of this exception are specified in the file whose name starts
 * with LINKING-EXCEPTION (or a file with a similar name).
 * This file included with this code.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * version 3 along with this code, with a file whose name starts
 * with COPYING (or with a file which have a similar name).
 * If not, the GPL-3.0-only is also available here: <http://www.gnu.org/licenses/>.
 * The linking exception is provided in the file whose name starts with
 * LINKING-EXCEPTION (or with a file which have a similar name).
 */

package com.distrimind.upnp.android;




import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;


@RunWith(Parameterized.class)
public class TestNGRunner {
	@Parameterized.Parameters(name = "{index}: {0}")
	public static Collection<com.distrimind.flexilogxml.Tests[]> data() {
		return AllTestsForAndroidEmulator.getTests().getDataForJunitTests(4);
	}
	private final com.distrimind.flexilogxml.Tests tests;
	public TestNGRunner(com.distrimind.flexilogxml.Tests tests)
	{
		if (tests==null)
			throw new NullPointerException();
		this.tests=tests;
	}

	@Test
	public void allTestNG() throws Throwable {
		tests.runTestNGWithJunit("UPnP_IGD");
	}
}