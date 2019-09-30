package org.openntf.xsp.jsonapi.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openntf.xsp.jsonapi.test.jsonb.TestJsonb;
import org.openntf.xsp.jsonapi.test.jsonp.TestJsonp;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestJsonp.class,
	TestJsonb.class
})
public class AllTests {

}
