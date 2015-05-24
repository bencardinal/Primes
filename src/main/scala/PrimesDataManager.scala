class PrimesDataManager(maxValue: Int) {   
   val _maxValue = maxValue
   // TODO Redis magic here in the constructor to build an internal list of integers.
   
   def GetPrimes(lowerLimit: Int, upperLimit: Int): Array[Int] = {
             
      if (upperLimit > _maxValue) {
         throw new IllegalArgumentException(
               "upperLimit must be less than maximum value specified in constructor")                
      }
      
      if (lowerLimit < 0) {
         throw new IllegalArgumentException(
               "lowerLimit must not be negative")                
      }
      
      if (lowerLimit >= upperLimit) {
         throw new IllegalArgumentException(
               "lowerLimit must be less than upperLimit")                
      }

      var buf = new scala.collection.mutable.ArrayBuffer[Int]()
       
      // TODO stub
      buf.append(lowerLimit)
      buf.append(upperLimit)
       
      return buf.toArray
   }   
}