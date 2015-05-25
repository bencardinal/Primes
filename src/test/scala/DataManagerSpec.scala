import org.scalatest._

class DataManagerSpec extends FlatSpec with Matchers {
   val upperBound = 100;

   "The PrimesDataManager constructor" should "throw RuntimeException if redis server cannot be found" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(100, "badhost", 6379, false)
      }
   }
   
   "The PrimesDataManager constructor" should "throw IllegalArgumentException if max value is less than 2" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(2, "badhost", 6379, true)
      }
   }

   "The PrimesDataManager constructor" should "only add each prime once when loading from cached values" in {
      val dm1 = new PrimesDataManager(10, "localhost", 6379, true)  // Clear database cache
      val dm2 = new PrimesDataManager(20, "localhost", 6379, false) // Reuse database cache (up to 10)
      val primes = dm2.GetPrimes(0, 20)
      primes should contain only (2, 3, 5, 7, 11, 13, 17, 19)
      primes should have length 8
   }

   val dm = new PrimesDataManager(upperBound, "localhost", 6379, false)
   
   "The PrimesDataManager" should "throw IllegalArgumentException if upperLimit is greater than constructor maximum" in {
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(0, upperBound + 1)
      }
   }

   "The PrimesDataManager" should "throw IllegalArgumentException if lowerLimit is less than zero" in {
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(-1, 11)
      }
   }

   "The PrimesDataManager" should "throw IllegalArgumentException if upperLimit is less than lowerLimit" in {
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(5, 4)
      }
   }
   
   "The PrimesDataManager" should "include the maximum value if that value is also a prime" in {
      val maxIsPrimeDm = new PrimesDataManager(23, "localhost", 6379, true)
      maxIsPrimeDm.GetPrimes(0, 23) should contain (23)
   }
   
   "The PrimesDataManager" should "return a list of 4 primes between 0 and 10: (2, 3, 5, 7)" in {
      val primes = dm.GetPrimes(0, 10)
      primes should contain only(2, 3, 5, 7)
      primes should have length 4
    
   }

   "The PrimesDataManager" should "return an empty list of primes between 8 and 10" in {
      val primes = dm.GetPrimes(8, 10)
      primes should have length 0    
   }

   "The PrimesDataManager" should "return single element array between 23 and 23: (23)" in {
      val primes = dm.GetPrimes(23, 23)
      primes should contain only (23)
      primes should have length 1
   }

   "The PrimesDataManager" should "return 79,498 elements between 0 and 1,000,000" in {
      val t0 = java.lang.System.currentTimeMillis()
      val bigDm = new PrimesDataManager(1000000, "localhost", 6379, true)
      val t1 = java.lang.System.currentTimeMillis()
      info("Building new set of primes < 1000000 took: "+(t1-t0)+" msec")
      val t2 = java.lang.System.currentTimeMillis()
      val bigDmCached = new PrimesDataManager(1000000, "localhost", 6379, false)
      val t3 = java.lang.System.currentTimeMillis()
      info("Loading cached primes < 1000000 took: "+(t3-t2)+" msec")
      val primes = bigDm.GetPrimes(0, 1000000)
      primes should have length 78498
   }
}