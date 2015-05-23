object Primes {
   def main(args: Array[String]) {
      if (args.length != 1) {
         println("Single integer argument is expected!")
      } else {
         try {
            val maxValue = args(0).toInt
            val dm = new PrimesDataManager(maxValue)
            val result = dm.GetPrimes(0,10)

            println("Prime numbers: ["+result.mkString(", ") + "]")
            println(result.foreach(println))
         } catch {
            case e:NumberFormatException => println("Argument is not a valid integer string")
         }
      }
   }
}