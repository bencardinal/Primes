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
               
               print ("Enter a lower bound: ")
               val upper = io.StdIn.readInt()
               
               val result = dm.GetPrimes(lower, upper)
               val sum = result.sum
               val mean = sum / result.length
               println("Result:")
               println("Prime numbers: ["+result.mkString(", ") + "]")
               println("Sum: " + sum)
               println("Mean: " + mean)
            }
         } catch {
            case e:NumberFormatException => println("Argument is not a valid integer string")
         }
      }
   }
}