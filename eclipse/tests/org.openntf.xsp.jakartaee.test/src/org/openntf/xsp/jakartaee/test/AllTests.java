package org.openntf.xsp.jakartaee.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.openntf.xsp.jakartaee.test.beanvalidation.TestValidationBasics;
import org.openntf.xsp.jakartaee.test.jsonb.TestJsonb;
import org.openntf.xsp.jakartaee.test.jsonp.TestJsonp;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestValidationBasics.class,
	
	TestJsonb.class,
	TestJsonp.class
})
public class AllTests {

}
