import org.scalatest._

class DataManagerSpec extends FlatSpec with Matchers {

   "The PrimesDataManager" should "throw IllegalArgumentException if upperLimit is greater than constructor maximum" in {
      val dm = new PrimesDataManager(10)
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(0, 11)
      }
   }

   "The PrimesDataManager" should "throw IllegalArgumentException if lowerLimit is less than zero" in {
      val dm = new PrimesDataManager(10)
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(-1, 11)
      }
   }

   "The PrimesDataManager" should "throw IllegalArgumentException if upperLimit is not greater than lowerLimit" in {
      val dm = new PrimesDataManager(10)
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(5, 5)
      }
   }
}