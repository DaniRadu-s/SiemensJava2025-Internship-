package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    //using a thread-safe collection to avoid concurrency issues
    //it is good to work with it when we have a multi-threading
    //application where multiple threads might access and modify
    //the collection at the same time
    //using ArrayList can lead to data corruption
    private ConcurrentLinkedQueue<Item> processedItems = new ConcurrentLinkedQueue<>();
    //using AtomicInteger for thread safety
    private AtomicInteger processedCount = new AtomicInteger(0);


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

    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        //create a list of futures for asynchronous processing
        List<CompletableFuture<Void>> futures = itemIds.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    try {
                        Optional<Item> optionalItem = itemRepository.findById(id);
                        if (optionalItem.isEmpty()) {
                            return;
                        }

                        Item item = optionalItem.get();
                        item.setStatus("PROCESSED");
                        itemRepository.save(item);

                        processedItems.add(item);

                        processedCount.incrementAndGet();
                    } catch (Exception e) {
                        System.err.println("Error processing item " + id + ": " + e.getMessage());
                    }
                }, executor))
                .collect(Collectors.toList());

        //waiting for all the tasks to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> new ArrayList<>(processedItems));
    }


}

