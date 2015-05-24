class PrimesDataManager(maxValue: Int) {   
   val _maxValue = maxValue
   // TODO Redis magic here in the constructor to build an internal list of integers.
   
   // Use iterator to iterate values from whatever maximum is currently in the
   // Redis database up to the provided maxVal.  Use a filtered query from
   // the databse to get a list of already calculated primes whose square is
   // less than the next potential prime.
   // Once a prime is found, push it to the database.
   val primeArray = new scala.collection.mutable.ArrayBuffer[Int]()
   val primeIter = Iterator.from(2).filter(i => primeArray.takeWhile(j => j * j <= i).forall(k => i %k > 0))
   var currentPrime = 2
   while (currentPrime <= maxValue)
   {
      currentPrime = primeIter.next
      primeArray.append(currentPrime)
   }
   
   def GetPrimes(lowerLimit: Int, upperLimit: Int): Array[Int] = {
             
      if (upperLimit > _maxValue) {
         throw new IllegalArgumentException(
               "upperLimit must be less than maximum value specified in constructor")                
      }
      
      if (lowerLimit < 0) {
         throw new IllegalArgumentException(
               "lowerLimit must not be negative")                
      }
      
      if (lowerLimit >= upperLimit) {
         throw new IllegalArgumentException(
               "lowerLimit must be less than upperLimit")                
      }

      var buf = new scala.collection.mutable.ArrayBuffer[Int]()
       
      // TODO stub
      buf.append(lowerLimit)
      buf.append(upperLimit)

      // Use a stream to build the values as needed and then write them to
      // the Redis database.
      lazy val primes: Stream[Int] = 
         2 #:: Stream.from(3, 2)
         .filter(i => primes.takeWhile(j => j * j <= i)
               .forall(k => i % k > 0)) // filter those that are not divisible by earlier calculated prime
      
      
      //return primes.dropWhile(p => p <= lowerLimit).takeWhile(p => p <= upperLimit)
      return primeArray.dropWhile(p => p <= lowerLimit).takeWhile(p => p <= upperLimit).toArray
   }   
}