# Siemens Internship
Hello

Here is my solution for the asynchronous problem. The ItemService class was modified with the correct solution and command line where added where explenations were necessary. 
Also the ItemController class was edited asa to resolve some coding problems. Moreover three test classes were added to test functionality for the controller and service classes and 
the regex email validation was completed in the Item class. 
The brief explanation of the problems encountered can be found in the ItemService class or here bellow.

Explanation of what was wrong:
     * One of the issues identified in the code was the wrong use of the @Async annotation as the method would return a list of items
     * before the CompletableFuture tasks are completed, resulting in an empty or incomplete list. The pocessedItems and processCount were modified by multiple threads
     * without synchronization leading to inconsistent results. Another problem was the CompletableFuture.runAsync multiple calls which would not wait for them to finish.
     * Thread.sleep(100) is unnecessary as it waste resources. And error handling was incomplete as other exceptions were ignored with InterruptedException
