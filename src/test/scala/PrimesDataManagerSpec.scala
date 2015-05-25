import org.scalatest._
import com.redis._

/** Unit test for PrimesDataManager
 *  
 *  Performs several data integrity checks as well as Redis server tests.  It is expected
 *  that a Redis server is running on localhost:6379 otherwise the test suite will abort.
 *  
 */
class PrimesDataManagerSpec extends FlatSpec with Matchers with BeforeAndAfter {

   // Use "before" to verify that server is available.  Not the most elegant
   // way to do this but there does not appear to be a "ping" method in the 
   // scala-redis library.
   before { 
      try
      {
         val r = new RedisClient("localhost", 6379)
         r.disconnect
      } catch {
         case e:Throwable =>  {
            alert("Unable to verify that redis server is available, Are you sure the Redis server is running on localhost:6379?")
            cancel(e)
         }
      }
   } 

   val upperBound = 100         // Used as max in basic tests
   var uncached1mTime: Long = 0 // Used to store the uncached load time for values < 1,000,000
   
   behavior of "The PrimesDataManager"

   it should "throw IllegalArgumentException if upperLimit is greater than constructor maximum" in {
      a [IllegalArgumentException] should be thrownBy {
         val basicDm = new PrimesDataManager(upperBound)
         basicDm.GetPrimes(0, upperBound + 1)
      }
   }
   
   it should "throw IllegalArgumentException if lowerLimit is less than zero" in {
      a [IllegalArgumentException] should be thrownBy {
         val basicDm = new PrimesDataManager(upperBound)
         basicDm.GetPrimes(-1, 11)
      }
   }

   it should "throw IllegalArgumentException if upperLimit is less than lowerLimit" in {
      a [IllegalArgumentException] should be thrownBy {
         val basicDm = new PrimesDataManager(upperBound)
         basicDm.GetPrimes(5, 4)
      }
   }
   
   it should "return a list of 4 primes between 0 and 10: (2, 3, 5, 7)" in {
      val basicDm = new PrimesDataManager(upperBound)
      val primes = basicDm.GetPrimes(0, 10)
      primes should contain only(2, 3, 5, 7)
      primes should have length 4
   }

   it should "return an empty list of primes between 8 and 10" in {
      val basicDm = new PrimesDataManager(upperBound)
      val primes = basicDm.GetPrimes(8, 10)
      primes should have length 0    
   }

   it should "return single element array between 23 and 23: (23)" in {
      val basicDm = new PrimesDataManager(upperBound)
      val primes = basicDm.GetPrimes(23, 23)
      primes should contain only (23)
      primes should have length 1
   }            
   
   it should "throw RuntimeException during construct if redis server cannot be found" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(100, "badhost", 6379, false)
      }
   }
   
   it should "throw IllegalArgumentException if constructor maxValue is less than 2" in {
      a [RuntimeException] should be thrownBy {
         var badDm = new PrimesDataManager(1)
      }
   }

   it should "only add each prime once when loading from cached values" in {
      val dm10 = new PrimesDataManager(10)  // Clear database cache
      val dm20 = new PrimesDataManager(20) // Reuse database cache (up to 10)
      val primes = dm20.GetPrimes(0, 20)
      primes should contain only (2, 3, 5, 7, 11, 13, 17, 19)
      primes should have length 8
   }   
      
   it should "include the maximum value if that value is also a prime" in {
      val dm23 = new PrimesDataManager(23)
      dm23.GetPrimes(0, 23) should contain (23)
   }
   
   it should "handle a case where the data key has already been created as a non-list type" in {
      try
      {
         val r = new RedisClient("localhost", 6379)
         r.set(PrimesDataManagerParameters.dbKey, "Evil data")
      } catch {
         case e:Throwable =>  {
            alert("Failed to create RedisClient so this test cannot be run!")
            cancel(e)
         }         
      }
      val dm = new PrimesDataManager(10)
      val primes = dm.GetPrimes(0, 10)
      primes should contain only(2, 3, 5, 7)
      primes should have length 4         
   }
   
   it should "handle a case where 2 is the only value in the database list" in {
      try
      {
         val r = new RedisClient("localhost", 6379)
         r.del(PrimesDataManagerParameters.dbKey)
         r.rpush(PrimesDataManagerParameters.dbKey, 2)
      } catch {
         case e:Throwable =>  {
            alert("Failed to create RedisClient so this test cannot be run!")
            cancel(e)
         }         
      }
      val dm = new PrimesDataManager(10)
      val primes = dm.GetPrimes(0, 10)
      primes should contain only(2, 3, 5, 7)
      primes should have length 4               
   }

   it should "return 79,498 elements between 0 and 1,000,000" in {
      val t0 = java.lang.System.currentTimeMillis()
      val dm1m = new PrimesDataManager(1000000, "localhost", 6379, true)
      val t1 = java.lang.System.currentTimeMillis()
      info("Building new set of primes < 1000000 took: "+(t1-t0)+" msec")
      uncached1mTime = t1 - t0
      val primes = dm1m.GetPrimes(0, 1000000)
      primes should have length 78498
   }
   
   it should "load much faster when pulling from database cache while still holding accurate results" in {
      val t2 = java.lang.System.currentTimeMillis()
      val dm1m = new PrimesDataManager(1000000, "localhost", 6379, false)
      val t3 = java.lang.System.currentTimeMillis()
      info("Loading cached primes < 1000000 took: "+(t3-t2)+" msec")     
      
      // Primary time test
      (t3 - t2) should be < uncached1mTime

      // Data integrity checks
      var primes = dm1m.GetPrimes(0, 20)
      primes should contain only (2, 3, 5, 7, 11, 13, 17, 19)
      primes should have length 8
      primes = dm1m.GetPrimes(0, 1000000)
      primes should have length 78498
   }
}