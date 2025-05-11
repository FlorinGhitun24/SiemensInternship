package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    //processedItems and processedCount represent shared state accessed by multiple threads in order to avoid race condition and inconsistent results I used Collections.synchronizedList and AtomicInteger to safely update shared states
    private List<Item> processedItems = Collections.synchronizedList(new ArrayList<>()) ;
    private AtomicInteger processedCount = new AtomicInteger();


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /**
     * Explanation of what was wrong:
     * One of the issues identified in the code was the wrong use of the @Async annotation as the method would return a list of items
     * before the CompletableFuture tasks are completed, resulting in an empty or incomplete list. The pocessedItems and processCount were modified by multiple threads
     * without synchronization leading to inconsistent results. Another problem was the CompletableFuture.runAsync multiple calls which would not wait for them to finish.
     * Thread.sleep(100) is unnecessary as it waste resources. And error handling was incomplete as other exceptions were ignored with InterruptedException
     *
     *
     */


    // removed th Async annotation because the method will block until all the items are processed
    // from the caller perspective the method is no longer asynchronous
    public List<Item> processItemsAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Long> itemIds = itemRepository.findAllIds();

        for (Long id : itemIds) {

            // used to reflect true async behavior
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return;
                    }

                    processedCount.incrementAndGet();
                    System.out.println("Processed count: " + processedCount.get());
                    if(!item.getStatus().equals("PROCESSED")){
                        item.setStatus("PROCESSED");
                        itemRepository.save(item);
                        processedItems.add(item);

                    }

                    //Changed the InterruptedException to Exception to catch all exceptions missed before for example from itemRepository
                } catch (Exception e) {
                    System.out.println("Error processing item ID:  " + id + ": " +  e.getMessage());

                }
            }, executor);
            futures.add(future);
        }

        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        //join is used to wait for all CompletableFutures to complete
        allDone.join();
        return processedItems;
    }

}

