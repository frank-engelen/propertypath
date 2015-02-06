package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;
import de.codecentric.propertypath.demolib.Address;

@WithProperties
public class OtherPerson {

	@Property
	private String name;

	@Property
	private Address address;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
