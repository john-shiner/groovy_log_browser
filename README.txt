LogBrowser

This is mainly a Groovy programming exercise and the code is shared for reference though both components have some useful applications.
The LogBrowserMenu app is a combination of two custom groovy apps – a Menu application builder and a Log indexing application. The LogBrowser app will parse a log file in a standard format and create indexes on field names, then display the values/counts in each index. The Main Menu loads in all the specified sourcefiles , parses each row into a LogEntry, creates some default indices, and provides menu options to dynamically calculate and display other interesting field values.
Scrubbed logfiles are included for processing. The files have to be in the same directory (set the sourcePath value in the sample code).
The code for the non-swing version should be executed in a cmd window (groovy LogBrowserMenu.groovy) — it doesn’t play nice in a GroovyConsole unless you comment out the BufferedReader.readline() lines and uncomment the swing.JOptionPane input lines in the MenuApp.runLoop() method.

The swing version executes equally well from the command line or from the build command in Sublime Text.

Basic features:
– Dynamically creates menus for each logFile being analyzed.
– Provides analysis of logFile fields by file and in total
– Provides basic logEntry searches by field value
– Generates HTML, CSV, and text output of field-value counts
– Basic capability to view logEntry search results either as rows of logEntry fields by logEntry, 
or as rows of logEntry fieldNames by columns for each logEntry value
– Separate files for each class group
– Basic gradle build script

Swing Version adds:
– Swing (SwingBuilder) UI instead of command line 
– Cleaner command output and session storage
– Direct menu access to File analysis windows
– A new Session manager class (AppConsole.ConsoleModel) to clean up and simplify command output and session saving

Groovy features employed:
– Query extensions on collections
– String parsing (regular expressions, tokenizing by separator values)
– File IO
– Storing closures as values in a Map
– Creating closure maps to support menu extensibility and for simplifying programming interface to reusable features
– Test driven development – learned and validated groovy features with assertion test scripts before implementing 
in application
– Sublime Text 2 Groovy Build (Ctrl-B to “build”/run the scripts from the editor)
– Basic gradle build
– HTML generated from MarkupBuilder
– Swing forms and menus generated from SwingBuilder

USAGE (non-swing): 
– Compile the groovy files: groovyc MenuApp.groovy LogBrowser.groovy LogBrowserMenu.groovy
– Set the sourcePath and sourceFiles variables in the main() function to point to the input log files. 
– run the main script (groovy LogBrowserMenu)
– Everything else is dynamically created. CSV and HTML options generate output files in sourcePath folder.

USAGE (swing):
– Set the sourcePath and sourceFiles variables in the main() function to point to the input log files. (Make sure the sample log files are in your sourcePath folder).
– Compile the groovy files (gradle compile) or: groovyc AppConsole.groovy, LogBrowser.groovy, MenuApp.groovy
– run the ‘groovy LogBrowserSwingApp’ to start the application