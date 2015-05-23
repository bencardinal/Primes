import org.scalatest._

class DataManagerSpec extends FlatSpec with Matchers {

   "The PrimesDataManager" should "throw IllegalArgumentException if max value is greater than constructor maximum" in {
      val dm = new PrimesDataManager(10)
      a [IllegalArgumentException] should be thrownBy {
         dm.GetPrimes(0, 11)
      }
   }
}