package de.codecentric.propertypath.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import de.codecentric.propertypath.api.PropertyPath;
import de.codecentric.propertypath.demolib.Address;
import de.codecentric.propertypath.demolib.AddressProperties;
import de.codecentric.propertypath.demolib.UsAddress;
import de.codecentric.propertypath.demolib.UsAddressProperties;
import de.codecentric.propertypath.utils.PropertyPathUtils;

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
	public void pathesToGetterShouldWork() {
		final PropertyPath<Person, String> fullNamePath = Person.PROPERTIES.fullName;

		Person person = new Person();
		person.setName("Jim");
		person.setSurname("Miller");

		Assert.assertEquals("Jim Miller", fullNamePath.get(person));

		// Silently ignored because there is no fullName-setter
		fullNamePath.set(person, "Tom Smith");
		Assert.assertEquals("Jim", person.getName());

		// Non-readable-Property should deliver 'null' on read
		Person person2 = new Person();
		person2.setWriteOnlyProperty("WRITEONLY");
		Assert.assertNull(Person.PROPERTIES.writeOnlyProperty.get(person2));
	}

	@Test
	public void getOnStartOfPathMeansNull() {
		Person person = new Person();

		Assert.assertTrue(null == Person.PROPERTIES.get(person));
	}

	@Test
	public void equalsAndHashCodeShouldWork() {
		final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
		final PropertyPath<Person, String> cityPath = Person.PROPERTIES.address.city;
		final PropertyPath<Person, String> namePath2 = PersonProperties
				.newPersonProperties().name;
		final AddressProperties<Person, Address> addressProperties = PersonProperties
				.newPersonProperties().address;
		final PropertyPath<Person, String> cityPath2 = addressProperties.city;
		addressProperties.get(null);
		cityPath.get(null);

		Assert.assertTrue(namePath.equals(namePath2));
		Assert.assertTrue(namePath.hashCode() == namePath2.hashCode());
		Assert.assertTrue(cityPath.equals(cityPath2));
		Assert.assertTrue(cityPath.hashCode() == cityPath2.hashCode());

		Assert.assertFalse(namePath.equals(cityPath));
		Assert.assertFalse(namePath.hashCode() == cityPath.hashCode());
		Assert.assertFalse(namePath.equals("name"));

		final PropertyPath<OtherPerson, String> otherPersonCityPath = OtherPersonProperties
				.newOtherPersonProperties().address.city;
		Assert.assertFalse(otherPersonCityPath.equals(cityPath));
		Assert.assertTrue(otherPersonCityPath.getFullPath().equals(
				cityPath.getFullPath()));
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

		field2Error.put(namePath, new ErrorMessages(
				"Please enter a valid name!"));
		field2Error.put(addressPath, new ErrorMessages("Address not valid!"));

		Assert.assertEquals(
				"Please enter a valid name!",
				field2Error.get(PersonProperties.newPersonProperties().name).message);
		Assert.assertEquals(
				"Address not valid!",
				field2Error.get(PersonProperties.newPersonProperties().address).message);
	}

	@Test
	public void downcastShouldWork() {
		final UsAddress address = new UsAddress();
		address.setState("TX");
		final Person p = new Person();
		p.setAddress(address);

		// This would be better. But no use of .class literal seems to be
		// possible
		// UsAddressProperties<Person, UsAddress>.class is illegal
		// Class<UsAddressProperties<Person, UsAddress>> cl =
		// UsAddressProperties<Person, UsAddress>.class;

		@SuppressWarnings({ "rawtypes" })
		final Class<UsAddressProperties> cl = UsAddressProperties.class;
		@SuppressWarnings("unchecked")
		final Object readState = Person.PROPERTIES.address.downcast(cl).state
				.get(p);
		Assert.assertEquals("TX", readState);
	}

	@Test
	public void startsWithShouldWork() {
		final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
		final PropertyPath<Person, String> cityPath = Person.PROPERTIES.address.city;
		final PropertyPath<Person, Address> addressPath = Person.PROPERTIES.address;

		Assert.assertTrue(cityPath.startsWith(addressPath));
		Assert.assertFalse(namePath.startsWith(addressPath));

		final PropertyPath<YetAnotherPerson, String> yetAnotherPersonCityPath = YetAnotherPersonProperties
				.newYetAnotherPersonProperties().address.city;
		Assert.assertFalse(yetAnotherPersonCityPath.startsWith(addressPath));
		Assert.assertTrue(yetAnotherPersonCityPath.getFullPath().startsWith(
				addressPath.getFullPath()));
	}

	@Test
	public void lengthShouldWork() {
		Assert.assertEquals(0, Person.PROPERTIES.length());
		Assert.assertEquals(1, Person.PROPERTIES.address.length());
		Assert.assertEquals(2, Person.PROPERTIES.address.city.length());
	}

	// @Test
	// public void endsWithShouldWork() {
	// final PropertyPath<Person, String> personCityPath =
	// Person.PROPERTIES.address.city;
	// final PropertyPath<Address, String> addressCityPath =
	// Address.PROPERTIES.city;
	// final PropertyPath<Person, String> namePath = Person.PROPERTIES.name;
	//
	// Assert.assertTrue(personCityPath.endsWith(addressCityPath));
	// Assert.assertFalse(personCityPath.endsWith(namePath));
	//
	// final PropertyPath<UsAddress, String> usAddressCityPath =
	// UsAddressProperties.newUsAddressProperties().city;
	// Assert.assertTrue(personCityPath.endsWith(usAddressCityPath));
	//
	// // final PropertyPath<YetAnotherPerson, String> yetAnotherPersonCityPath
	// = YetAnotherPerson.PROPERTIES.address.city;
	// //
	// Assert.assertFalse(yetAnotherPersonCityPath.endsWith(addressCityPath));
	// }

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

	@Test
	public void extendsShouldWork() {

		// "number" is individually declared in Subclass1 and Subclass2 => it
		// isn't "equal"
		Assert.assertFalse(PersonSubclass1.PROPERTIES.number
				.equals(PersonSubclass2.PROPERTIES.number));
		Assert.assertFalse(PersonSubclass1.PROPERTIES.number
				.startsWith(PersonSubclass2.PROPERTIES.number));

		// "address.city" is inherited from Person into Subclass1 and Subclass2
		// => it is "equal"
		final PropertyPath<PersonSubclass1, String> city1 = PersonSubclass1.PROPERTIES.address.city;
		final PropertyPath<PersonSubclass2, String> city2 = PersonSubclass2.PROPERTIES.address.city;
		Assert.assertTrue(city1.equals(city2));
		Assert.assertTrue(city1.startsWith(city2));

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

			final ByteArrayInputStream bis = new ByteArrayInputStream(
					bos.toByteArray());
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

	@Test
	public void copyShouldWork() {
		final Person src = new Person();
		src.setName("Tom");
		src.setSurname("Miller");
		src.setAge(65);

		final Person dest = new Person();

		final List<PropertyPath<Person, ?>> properties = new ArrayList<PropertyPath<Person, ?>>();
		properties.add(Person.PROPERTIES.name);
		properties.add(Person.PROPERTIES.surname);

		PropertyPathUtils.copy(src, dest, properties);

		Assert.assertEquals("Tom", dest.getName());
		Assert.assertEquals("Miller", dest.getSurname());
		Assert.assertEquals(0, dest.getAge());
	}

	@Test
	public void nullsafeGetShouldWork() {
		final Person p = new Person();
		p.setAddress(new Address());
		p.getAddress().setCity("Ratingen");

		Assert.assertEquals("Ratingen",
				Person.PROPERTIES.address.city.getNullsafe(p));

		p.setAddress(null);
		Assert.assertEquals(null, Person.PROPERTIES.address.city.getNullsafe(p));
	}

	@Test
	public void writableAndReadableShouldWork() {
		Assert.assertTrue(Person.PROPERTIES.address.isWritable());
		Assert.assertTrue(Person.PROPERTIES.address.isReadable());

		Assert.assertFalse(Person.PROPERTIES.fullName.isWritable());
		Assert.assertTrue(Person.PROPERTIES.fullName.isReadable());

		Assert.assertTrue(Person.PROPERTIES.writeOnlyProperty.isWritable());
		Assert.assertFalse(Person.PROPERTIES.writeOnlyProperty.isReadable());

		Assert.assertTrue(Person.PROPERTIES.address.writeOnlyProperty
				.isWritable());
		Assert.assertFalse(Person.PROPERTIES.address.writeOnlyProperty
				.isReadable());
	}
}
