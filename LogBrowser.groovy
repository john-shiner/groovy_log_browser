// @author:  John Shiner
// 1/22/2014 

package jfs
import jfs.AppConsole.*
import groovy.swing.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.awt.BorderLayout
import javax.swing.BorderFactory

/************************ LogBrowser classes **************************/
class LogEntry {
    def rawString
    def sourcePath
    def sourceFileName

    def rawFields = [] // parsed array of contents within each logentry field (delimited by "|")
    def fields = [:] // name:value pairs for each log entry -- no payloads or other fields with ":" chars in value
    def parseFields() { 
        rawFields = rawString.tokenize('|') // parse each logEntry field -- this has all the information in the logEntry
        def cnt = 0
        def m = [:] // interim Dictionary (Map) to manage filtering rawFields
        rawFields.each { f ->
            def ttok = [ (++cnt) : f ]
            m += ttok
        }
        // we are skipping the payload fields (last two)
        def sz = rawFields.size()-3
        def workingSet = []
        [1..sz].each { ind ->
            def overall = rawFields[ind]
            // do not include fields with XML value strings
            if (overall[0] != "response_content") {
                workingSet += rawFields[ind]
            }
        }
        def sz2 = workingSet.size() - 1
        m.each { k, v ->
            def item = v.tokenize(':')
            // yet another filter to remove multi-token value fields (i.e. fields that include  ":" in the value string)
            if (item.size() <= 2) {
               //  println "$k = $item"
               // fields are the final working list of fields for indexing
                fields += [(item[0]):(item[1])]
            } // if
        } // m
    } // parseFields
} 

// LogBrowser is the application class that manages the processing of each log file
class LogBrowser {
    def mod = Console

    // supportedIndices are the fields that are interesting to count and track value counts.  Update as desired
   //  def supportedIndices =  ["client_id", "proxy_name", "request_verb", "soap_siteId", "soap_operation", "proxy", "response_status_code"]
    def supportedIndices = ["client_host", "client_id", "client_ip", "environment", "organization", "proxy", "proxy_basepath", "proxy_name", "proxy_revision", "request_path", "request_uri", "request_verb", "response_reason_phrase", "response_status_code", "soap_operation", "soap_siteId", "target_basepath", "target_host", "target_ip", "virtual_host"]

    // def supportedIndices =  ["client_id", "client_host", "client_ip", "proxy"]
    def sourceFiles = [] // array of filenames, all assumed to be in the same sourcePath directory
    def sourcePath = ""  // path where log files will be found
    def htmlOutputFileName = "markup.html"
    def indices = [:]    // holds a map of supportedIndices to a corresponding IndexMgr to hold counts and referenced logEntries
    def logEntries = []  // holds all the parsed logEntry rows (1 per row in the logFile)
    def loadLogFile(filePath, fileName) {
        // This parses the logFile and does the heavy lifting
        def fn = fileName
        def fp = filePath
        File f = new File(fp+'/'+fn)
        List lines = f.readLines()
        lines.each { line ->
            logEntries << new LogEntry(sourcePath:fp, sourceFileName:fn, rawString:"${line}")
        }
        // initialize the IndexMgr map for each value in supportedIndices
        loadIndices()
        
        logEntries.each { le ->
            le.parseFields()
        }
        
        loadValueIndices()
    }
    
    // this produces the index summary output by fileName
    def processLogFile(path, filename){
        // reset the logEntry count for each file
        // if processing many files, you could comment this out and report on the total of the logEntries
        logEntries = []
        loadLogFile(path, filename)
        mod.println "Logfile:  ${filename}"
        mod.println "\tLogEntries.size() = ${logEntries.size()}"

        for (indx in indices) {
            mod.println "\n\t*****************"
            mod.println "\tFieldname: ${indx.value.fieldName}, Contains ${indx.value.valueMap.size()} different values"
            mod.println "\tThis is the ${indx.value.fieldName} valueMap showing counts for each value:"
            indx.value.valueMap.each {
                mod.println "\t\t${indx.value.fieldName}: $it.key => ${it.value.size()}"
            }
        }

    }

    // this produces the index summary output 
    def printSummary() {
        // reset the logEntry count for each file
        // if processing many files, you could comment this out and report on the total of the logEntries
        // logEntries = []
        mod.println "\n||************************************||"
        mod.println   "|| *    Combined Logfile Summary        *||"
        mod.println   "||************************************||"
        mod.println "\tLogEntries.size() = ${logEntries.size()}"
        mod.println "\n****** Source Files Processed  *****"
        this.sourceFiles.each {
            mod.println "$it"
        }
        mod.println "\n************** Indices across files *************"
        for (indx in indices) {
            // mod.println "\n\t*****************"
            mod.println "\tFieldname: ${indx.value.fieldName}, Contains ${indx.value.valueMap.size()} different values"
            mod.println "\tThis is the ${indx.value.fieldName} valueMap showing counts for each value:"
            indx.value.valueMap.each {
                mod.println "\t\t${indx.value.fieldName}:  $it.key => ${it.value.size()}"
            }
        }
    }
    
