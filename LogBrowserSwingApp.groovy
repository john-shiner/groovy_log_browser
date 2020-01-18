// @author:  John Shiner
// 1/23/2014 

package jfs
import jfs.LogBrowser.*
import jfs.AppConsole.*
import groovy.swing.*
import javax.swing.*
import java.awt.*
import java.awt.event.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.awt.BorderLayout as BL
import javax.swing.BorderFactory

// LogBrowserMenu application class
class LogBrowserMenu {
    def model = new ConsoleModel()
    def logBrowser = new LogBrowser(mod: model)
    def mainFrame
    def fileFrame
    Map actions = [:]

    Object invokeMethod(String actionName, Closure action) {
            actions."$actionName" = action
    }

    static void main(args) {
        def lbm = new LogBrowserMenu()
        def lb = lbm.logBrowser
        def mod = lbm.model
        lb.sourcePath = '.'
        lb.sourceFiles = [ 
			// append this list as necessary
			// Dynamic menu creators will adjust based on this list
       /*
            "logFile1.log", 
            "logFile2.log", 
            "logFile3.log", 
            "logFile4.log", 
            "logFile5.log", 
            "logFile6.log", 
            "logFile7.log",
        */
            "logFile8.log", 
            "logFile9.log"
        ]

		setLbActions(lbm)
        lb.runBatch() // initialize the logBrowser data before displaying menu;  
        lbm.mainSwingMenu(lbm)
    }

	static void setLbActions(LogBrowserMenu logBrowserMenu) {
        def lbm = logBrowserMenu
        def lb = lbm.logBrowser
        def mod = lbm.model
        // this function works because of the the invokeMethod() method defined in lbm to create the action Map
        lbm.actions << [
            printIndex: { idx ->
                    mod.println "actions.printIndex:  lb.logEntries.size() == ${lb.logEntries.size()}"
                    mod.println "\n************** $idx across files *************"

                    if (lb.indices[idx] == null) {
                        mod.println "actions.printIndex:  lb.indices[idx] == null"
                        lb.loadValueIndices("$idx") 
                    }

                    def indx = lb.indices["$idx"]
                    mod.println "\tFieldname: ${indx.fieldName}, Contains ${indx.valueMap.size()} different values"
                    mod.println "\tThis is the ${indx.fieldName} valueMap showing counts for each value:"
                    indx.valueMap.each {
                        mod.println "\t\t${indx.fieldName}:  $it.key => ${it.value.size()}"
                    }
                }, 
            showSummary: { 
                if (!(lb.logEntries.size() > 0)) {
                    // mod.println "actions.showSummary:  lb.logEntries.size() == ${lb.logEntries.size()}"
                    lb.runBatch()
                }
                else {
                    lb.printSummary()     
                }
            },  
            saveSession: { sourcePath ->
                lbm.model.saveSession(sourcePath)
            },
            showCSV: { 
                if (!(lb.logEntries.size() > 0)) {
                    lb.runBatch()
                }
                else {
                    lb.printCSV()     
                }
            },
            showHtml: { 
                if (!(lb.logEntries.size() > 0)) {
                    lb.runBatch()
                }
                else {
                    lb.printHtml()     
                }
            },
            processLogFile : { fn -> lb.processLogFile(lb.sourcePath, fn) },
            listSourceFiles: { lb.sourceFiles.each { mod.println "$it" }},
            exit: { println "Exiting"; System.exit(0) }
        ]
	}

