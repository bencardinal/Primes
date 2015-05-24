import org.scalatest._

class DataManagerSpec extends FlatSpec with Matchers {
   val upperBound = 1000000;

   "The PrimesDataManager constructor" should "throw RuntimeException if redis server cannot be found" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(upperBound, "badhost", 6379, false)
      }
   }
   val t0 = java.lang.System.currentTimeMillis()
   val dm = new PrimesDataManager(upperBound, "localhost", 6379, false)
   val t1 = java.lang.System.currentTimeMillis()
   info("Constructor took: "+(t1-t0)+" msec")
   
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
      val primes = dm.GetPrimes(0, 1000000)
      primes should have length 78498
   }
}