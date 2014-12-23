package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.Property;
import de.codecentric.propertypath.api.WithProperties;
import de.codecentric.propertypath.demolib.Address;

@WithProperties
public class Person {

    public static final PersonProperties<Person, Person> PROPERTIES = PersonProperties.create();

    @Property
    private String name;

    @Property
    private String surname;

    @Property
    private Address address;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getSurname() {
	return surname;
    }

    public void setSurname(String surname) {
	this.surname = surname;
    }

    public Address getAddress() {
	return address;
    }

    public void setAddress(Address address) {
	this.address = address;
    }

}
