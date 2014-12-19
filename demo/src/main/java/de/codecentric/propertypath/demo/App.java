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

	PropertyPath street = new PQ_Person().address.street;

	street.set(p, "Homestr.1");

	System.out.println(p.getAddress().getStreet());
    }
}
