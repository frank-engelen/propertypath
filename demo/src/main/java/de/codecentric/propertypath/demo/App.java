package de.codecentric.propertypath.demo;

import de.codecentric.propertypath.api.PropertyPath;
import de.codecentric.propertypath.demolib.Address;

/**
 * Hello world!
 * 
 */
public class App {
    public static void main(String[] args) {
	System.out.println("Hello World!");

	Person p = new Person();
	p.setAddress(new Address());

	final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
	final PropertyPath<Person, String> cityPath = Person.PROPERTIES.address.city;
	System.out.println(namePath.sameOriginClass(cityPath));

	PropertyPath<Person, String> streetPath = Person.PROPERTIES.address.streetAndNumber;
	streetPath.set(p, "Homestr.1");
	Address address = p.getAddress();
	String streetAndNumber = address.getStreetAndNumber();
	System.out.println(streetAndNumber);

	System.out.println(p == Person.PROPERTIES.get(p));

	Person.PROPERTIES.set(p, p);
    }
}
