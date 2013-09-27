package fly.reactivemongo.evolutions

import org.specs2.mutable.Specification

object BSONDocumentHelpersTests extends Specification {
	
  "BSONDocumentHelpers" should {
    
    "have implicits for" >> {
      
      "removing keys" in {
    	  //doc remove "bla"
        todo
      }
      
      "rename keys" in {
        //doc rename ("bla" -> "other")
        todo
      }
      
      "move keys" in {
        //doc move ("bla" -> "other.bla")
        todo
      }
      
    } 
  }
  
}