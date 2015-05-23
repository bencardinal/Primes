class PrimesDataManager(maxValue: Int) {   
   val _maxValue = maxValue
   // TODO Redis magic here in the constructor to build an internal list of integers.
   
   def GetPrimes(lowerLimit: Int, upperLimit: Int): Array[Int] = {
             
      if (upperLimit > _maxValue) {
         throw new IllegalArgumentException(
               "upperLimit must be less than maximum range specified in constructor")                
      }
      
      var buf = new scala.collection.mutable.ArrayBuffer[Int]()
       
      // TODO stub
      buf.append(lowerLimit)
      buf.append(upperLimit)
       
      return buf.toArray
   }   
}