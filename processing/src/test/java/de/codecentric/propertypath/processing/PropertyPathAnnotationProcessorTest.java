package de.codecentric.propertypath.processing;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyPathAnnotationProcessorTest {

	private PropertyPathAnnotationProcessor processor;

	@Before
	public void setup() {
		processor = new PropertyPathAnnotationProcessor();
	}

	@Test
	public void transformGetterName2PropertyNameShouldWork() {
		Assert.assertEquals("fullName", processor.transformGetterName2PropertyName("getFullName"));
	}
}
