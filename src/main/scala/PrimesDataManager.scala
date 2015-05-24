import com.redis._

class PrimesDataManager(maxValue: Int, redisHost: String, redisPort: Int, deleteKey: Boolean) {  
   val dbKey = "PrimesDataManager:primeArray";
   val _maxValue = maxValue
   // TODO Redis magic here in the constructor to build an internal list of integers.
   
   // Use iterator to iterate values from whatever maximum is currently in the
   // Redis database up to the provided maxVal.  Use a filtered query from
   // the databse to get a list of already calculated primes whose square is
   // less than the next potential prime.
   // Once a prime is found, push it to the database.

   // primeArray will hold the local copy of the primes.
   val primeArray = new scala.collection.mutable.ArrayBuffer[Int]()
   // Default to the second prime, 3, since the database will be initialized with
   // the first prime, 2.
   var currentPrime  = 1

   val client = new RedisClient(redisHost, redisPort)
   
   if (deleteKey) {
      client.del(dbKey)
   }
   
   if (client.exists(dbKey)) {
      if (client.getType(dbKey).getOrElse("").equals("list")) {
         // There is an assumption here that the list of data in this key
         // contains only a sequential set of valid prime numbers starting
         // with 2.  Things will go badly if this is not the case!
         val count: Long = client.llen(dbKey).getOrElse(0)
         primeArray.appendAll(client.lrange(dbKey, 0, count.toInt).getOrElse(List()).map(p => p.get.toInt))
         //dbList.foreach(a => primeArray.append(a.get.toLong))
         currentPrime = primeArray.last
      } else {
         // This is MY key and someone has messed with it so I'll show them!
         client.del(dbKey)
      }
   }

   //val dbScanner: Stream[Long] = Stream.from(0).map(i => client.lindex(dbKey, i).getOrElse("-1").toLong)
   
   //var largestPrime = currentPrime
   val primeIter = Iterator.from(currentPrime + 1) // Start one after the current prime (may be >2 if loaded from database)
   .filter(i => primeArray                         // Filter based on the current contents of the primeArray ...
         .takeWhile(j => j * j <= i)               // ... and take values whose square is less than the current value
         .forall(k => i % k > 0))                  // Yield the value if it is not evenly divisible by any previous prime
   
//   while (currentPrime <= maxValue)
//   {
//      println("currentPrime: " + currentPrime)
//      currentPrime = primeIter.next
//      primeArray.append(currentPrime)
//      client.rpush(dbKey, currentPrime)
//   }
   
   //if (primeArray.length > 0) primeIter.next() //KLUDGE!
   
   primeIter.takeWhile(p => p <= maxValue)     // Iterator will return as long as the prime is less or equal to max
   .foreach(p => {   
      //println("currentPrime "+p)
      primeArray.append(p)                   // Append to local array
      client.rpush(dbKey, p)                   // Push to databse
      //largestPrime = p                         // Update largestPrime
      //r.rpush(dbKey, p)                        // Push to database
      })

    //dbScanner.dropWhile(p => p > 0)
    
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

      // Use a stream to build the values as needed and then write them to
      // the Redis database.
//      lazy val primes: Stream[Int] = 
//         2 #:: Stream.from(3, 2)
//         .filter(i => primes.takeWhile(j => j * j <= i)
//               .forall(k => i % k > 0)) // filter those that are not divisible by earlier calculated prime
      
      
      //return primes.dropWhile(p => p <= lowerLimit).takeWhile(p => p <= upperLimit)
      //val count: Long = client.llen(dbKey).getOrElse(0)
      //val dbList = client.lrange(dbKey, 0, count.toInt).getOrElse(List())
      //dbList.dropWhile(p => p.get.toInt < lowerLimit).takeWhile(p => p.get.toInt <= upperLimit).map(p => p.get.toInt).toArray
      primeArray.dropWhile(p => p < lowerLimit).takeWhile(p => p <= upperLimit).toArray
      //val dbScanner: Iterator[Long] = Iterator.from(0).map(i => client.lindex(dbKey, i).get.toInt)
      //dbScanner.dropWhile(p => p < lowerLimit).takeWhile(p => p <= upperLimit && p > 0).toArray

   }   
}