    // this produces the index summary output for CSV processing
    def printCSV() {
       // def writer = new FileWriter(sourcePath + '\\' + "indexSummary${new Date().time}.csv")
       def writer = new FileWriter(sourcePath + '/' + "indexSummary${new Date().time}.csv")
        // def writer = new StringWriter()  // html is written here by markup builder

        writer.println "\nFieldName; Value; Count"
        println "Total; Log Entries; ${logEntries.size()}"
        for (indx in indices) {
            indx.value.valueMap.each {
                writer.println "${indx.value.fieldName}; ${it.key}; ${it.value.size()}"
            }
        }
        writer.close()
    }

    def printHtml() {
        def writer = new FileWriter(sourcePath + '/' + "markup${new Date().time}.html")
        // def writer = new StringWriter()  // html is written here by markup builder

        def html = new groovy.xml.MarkupBuilder(writer)
        html.html {
            head {
                title 'Summary Output'
            }
            body {
                for (indx in indices) {
                    h4 "Index ${indx.value.fieldName} Value Counts"
                    table(border:1) {
                        th {
                            tr {
                                td 'Value'  
                                td "Count"
                            }
                        }

                        indx.value.valueMap.each { k, v ->
                            tr {
                                td "$k"
                                td "${v.size()}"
                            } 
                        }
                    }
                }
                // println writer.toString()
            }
        }
    }

    def queryResultsHtml(String title, String resultsMessage,  ArrayList resultsList) {
        // work in progress... not enabled yet
        def writer = new FileWriter(sourcePath + '/' + "query${new Date().time}.html")
        // def writer = new StringWriter()  // html is written here by markup builder

        def html = new groovy.xml.MarkupBuilder(writer)
        html.html {
            head {
                title 'title'
            }
            body {
                h3 "$title"
                p "${resultsMessage}"
                table(border:1) {
                    th {
                        tr {
                            td 'Field'
                            for (lgEntry in resultsList) { 
                                td "$lgEntry\n"
                            }
                        }
                    }

                    for (entry in resultsList[0].fields.keySet()) {
                        tr {
                            td "${entry}\n"
                            for (le in resultsList) {
                                td "${le.fields["$entry"]}\n"
                            }
                        } 
                    }
                }
            }
        }
    }

    def runBatch() {
        // There are two paths in this function that can be run together or separately
        // The first processes each file and resets the counts for each file
        // The second processes all the files and reports the counts against the total calls across all files
        // walk through and process each sourceFile

        println "\nInitializing Data..."
        // Print out a combined summary report
        logEntries = []
        indices = [:]
        def maxCnt = sourceFiles.size()-1

        sourceFiles.eachWithIndex { s, i ->
            def fn = s
            loadLogFile(sourcePath, fn)
        } // eachWithIndex
    }    
             
    def loadIndices() {
        for (indx in supportedIndices) {
            indices[indx] = new IndexMgr(fieldName:indx) 
        }
        indices
    }
    // for each supportedIndices, count and reference each LogEntry with the same value
    def loadValueIndices() {
        logEntries.each { le ->
            // this should only be called after loadIndices and after the logFile has been parsed into logEntries
            indices.each { idx ->
                def idxField = idx.key
                def idxValue = le.fields[idxField]
                if (idx.value.valueMap[idxValue])
                     idx.value.valueMap[idxValue] += le
                else 
                    idx.value.valueMap[idxValue] = [ le ]
            } // idx
        } // le
    } // loadValueIndices()
    
    def loadValueIndices(String colName) {
        indices[colName] = new IndexMgr(fieldName: colName) 
        IndexMgr idx = indices[colName]
        logEntries.each { le ->
            def idxField = colName
            def idxValue = le.fields[idxField]
            if (idx.valueMap[idxValue])
                 idx.valueMap[idxValue] += le
            else 
                idx.valueMap[idxValue] = [ le ]
        } // le
    } // loadValueIndices(colName)
        static Map loadIndices(indexNames) {
        def indexMap = [:]
        for (indx in indexNames) {
            indexMap[indx] = new IndexMgr(fieldName:indx) 
        }
        indexMap
    }

    static def loadValueIndices(java.util.LinkedHashMap indexMap, java.util.ArrayList leList) {
        leList.each { le ->
            // this should only be called after loadIndices and after the logFile has been parsed into logEntries
            indexMap.each { idx ->
                def idxField = idx.key
                def idxValue = le.fields[idxField]
                if (idx.value.valueMap[idxValue])
                     idx.value.valueMap[idxValue] += le
                else 
                    idx.value.valueMap[idxValue] = [ le ]
            } // idx
        } // le
    } // loadValueIndices()


}

// indexMgr keeps track of logEntries for each supportedIndices that match each value that appears in the logFile
class IndexMgr {
    def fieldName
    def valueMap = [:]
    def add = { logEntry ->
        def le = logEntry
        def val = le.fields.fieldName?:null
        if (valueMap.keySet().contains(le.fields[fieldName])) {
            valueMap[le.fields(fieldName)] << le
        } else {
            valueMap[le.fields(fieldName)] = [ le ] // create a list for the value with this logEntry in it
       }
    } // add( logEntry)
}
