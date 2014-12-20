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

	PersonProperties<Person> personProperties = PersonProperties.create();

	PropertyPath<Person> namePath = personProperties.name;
	PropertyPath<Person> cityPath = personProperties.address.city;
	System.out.println(namePath.sameOriginClass(cityPath));

	PropertyPath<Person> streetPath = personProperties.address.streetAndNumber;
	streetPath.set(p, "Homestr.1");
	System.out.println(p.getAddress().getStreetAndNumber());
    }
}
