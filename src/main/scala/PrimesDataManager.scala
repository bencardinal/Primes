import com.redis._

class PrimesDataManager(maxValue: Int, redisHost: String, redisPort: Int, deleteKey: Boolean) {
   def this(maxValue: Int) = this(maxValue, "localhost", 6379, false) // Auxiliary constructor with max only
   
   val dbKey = "PrimesDataManager:primeArray";
   
   // Store off max value for error checking later on
   val _maxValue = maxValue
   
   // primeArray will hold the local copy of the primes.
   val primeArray = new scala.collection.mutable.ArrayBuffer[Int]()

   // Initialize currentPrime to 0, which is not technically a prime number but
   // the iterator (below) will increment current prime by 2 to avoid emitting
   // a cached value twice
   var currentPrime  = 0

   // Will throw an ConnectException redis server is not available
   val dbClient = new RedisClient(redisHost, redisPort)
   
   if (deleteKey) {
      dbClient.del(dbKey)
   }
   
   if (dbClient.exists(dbKey)) {
      if (dbClient.getType(dbKey).getOrElse("").equals("list")) {
         
         // There is an assumption here that the list of data in this key
         // contains only a sequential set of valid prime numbers starting
         // with 2.  Things will go badly if this is not the case!
         val count: Long = dbClient.llen(dbKey).getOrElse(0)
         
         primeArray.appendAll(
               dbClient.lrange(dbKey, 0, count.toInt) // Get entire list
               .getOrElse(List())                     // Return an empty list if that fails
               .map(p => p.get.toInt))                // And map Option to an integer.
         currentPrime = primeArray.last               // Set the currentPrime to the biggest one
      } else {
         // This is MY key and someone has messed with it so I'll show them!
         dbClient.del(dbKey)
      }
   }

   val primeIter = 
      Iterator.from(currentPrime + 2)      // Start two after the current prime (may be >2 if loaded from database)
      .filter(i => primeArray              // Filter based on the current contents of the primeArray ...
            .takeWhile(j => j * j <= i)    // ... and take values whose square is less than the current value
            .forall(k => i % k > 0))       // Yield the value if it is not evenly divisible by any previous prime
   
   primeIter.takeWhile(p => p <= maxValue) // Take values as long as the prime is less or equal to max
   .foreach(p => {                         // For each prime from the iterator ...
      primeArray.append(p)                 // ... Append to local array
      dbClient.rpush(dbKey, p)             // ... Push to databse
   })

   /*
    * Get an array of prime numbers between lowerLimit and upperLimit inclusive
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