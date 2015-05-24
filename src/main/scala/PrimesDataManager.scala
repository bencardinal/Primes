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
   // Default to the first prime, 2
   var currentPrime = 2

//   try {
      // Will throw a ConnectException if unable to connect.
      val r = new RedisClient(redisHost, redisPort)
      
      if (deleteKey) {
         r.del(dbKey)
      }
      
      if (r.exists(dbKey)) {
         val count: Long = r.llen(dbKey).getOrElse(0)
         println("Got " + count + " existing primes from db")
         val dbList = r.lrange(dbKey, 0, count.toInt).get
         dbList.foreach(a => primeArray.append(a.get.toInt))
         currentPrime = primeArray.last
      }
      
//   } catch {
//      case e:java.net.ConnectException => println("Unable to connect to redis server, using local store")
//   }

   val primeIter = Iterator.from(currentPrime).filter(i => primeArray.takeWhile(j => j * j <= i).forall(k => i %k > 0))
   while (currentPrime <= maxValue)
   {
      currentPrime = primeIter.next
      primeArray.append(currentPrime)
      r.rpush(dbKey, currentPrime)
   }
   
   //r.rpush(dbKey, primeArray)
   
   def GetPrimes(lowerLimit: Int, upperLimit: Int): Array[Int] = {
             
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
      lazy val primes: Stream[Int] = 
         2 #:: Stream.from(3, 2)
         .filter(i => primes.takeWhile(j => j * j <= i)
               .forall(k => i % k > 0)) // filter those that are not divisible by earlier calculated prime
      
      
      //return primes.dropWhile(p => p <= lowerLimit).takeWhile(p => p <= upperLimit)
      return primeArray.dropWhile(p => p < lowerLimit).takeWhile(p => p <= upperLimit).toArray
   }   
}