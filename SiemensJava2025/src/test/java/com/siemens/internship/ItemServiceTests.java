package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ItemServiceTests {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private List<Item> processedItems;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Simulated in-memory DB
        processedItems = new CopyOnWriteArrayList<>();

        Item item1 = new Item();
        item1.setId(1L);
        item1.setStatus("UNPROCESSED");

        Item item2 = new Item();
        item2.setId(2L);
        item2.setStatus("PROCESSED");  // Already processed

        Item item3 = new Item();
        item3.setId(3L);
        item3.setStatus("UNPROCESSED");

        processedItems.addAll(Arrays.asList(item1, item2, item3));

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L, 3L));

        when(itemRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return processedItems.stream().filter(i -> i.getId().equals(id)).findFirst();
        });

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item updated = invocation.getArgument(0);
            // Simulate update
            processedItems.removeIf(i -> i.getId().equals(updated.getId()));
            processedItems.add(updated);
            return updated;
        });
    }

    @Test
    public void testProcessItemsAsync_shouldUpdateOnlyUnprocessedItems() {
        List<Item> processedItems = itemService.processItemsAsync();

        // Assert that only 2 of 3 items were processed
        assertEquals(2, processedItems.size());

        // Assert all returned items are marked as PROCESSED
        assertTrue(processedItems.stream().allMatch(i -> "PROCESSED".equals(i.getStatus())));

        // Check in-memory DB: all items should now be PROCESSED
        for (Item item : processedItems) {
            assertEquals("PROCESSED", item.getStatus());
        }

        // Verify interactions
        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(3)).findById(anyLong());
        verify(itemRepository, times(2)).save(any(Item.class));
    }

    @Test
    public void testProcessItemsAsync_AllItemsInitiallyUnprocessed() {

        processedItems.clear();
        Item item1 = new Item(); item1.setId(1L); item1.setStatus("UNPROCESSED");
        Item item2 = new Item(); item2.setId(2L); item2.setStatus("UNPROCESSED");
        Item item3 = new Item(); item3.setId(3L); item3.setStatus("UNPROCESSED");

        processedItems.addAll(Arrays.asList(item1, item2, item3));

        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L, 3L));

        when(itemRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return processedItems.stream().filter(i -> i.getId().equals(id)).findFirst();
        });

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item updated = invocation.getArgument(0);
            processedItems.removeIf(i -> i.getId().equals(updated.getId()));
            processedItems.add(updated);
            return updated;
        });


        List<Item> processedItems = itemService.processItemsAsync();

        // Assert: All 3 should be returned and processed
        assertEquals(3, processedItems.size());
        assertTrue(processedItems.stream().allMatch(i -> "PROCESSED".equals(i.getStatus())));

        // verify that all items in the DB are processed
        for (Item item : processedItems) {
            assertEquals("PROCESSED", item.getStatus());
        }

        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(3)).findById(anyLong());
        verify(itemRepository, times(3)).save(any(Item.class));
    }

}