    def mainSwingMenu(LogBrowserMenu lbm) {
        def lb = lbm.logBrowser
        def mod = lbm.model 
        // ota is the outputTextArea of the Swing app window
        def ota 
        def mainMenu
        def fileMenu
        def swing = new SwingBuilder()
        lbm.mainFrame = swing.frame(
            title:'LogBrowser Main Menu',
            location:[100,100],
            size:[1200,900],
            defaultCloseOperation:javax.swing.WindowConstants.EXIT_ON_CLOSE) {
                menuBar {
                    mainMenu = menu(text:'Log Browser Main') {
                        menuItem() { action(name: "List Source Files",  closure:{  mod.begin();lbm.actions.listSourceFiles(); mod.end();})}
                        // menuItem() { action(name: "Initialize Data",  closure:{ lb.runBatch() })}
                        menuItem() { action(name: "Show Summary Output",  closure:{  mod.begin(); lbm.actions.showSummary(); mod.end(); })}
                        menuItem() { action(name: "Show Summary CSV",  closure:{ lbm.actions.showCSV() })}
                        menuItem() { action(name: "Show Summary HTML",  closure:{  lbm.actions.showHtml() })}
                        menuItem() { action(name: "Print proxy values",  closure:{  mod.begin(); lbm.actions.printIndex("proxy");mod.end(); })}
                        menuItem() { action(name: "Print proxy_name values",  closure:{  mod.begin(); lbm.actions.printIndex("proxy_name"); mod.end();})}
                        menuItem() { action(name: "Print client_id values",  closure:{  mod.begin(); lbm.actions.printIndex("client_id");  mod.end();})}
                        menuItem() { action(name: "Print client_host values", closure:{  mod.begin(); lbm.actions.printIndex("client_host");  mod.end();})}
                        menuItem() { action(name: "Print client_ip values",  closure:{  mod.begin(); lbm.actions.printIndex("client_ip"); mod.end(); })}
                        menuItem() { action(name: "Print soap_site_Id values",  closure:{  mod.begin(); lbm.actions.printIndex("soap_site_Id"); mod.end(); })}
                        menuItem() { action(name: "Print soap_operation values",  closure:{  mod.begin(); lbm.actions.printIndex("soap_operation"); mod.end(); })}
                        menuItem() { action(name: "Print request_verb values",  closure:{  mod.begin(); lbm.actions.printIndex("request_verb"); mod.end();})}
                        menuItem() { action(name: "Print response_status_code values",  closure:{  mod.begin(); lbm.actions.printIndex("response_status_code");mod.end();  })}
                        menuItem() { action(name: "Print request_path values",  closure:{  mod.begin(); lbm.actions.printIndex("request_path"); mod.end(); })}
                        menuItem() { action(name: "Print available field names",  closure:{  mod.begin(); lb.logEntries[1].fields.keySet().each {mod.println "$it"};mod.end();  })}
                              menu("File Sub Menu"){
                                 lb.sourceFiles.each { fn ->
                                    def parts = fn.tokenize(".")
                                    def prompt = parts[0].capitalize()
                                    menuItem() { 
                                        action(name: fn, 
                                            closure:{ 
                                                lb.logEntries = []; lb.indices=[:]; 
                                                mod.begin(); lb.processLogFile(lb.sourcePath, fn); mod.end();
                                                lbm.mainFrame.hide();lbm.fileFrame=swingFileMenu( lbm, prompt, fn, lbm.mainFrame).show()
                                            }
                                        )
                                    }
                                }
                        }
                        menuItem() { action(name: "Save Session Output",  
                            closure:{ 
                                lbm.actions.saveSession(lb.sourcePath); 
                            })}
                        menuItem() { action(name: "Exit",  closure:{ lbm.actions.exit()})}
                    }

                }
                scrollPane {
                    panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'Command Output')) {
                        borderLayout()
                        scrollPane(constraints:CENTER, border:BorderFactory.createRaisedBevelBorder()) {
                            // textArea(text:'Output Window Contents', toolTipText:'Command output goes in this window')
                            ota = textArea(text: bind(source: mod, sourceProperty: "outputStr"))
                        }
                    }
                }
            }
        lbm.mainFrame.show()
    }

    static def swingFileMenu(LogBrowserMenu lbm, String title, String logFileName, javax.swing.JFrame mainFrame) { 
        def lb = lbm.logBrowser
        def mod = lbm.model  

        def swing = new SwingBuilder()
        lbm.fileFrame = swing.frame(
          title: "$title",
          location:[100,100],
          size:[1200,900]) {
            menuBar {
                menu(text:"$title") {  
                menuItem() { action(name: "Show Summary Output", closure:{ mod.begin(); lbm.actions.showSummary(); mod.end(); }) }
                menuItem() { action(name: "Show Summary CSV", closure:{ mod.begin(); lbm.actions.showCSV() }) }
                menuItem() { action(name: "Show Summary HTML", closure:{ mod.begin(); lbm.actions.showHtml()  }) }
                menuItem() { action(name: "Print proxy values", closure:{mod.begin(); lbm.actions.printIndex("proxy"); mod.end();  }) }
                menuItem() { action(name: "Print proxy_name values", closure:{mod.begin(); lbm.actions.printIndex("proxy_name"); mod.end(); }) }
                menuItem() { action(name: "Print client_id values", closure:{mod.begin(); lbm.actions.printIndex("client_id"); mod.end(); }) }
                menuItem() { action(name: "Print client_host values", closure:{mod.begin(); lbm.actions.printIndex("client_host"); mod.end(); }) }
                menuItem() { action(name: "Print client_ip values", closure:{mod.begin(); lbm.actions.printIndex("client_ip"); mod.end();}) }
                menuItem() { action(name: "Print soap_site_Id values", closure:{mod.begin(); lbm.actions.printIndex("soap_site_Id"); mod.end(); }) }
                menuItem() { action(name: "Print soap_operation values", closure:{mod.begin(); lbm.actions.printIndex("soap_operation"); mod.end(); }) }
                menuItem() { action(name: "Print request_verb values", closure:{mod.begin(); lbm.actions.printIndex("request_verb"); mod.end(); }) }
                menuItem() { action(name: "Print response_status_code values", closure:{mod.begin(); lbm.actions.printIndex("response_status_code"); mod.end();  }) }
                menuItem() { action(name: "Print request_path values", closure:{mod.begin(); lbm.actions.printIndex("request_path"); mod.end(); }) }
                menuItem() { action(name: "Return to Main Menu", closure:{ 
                        lb.logEntries = []; lb.indices=[:]; lb.runBatch()
                        lbm.fileFrame.hide(); lbm.mainSwingMenu(lbm)
                        lbm.mainFrame.show()
                        mod.begin();mod.println "Main Menu";mod.end()
                    }) }
                menuItem() { action(name: "Exit", closure: { lbm.actions.exit() }  ) }
                }
            }
            scrollPane {
                panel(border:BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), 'Command Output')) {
                    borderLayout()
                    scrollPane(constraints:CENTER, border:BorderFactory.createRaisedBevelBorder()) {
                        // textArea(text:'Output Window Contents', toolTipText:'Command output goes in this window')
                        ota = textArea(text: bind(source: mod, sourceProperty: "outputStr"))
                    }
                }
            }
        }
        lbm.fileFrame.show()
    }
} // LogBrowserMenu application class
