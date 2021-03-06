/** Manages the command line interface to a PrimesDataManager.
 *  
 *  It is expected that a Redis server is running on localhost:6379
 *  
 *  A single integer command line argument must be provided.  That integer
 *  will be used as the maximum prime value generated by the PrimesDataManager.
 *  Upper and lower bounds for the query must be integers between 0 and the
 *  maximum (inclusive)
 */
object Primes {
   def main(args: Array[String]) {
      if (args.length != 1) {
         println("Single integer argument is expected!")
      } else {
         try {
            val maxValue = args(0).toInt
            val dm = new PrimesDataManager(maxValue)
            
            while (true) {
               print ("Enter a lower bound: ")
               val lower = io.StdIn.readInt()
               
               print ("Enter an upper bound: ")
               val upper = io.StdIn.readInt()
               
               val result = dm.GetPrimes(lower, upper)
               val sum = result.sum
               val mean = if (result.length > 0) sum / result.length else 0
               println("Result:")
               println("Prime numbers: ["+result.mkString(", ") + "]")
               println("Sum: " + sum)
               println("Mean: " + mean)
            }
         } catch {
            case e:NumberFormatException => println("Input is not a valid integer string")
         }
      }
   }
}
