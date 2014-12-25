package de.codecentric.propertypath.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.codecentric.propertypath.api.PropertyPath;
import de.codecentric.propertypath.demolib.Address;
import de.codecentric.propertypath.demolib.UsAddress;
import de.codecentric.propertypath.demolib.UsAddressProperties;

public class PropertyPathIntegrationTest {

    @Test
    public void simplePathesShouldWork() {
	final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
	final PropertyPath<Person, String> cityPath = Person.PROPERTIES.address.city;

	Person person = new Person();
	person.setAddress(new Address());

	// Write direct - read via path
	person.setName("Jim");
	person.getAddress().setCity("Ratingen");
	Assert.assertEquals("Jim", namePath.get(person));
	Assert.assertEquals("Ratingen", cityPath.get(person));

	// Write via path - read direct
	namePath.set(person, "Tom");
	cityPath.set(person, "Essen");
	Assert.assertEquals("Tom", person.getName());
	Assert.assertEquals("Essen", person.getAddress().getCity());
    }

    @Test
    public void startOfPathMeansIdentity() {
	Person person = new Person();

	Assert.assertTrue(person == Person.PROPERTIES.get(person));
    }

    @Test
    public void equalsAndHashCodeShouldWork() {
	final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
	final PropertyPath<Person, String> cityPath = Person.PROPERTIES.address.city;
	final PropertyPath<Person, String> namePath2 = PersonProperties.newPersonProperties().name;
	final PropertyPath<Person, String> cityPath2 = PersonProperties.newPersonProperties().address.city;

	Assert.assertTrue(namePath.equals(namePath2));
	Assert.assertTrue(namePath.hashCode() == namePath2.hashCode());
	Assert.assertTrue(cityPath.equals(cityPath2));
	Assert.assertTrue(cityPath.hashCode() == cityPath2.hashCode());

	Assert.assertFalse(namePath.equals(cityPath));
	Assert.assertFalse(namePath.hashCode() == cityPath.hashCode());
    }

    @Test
    public void usingPathesAsHashMapKeysWork() {
	class ErrorMessages {
	    String message;

	    ErrorMessages(String message) {
		this.message = message;
	    }
	}

	final Map<PropertyPath<Person, ?>, ErrorMessages> field2Error = new HashMap<PropertyPath<Person, ?>, ErrorMessages>();

	final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
	final PropertyPath<Person, Address> addressPath = Person.PROPERTIES.address;

	field2Error.put(namePath, new ErrorMessages("Please enter a valid name!"));
	field2Error.put(addressPath, new ErrorMessages("Address not valid!"));

	Assert.assertEquals("Please enter a valid name!", field2Error.get(PersonProperties.newPersonProperties().name).message);
	Assert.assertEquals("Address not valid!", field2Error.get(PersonProperties.newPersonProperties().address).message);
    }

    @Test
    public void downcastShouldWork() {
	final UsAddress address = new UsAddress();
	address.setState("TX");
	final Person p = new Person();
	p.setAddress(address);

	@SuppressWarnings("unchecked")
	final Object readState = Person.PROPERTIES.address._downcast(UsAddressProperties.class).state.get(p);
	Assert.assertEquals("TX", readState);
    }

    @Test
    public void serializationShouldWork() {
	final PropertyPath<Person, String> namePathOrig = Person.PROPERTIES.name;
	final PropertyPath<Person, String> cityPathOrig = Person.PROPERTIES.address.city;

	final PropertyPath<Person, String> namePath = deepCopy(namePathOrig);
	final PropertyPath<Person, String> cityPath = deepCopy(cityPathOrig);

	Person person = new Person();
	person.setAddress(new Address());

	// Write direct - read via path
	person.setName("Jim");
	person.getAddress().setCity("Ratingen");
	Assert.assertEquals("Jim", namePath.get(person));
	Assert.assertEquals("Ratingen", cityPath.get(person));

	// Write via path - read direct
	namePath.set(person, "Tom");
	cityPath.set(person, "Essen");
	Assert.assertEquals("Tom", person.getName());
	Assert.assertEquals("Essen", person.getAddress().getCity());

    }

    private <T extends Serializable> T deepCopy(T source) {
	if (source == null) {
	    return null;
	}

	try {
	    final ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
	    final ObjectOutputStream oos = new ObjectOutputStream(bos);
	    oos.writeObject(source);
	    oos.flush();
	    oos.close();
	    bos.flush();
	    bos.close();

	    final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	    final ObjectInputStream ois = new ObjectInputStream(bis);

	    @SuppressWarnings("unchecked")
	    T destination = (T) ois.readObject();

	    return destination;
	} catch (IOException e) {
	    throw new RuntimeException(e);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }
}
