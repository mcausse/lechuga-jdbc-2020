package org.lechuga.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.lechuga.anno.Id;

public class PropertyPathAccessorTest {

	@Test
	public void testName() throws Exception {

		Dog chucho = new Dog();

		PropertyPathAccessor id = new PropertyPathAccessor(Dog.class, "id.id");
		PropertyPathAccessor dni = new PropertyPathAccessor(Dog.class, "id.dni");
		PropertyPathAccessor name = new PropertyPathAccessor(Dog.class, "name");

		assertNull(id.get(chucho));
		assertNull(dni.get(chucho));
		assertNull(name.get(chucho));

		assertEquals("[null, name=null]", chucho.toString());
		dni.set(chucho, "8P");
		assertEquals("[[id=null, dni=8P], name=null]", chucho.toString());
		id.set(chucho, 100L);
		name.set(chucho, "chucho");
		assertEquals("[[id=100, dni=8P], name=chucho]", chucho.toString());

		assertEquals(100L, id.get(chucho));
		assertEquals("8P", dni.get(chucho));
		assertEquals("chucho", name.get(chucho));

		assertEquals("{interface org.lechuga.anno.Id=@org.lechuga.anno.Id()}", dni.getAnnotations().toString());
	}

	@Test
	public void testName2() throws Exception {

		assertEquals("[id.id, id.dni, name]", new PropertyPathAccessorScanner().scan(Dog.class).keySet().toString());
	}

	@Embbedable
	public static class DogId {

		Long id;
		public String dni;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return String.format("[id=%s, dni=%s]", id, dni);
		}
	}

	public static class Dog {

		@Id
		DogId id;

		String name;

		public DogId getId() {
			return id;
		}

		public void setId(DogId id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return String.format("[%s, name=%s]", id, name);
		}
	}

}
