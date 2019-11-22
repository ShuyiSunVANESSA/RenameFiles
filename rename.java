import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.List;

class rename {
    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
        } else {
            ArrayList<String> entries = parse(args);
            renameFiles(entries);
            System.out.println("Commands completed with no error.");
        }
    }

    // Print rename syntax
    static void printHelp() {
        System.out.println("(c) 2019 Shuyi Sun. Last revised: Sept 11, 2019.");
        System.out.println("Usage: java rename [-option argument1 argument2 ...]\n");
        System.out.println("Options:");
        System.out.println("-help                     :: display this help");
        System.out.println("-prefix [string]          :: rename the file by prepending [string] to the filename");
        System.out.println("-suffix [string]          :: rename [filename] so that it ends with [string]. ");
        System.out.println("-replace [str1] [str2]    :: rename [filename] by replacing all instances of [str1] with [str2]. This option requires exactly two arguments.");
        System.exit(1);
    }

    static void printError(String errMsg) {
        System.out.println("Error: " + errMsg);
        System.exit(1);
    }

    // Parse the options and arguments into a queue of strings
    static ArrayList<String> parse(String[] entries) {
        String option = null;
        String expectedNext = "option";
        String arg = null;
        ArrayList<String> parsedEntries = new ArrayList<String>();
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        DateFormat tf = new SimpleDateFormat("HH-mm-ss");
        Date dateobj = new Date();
        String curDate = df.format(dateobj);
        String curTime = tf.format(dateobj);

        // go through entries for the first time, checking -help and errors
        for(String entry : entries) {
            // assume that options start with a dash
            if (entry.startsWith("-")) {
                // save arg into the array since encoutering an option means prefix/suffix finished taking args
                if (option != null
                        && (option.equals("prefix") || option.equals("suffix") || option.equals("replace"))
                        && arg != null) {
                    parsedEntries.add(arg);
                }
                // initialize arg everytime we encounter and option
                option = entry.substring(1);   // skip leading "-"
                arg = null;
                // expectedNext may be null after accepting args, the case we need to exclude here is expecting
                if (expectedNext != null && !expectedNext.equals("option") && !expectedNext.equals("filenames/option")) {
                    printError("Unexpected option: " + entry + " Expected argument.");
                }
                if (option.equals("help")) {
                    printHelp();
                }
                else if (option.equals("prefix") || option.equals("suffix")) {
                    expectedNext = "argument";
                    parsedEntries.add(entry);
                }
                else if (option.equals("replace")) {
                    expectedNext = "2 arguments";
                    parsedEntries.add(entry);
                } else if (option.equals("file")) {
                    expectedNext = "filenames";
                    parsedEntries.add(entry);
                }
                else {
                    printError("Unrecognized option: " + entry);
                }
            // processing args
            } else {
                // if we already have a action option, and then find a second option
                // before we've found the corresponding argument, it's an error.
                if (entry.contains("@date")) {
                    entry = entry.replace("@date", curDate);
                } if (entry.contains("@time")) {
                    entry = entry.replace("@time", curTime);
                }
                // when option is prefix/suffix and have multiple args following
                if (expectedNext == null && (option.equals("prefix") || option.equals("suffix")) && arg != null) {
                    arg = arg + entry;
                }
                else if (expectedNext.equals("argument")) {
                    // could be arg which will be combined as one arg
                    // Or could be option
                    expectedNext = null;
                    arg = entry;
                }
                else if (expectedNext.equals("2 arguments")) {
                    expectedNext = "argument";
                    parsedEntries.add(entry);
                } else if (expectedNext.equals("filenames") || expectedNext.equals("filenames/option")) {
                    parsedEntries.add(entry);
                    expectedNext = "filenames/option";
                } else {
                    printError("Unexpected argument: " + entry + expectedNext + option);
                }
            }
        }
        if (arg != null && !option.equals("file")) {
            parsedEntries.add(arg);
        }
        // # of files cannot be anticipated
        if (expectedNext != null && !expectedNext.equals("filenames") && !expectedNext.equals("filenames/option")) {
            printError("Expected " + expectedNext + " but found null.");
        }
        System.out.println("Exit for parsing successfully, Parsed entries : " + parsedEntries);
        return parsedEntries;
    }

    static void renameFiles (ArrayList<String> parsedArgs) {
        // a list of filenames which are to be processed
        int indexOfFilenameStart = parsedArgs.indexOf("-file") + 1;
        List<String> filenames = new ArrayList<>();
        for (String name : parsedArgs.subList(indexOfFilenameStart, parsedArgs.size())){
            if (name.startsWith("-")) {
                break;
            } else {
                filenames.add(name);
            }
        }

        for (int i = 0; i < (filenames.size()+1); i++) {
            parsedArgs.remove(indexOfFilenameStart-1);
        }
        String prefix = "";
        String suffix = "";
        List<String> replaceOptions = new ArrayList<>();
        int countReplace = 0;
        for (int i = 0; i < parsedArgs.size(); ) {
            String option = parsedArgs.get(i);
            if (option.equals("-prefix")) {
                prefix = parsedArgs.get(i+1) + prefix;
                i = i+ 2;
            } else if (option.equals("-suffix")) {
                suffix = suffix + parsedArgs.get(i+1);
                i = i+ 2;

            } else if (option.equals("-replace")) {
                countReplace++;
                replaceOptions.add(parsedArgs.get(i));
                replaceOptions.add(parsedArgs.get(i+1));
                replaceOptions.add(parsedArgs.get(i+2));
                i = i+ 3;
            }
        }

        for (String filename : filenames) {
            int countReplaceForCurFile = countReplace;
            String newName = filename;
            if (!prefix.isEmpty()){
                newName = prefix + newName;
            } if (!suffix.isEmpty()){
                newName = newName + suffix;
            }
            int shift = 1;
            while (countReplaceForCurFile > 0){
                if (newName.contains(replaceOptions.get(shift))) {
                    newName = newName.replace(replaceOptions.get(shift), replaceOptions.get(shift + 1));
                    shift += 3;
                    countReplaceForCurFile--;
                } else {
                    // replacement errors
                    printError("Cannot find " + replaceOptions.get(shift) + " to be replaced in filename: " + newName);
                }
            }
            try {
                File fileToBeRenamed = new File(filename);
                File fileWithNewName = new File(newName);
                Boolean doesSuccess = fileToBeRenamed.renameTo(fileWithNewName);
                if (!doesSuccess) {
                    printError("Fail to rename file " + filename + ", please verify your commands.");
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}