// The purpose here is to allow viewing console output while running the app as well as 
// providing a way to capture session output in a file

package jfs
import groovy.beans.Bindable

class ConsoleModel {
   @Bindable int count = 0
   @Bindable String outputStr = ""
   def sessionStr = ""
   def commandStr = ""
   def begin = {
      commandStr = ""
   }
   def end = { 
      outputStr = commandStr
      sessionStr += commandStr
   }
   void println(str) {
        commandStr += "${str}\n"
        Console.println str
   }
   void saveSession(sourcePath) {
     def strFileName = "sessionOutput${new Date().time}.txt"
     // def writer = new FileWriter(sourcePath + '\\' + "$strFileName")

     def writer = new FileWriter(sourcePath + '/' + "$strFileName")
     writer.write(sessionStr); 
     writer.close()
     resetSession()
     this.println("Saved $strFileName")
   }
   void resetSession() {
        outputStr = ""
        sessionStr = ""
        commandStr = ""
        this.begin()
        this.println("Session output reset")
        this.end()
   }
}
