package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InternshipApplicationTests {

	@Autowired
	private ItemService itemService;

	@Autowired
	private ItemRepository itemRepository;

	@Test
	void testCreateValidItem() {
		Item item = new Item(null, "Test", "Description", "NEW", "test@example.com");
		Item saved = itemService.save(item);
		assertNotNull(saved.getId());
	}

	@Test
	void testProcessItemsAsync() throws Exception {
		Item item = new Item(null, "Bulk", "To be processed", "PENDING", "bulk@example.com");
		itemRepository.save(item);
		List<Item> result = itemService.processItemsAsync().get(10, TimeUnit.SECONDS);
		assertTrue(result.stream().anyMatch(i -> "PROCESSED".equals(i.getStatus())));
	}

	@Test
	void testFindAll() {
		Item item1 = new Item(null, "Item 1", "Description", "NEW", "item1@example.com");
		Item item2 = new Item(null, "Item 2", "Description", "NEW", "item2@example.com");
		itemRepository.save(item1);
		itemRepository.save(item2);
		List<Item> items = itemService.findAll();
		assertNotNull(items);
		assertTrue(items.size() >= 2);
	}

	@Test
	void testFindById() {
		Item item = new Item(null, "Item 3", "Description", "NEW", "item3@example.com");
		Item savedItem = itemRepository.save(item);
		var foundItem = itemService.findById(savedItem.getId());
		assertTrue(foundItem.isPresent());
		assertEquals(savedItem.getId(), foundItem.get().getId());
	}

	@Test
	void testSave() {
		Item item = new Item(null, "Item 4", "Description", "NEW", "item4@example.com");
		Item savedItem = itemService.save(item);
		assertNotNull(savedItem.getId());
		assertEquals("Item 4", savedItem.getName());
	}

	@Test
	void testDeleteById() {
		Item item = new Item(null, "Item 5", "Description", "NEW", "item5@example.com");
		Item savedItem = itemRepository.save(item);
		itemService.deleteById(savedItem.getId());
		var deletedItem = itemRepository.findById(savedItem.getId());
		assertFalse(deletedItem.isPresent());
	}
}
