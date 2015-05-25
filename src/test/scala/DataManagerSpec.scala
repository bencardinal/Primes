import org.scalatest._
import com.redis._

class DataManagerSpec extends FlatSpec with Matchers {

   behavior of "The PrimesDataManager"
   
   it should "Pass basic tests" in { // Block to scope this instance of a data manager
      try
      {
         val upperBound = 100;
         val dm = new PrimesDataManager(upperBound, "localhost", 6379, false)   
      } catch {
         case e:Throwable =>  {
            alert("This test failed to run.  Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
      it should "throw IllegalArgumentException if upperLimit is greater than constructor maximum" in {
         a [IllegalArgumentException] should be thrownBy {
            dm.GetPrimes(0, upperBound + 1)
         }
      }
   
      it should "throw IllegalArgumentException if lowerLimit is less than zero" in {
         a [IllegalArgumentException] should be thrownBy {
            dm.GetPrimes(-1, 11)
         }
      }
   
      it should "throw IllegalArgumentException if upperLimit is less than lowerLimit" in {
         a [IllegalArgumentException] should be thrownBy {
            dm.GetPrimes(5, 4)
         }
      }
      
      it should "return a list of 4 primes between 0 and 10: (2, 3, 5, 7)" in {
         val primes = dm.GetPrimes(0, 10)
         primes should contain only(2, 3, 5, 7)
         primes should have length 4
       
      }
   
      it should "return an empty list of primes between 8 and 10" in {
         val primes = dm.GetPrimes(8, 10)
         primes should have length 0    
      }
   
      it should "return single element array between 23 and 23: (23)" in {
         val primes = dm.GetPrimes(23, 23)
         primes should contain only (23)
         primes should have length 1
      }            
   }

   
   it should "throw RuntimeException during construct if redis server cannot be found" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(100, "badhost", 6379, false)
      }
   }
   
   it should "throw IllegalArgumentException if constructor maxValue is less than 2" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(2, "badhost", 6379, true)
      }
   }

   it should "only add each prime once when loading from cached values" in {
      try
      {
         val dm1 = new PrimesDataManager(10, "localhost", 6379, true)  // Clear database cache
         val dm2 = new PrimesDataManager(20, "localhost", 6379, false) // Reuse database cache (up to 10)
         val primes = dm2.GetPrimes(0, 20)
         primes should contain only (2, 3, 5, 7, 11, 13, 17, 19)
         primes should have length 8
      } catch {
         case e:Throwable =>  {
            alert("This test failed to run.  Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
   }   
      
   it should "include the maximum value if that value is also a prime" in {
      try
      {
         val maxIsPrimeDm = new PrimesDataManager(23, "localhost", 6379, true)
         maxIsPrimeDm.GetPrimes(0, 23) should contain (23)
      } catch {
         case e:Throwable =>  {
            alert("This test failed to run.  Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
   }
   
   it should "handle a case where the data key has already been created as a non-list type" in {
      try
      {
         var r = new RedisClient("localhost", 6379)
         r.set("PrimesDataManager:primeArray", "Evil data")
         val dm = new PrimesDataManager(10)
         val primes = dm.GetPrimes(0, 10)
         primes should contain only(2, 3, 5, 7)
         primes should have length 4         
      } catch {
         case e:Throwable =>  {
            alert("This test failed to run.  Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
   }

   it should "return 79,498 elements between 0 and 1,000,000" in {
      try
      {
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
      } catch {
         case e:Throwable => {
            alert("This test failed to run.  Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
   }
}