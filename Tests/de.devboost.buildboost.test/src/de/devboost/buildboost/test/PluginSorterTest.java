/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.buildboost.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.devboost.buildboost.artifacts.Plugin;
import de.devboost.buildboost.model.IDependable;
import de.devboost.buildboost.util.Sorter;

public class PluginSorterTest {

	private Plugin p1;
	private Plugin p2;
	private Plugin p3;
	private Plugin p4;
	private Plugin p5;

	@Before
	public void setUp() throws Exception {
		p1 = createPlugin("p1");
		p2 = createPlugin("p2");
		p3 = createPlugin("p3");
		p4 = createPlugin("p4");
		p5 = createPlugin("p5");
	}

	@Test
	public void testSorting1() throws Exception {
		p2.addDependency(p1);
		p3.addDependency(p2);
		p4.addDependency(p3);
		p5.addDependency(p4);
		p5.addDependency(p1);
		
		List<IDependable> plugins = new ArrayList<IDependable>();
		plugins.add(p3);
		plugins.add(p5);
		plugins.add(p2);
		plugins.add(p4);
		plugins.add(p1);
		
		plugins = new Sorter().topologicalSort(plugins);
		assertSame(p1, plugins.get(0));
		assertSame(p2, plugins.get(1));
		assertSame(p3, plugins.get(2));
		assertSame(p4, plugins.get(3));
		assertSame(p5, plugins.get(4));
	}

	@Test
	public void testBucketing1() throws Exception {
		p2.addDependency(p1);
		p3.addDependency(p2);
		p4.addDependency(p3);
		p5.addDependency(p4);
		
		List<IDependable> plugins = new ArrayList<IDependable>();
		plugins.add(p3);
		plugins.add(p5);
		plugins.add(p2);
		plugins.add(p4);
		plugins.add(p1);
		
		List<List<IDependable>> buckets = new Sorter().sortTopologicallyToBuckets(plugins);
		assertBucket(buckets.get(0), p1);
		assertBucket(buckets.get(1), p2);
		assertBucket(buckets.get(2), p3);
		assertBucket(buckets.get(3), p4);
		assertBucket(buckets.get(4), p5);
	}

	@Test
	public void testBucketing2() throws Exception {
		p2.addDependency(p1);
		p3.addDependency(p1);
		p4.addDependency(p3);
		p5.addDependency(p1);
		p5.addDependency(p3);
		
		List<IDependable> plugins = new ArrayList<IDependable>();
		plugins.add(p3);
		plugins.add(p5);
		plugins.add(p2);
		plugins.add(p4);
		plugins.add(p1);
		
		List<List<IDependable>> buckets = new Sorter().sortTopologicallyToBuckets(plugins);
		System.out.println("buckets = " + buckets);
		assertBucket(buckets.get(0), p1);
		assertBucket(buckets.get(1), p2, p3);
		assertBucket(buckets.get(2), p4, p5);
	}

	private void assertBucket(List<IDependable> bucket, Plugin... expectedContent) {
		assertEquals("Size of bucket is wrong", expectedContent.length, bucket.size());
		for (Plugin next : expectedContent) {
			assertTrue("Expected element not found in bucket", bucket.contains(next));
		}
	}

	private Plugin createPlugin(String name) throws Exception {
		return new Plugin(new File(name));
	}

	@Test
	public void testSorting2() {
		
		List<IDependable> plugins = new ArrayList<IDependable>();
		plugins = new Sorter().topologicalSort(plugins);
	}

	@Test
	public void testSorting3() throws Exception {
		p2.addDependency(p1);
		
		List<IDependable> plugins = new ArrayList<IDependable>();
		// p1 is missing from the list!
		plugins.add(p2);
		try {
			plugins = new Sorter().topologicalSort(plugins);
			fail("Sorting must fail, because p1 was not passed to the sorter.");
		} catch (Exception e) {
			// we expect this
			assertTrue(e.getMessage().contains("some dependencies are missing"));
		}
	}

	@Test
	public void testCycle1() throws Exception {
		p2.addDependency(p1);
		p3.addDependency(p2);
		p1.addDependency(p3);
		
		List<IDependable> cycle = new Sorter().findCycle(p1);
		System.out.println("PluginSorterTest.testCycle() " + cycle);
		assertNotNull(cycle);
		assertEquals(3, cycle.size());
	}

	@Test
	public void testCycle2() throws Exception {
		p1.addDependency(p1);
		
		List<IDependable> cycle = new Sorter().findCycle(p1);
		System.out.println("PluginSorterTest.testCycle() " + cycle);
		assertNotNull(cycle);
		assertEquals(1, cycle.size());
	}
}
