// @author:  John Shiner
// 1/22/2014 

package jfs
import groovy.swing.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.awt.BorderLayout
import javax.swing.BorderFactory

/************************ Menu Classes *********/
class MenuApp {
    def mCount = 0
    String currentMenu = "" // strId of current Menu
    Menu activeMenu
    def menus = [:]
    def actions = [:]  
    def addAction(String actionId, Closure action) {
        if (actions."$actionId") { println "Action already exists" }
        actions."$actionId" = action
    }
    def setActions(Map appActions) { 
        setActions()
        actions << appActions 
    }

    def setActions() {
        // default menu actions
        actions.returnToParent = { parentMenu -> currentMenu = parentMenu }
        actions.callMenu  = { targetMenu -> currentMenu = targetMenu }
        actions.exit  = { println "Exiting"; System.exit(0) }
        
        // use setActions(Map) or addAction() to add application specific actions
    }

    def addMenu(Menu m) {
        // the idea with this method is that the Menu is fully formed and that the
        // title can be used to store the menu in the menus Map
        if (mCount == 0) { currentMenu = title } // first menu created is assumed to be the root
        if (m.title.size() > 0) {
            mCount++
            menus[m.title] = m
        }
        else { return -1 }
    }
    
    Boolean validMenuTitle(String title) {
        (title in menus.keySet())?true:false
    }
    
    Menu getMenu(String title) {
        def m = menus[title]?: null
    }

    Menu changeMenu(String title) {
        if (title in menus.keySet()) {
            currentMenu = title
            activeMenu = menus[currentMenu]
        }
    }
    
    Menu createMenu(String title) {
        if (mCount == 0) { currentMenu = title } // first menu created is assumed to be the root
        mCount++
        Menu m = new Menu(title: title)
        menus[m.title] = m
        m
    }  
      
    void runLoop() {       
        def lookup = ""
        def quitPattern = ~/[Qq].*/
        def pattern = ~/\d+/   /* ~/[0123456789] */
        while ("$lookup" != "Exit") {
            activeMenu = menus[currentMenu]
            activeMenu.display()
            /*
            def readln = javax.swing.JOptionPane.&showInputDialog
            def val = readln 'Select an option:  '
            // println "Selected: $val."  
            */
             
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
            print "Input:"
            def val = ""
            val = br.readLine()
            def cmd
            if ( quitPattern.matcher(val).matches()) { //matches() must match whole String
                    lookup = "Exit"
                    cmd = actions.exit
                    cmd()
            } 
            def actionList = activeMenu.menuItems.collect { k, v -> "${k}" }
            if ((pattern.matcher(val).matches())  && (val.toInteger() <= activeMenu.menuItems.size())) {
                lookup = actionList[val.toInteger()-1]
                // println "${lookup}"
                cmd = activeMenu.menuItems."${lookup}".action
                cmd()
            }
        }
        println "Exiting"
    }
    
    def runShellCommand(String cmdString) {
        def sout = new StringBuffer(), serr = new StringBuffer()
        def proc = "${cmdString}".execute()
        proc.consumeProcessOutput(sout, serr)
        proc.waitForOrKill(1000)
        println "out> $sout err> $serr" 
    }

}

class Menu {
    def title = ""
    //final def parent = null //strId
    def menuItems = [:]
    //def strId = ""
    def createMenuItem(String key, Closure value) {
        def mi = MenuItem.createMenuItem(key, value) 
        menuItems."${mi.prompt}" = mi
    }
    def cmi(String key, Closure value) { // convenience function -- less boilerplate
        createMenuItem(key, value)
    }
    def display() {
        def t = "\n$title\n\n"
        def lastItem = menuItems.size()
        menuItems.eachWithIndex { mi, i ->
            def itemNum = i+1
            if (itemNum == lastItem) {
                t = "$t[Q|q] ${mi.key}\n"
            }
            else { 
                t = "$t[${itemNum}] ${mi.key}\n"
            }
        }
        println "$t\n"
    }
}

class MenuItem {
    def prompt = ""
    def action = { println "Action not set" }
    def params = [:]
    static MenuItem createMenuItem(String key, Closure value) {
        def m = new MenuItem()
        m.prompt = key
        m.action = value
        m
    }
}
