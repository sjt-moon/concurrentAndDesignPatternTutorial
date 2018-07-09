# concurrentTutorial
Some examples for classic concurrent solutions

##  classic concurrent problems
### H2O generator
### condition variable
customer buys products (poping from a shared queue) while producer keeps creating items (pushing into the queue) 
it is exactly the same when there are multiple customers and producers working concurrently, still one mutex lock and 2 condition variables (isQueueFull and isQueueEmpty)
### merge sort
becomes faster given more than 3 million elements to be sorted 
### quick sort
same as above 

## design patterns
### visitor pattern
