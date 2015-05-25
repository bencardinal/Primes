import com.redis._

/** Prime number class that will generate a list of primes which will be stored in a Redis database
 * 
 * Any values already in the Redis database will be loaded first and then
 * additional values will be generated up to maxValue.
 * 
 * @constructor Create a new PrimesDataManager loaded with primes up to maxValue inclusive
 * @param redisHost the hostname where the Redis server is running ("localhost" if not provided)
 * @param redisPort the port that the Redis server is listening on (3769 if not provided)
 * 
 */
class PrimesDataManager(maxValue: Int, redisHost: String, redisPort: Int, deleteKey: Boolean) {
   def this(maxValue: Int) = this(maxValue, "localhost", 6379, false) // Auxiliary constructor with max only
   
   if (maxValue < 2) throw new IllegalArgumentException("maxValue must be greater than or equal to 2")   

   val dbKey = "PrimesDataManager:primeArray";                      // Key used to store primes
   val _maxValue = maxValue                                         // Keep max value for error checking
   val primeArray = new scala.collection.mutable.ArrayBuffer[Int]() // Local copy of primes

   // Will throw an ConnectException redis server is not available
   val dbClient = new RedisClient(redisHost, redisPort)
   
   if (deleteKey) {
      dbClient.del(dbKey)
   }
   
   if (  !deleteKey
      && dbClient.exists(dbKey)
      && dbClient.getType(dbKey).getOrElse("").equals("list")
      ) {
         // There is an assumption here that the list of data in this key
         // contains only a sequential set of valid prime numbers starting
         // with 2.  Things will go badly if this is not the case!
         val count: Long = dbClient.llen(dbKey).getOrElse(0)
         
         primeArray.appendAll(
               dbClient.lrange(dbKey, 0, count.toInt) // Get entire list
               .getOrElse(List())                     // Return an empty list if that fails
               .map(p => p.get.toInt))                // And map Option to an integer.
   } else {
      // Need to reinitialize the database and local array.
      // Initial state is loaded with 2 and 3
      dbClient.del(dbKey)
      primeArray.append(2, 3)
      dbClient.rpush(dbKey, 2, 3)
   }

   val primeIter = 
      Iterator.from(primeArray.last + 2, 2) // Start two after the current prime (may be >5 if loaded from database)
      .filter(i => primeArray               // Filter based on the current contents of the primeArray ...
            .takeWhile(j => j * j <= i)     // ... and take values whose square is less than the current value
            .forall(k => i % k > 0))        // Yield the value if it is not evenly divisible by any previous prime
   
   primeIter.takeWhile(p => p <= maxValue)  // Take values as long as the prime is less or equal to max
   .foreach(p => {                          // For each prime from the iterator ...
      primeArray.append(p)                  // ... Append to local array
      dbClient.rpush(dbKey, p)              // ... Push to databse
   })
   
   dbClient.disconnect

   /** Get an array of prime numbers in a given range
    * 
    * @param lowerLimit minimum allowable prime number, inclusive
    * @param upperLimit maximum allowable prime number, inclusive
    * @return an array of prime numbers between lowerLimit and upperLimit, inclusive
    *   
    */
   def GetPrimes(lowerLimit: Long, upperLimit: Long): Array[Int] = {
             
      if (upperLimit > _maxValue) {
         throw new IllegalArgumentException(
               "upperLimit must be less than maximum value specified in constructor")                
      }
      
      if (lowerLimit < 0) {
         throw new IllegalArgumentException(
               "lowerLimit must not be negative")                
      }
      
      if (lowerLimit > upperLimit) {
         throw new IllegalArgumentException(
               "lowerLimit must be less than or equal to upperLimit")                
      }

      primeArray.dropWhile(p => p < lowerLimit).takeWhile(p => p <= upperLimit).toArray
   }   
}