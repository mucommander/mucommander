package com.mucommander.text;

import com.mucommander.conf.ConfigurationManager;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Locale;


/**
 * This class takes care of all text localization issues by loading all text entries from 
 * a dictionary file on startup and translating them into the current language on demand.
 *
 * <p>All public methods are static to make it easy to call them throughout the application</p>
 *
 * <p>See dictionary file for more information about dictionary file grammar.</p>
 *
 * @author Maxence Bernard
 */
public class Translator {

    /** Hashtable that maps text keys to values */
    private static Hashtable dictionary;

    /** List of all available languages in the dictionary file (UPPER CASED) */
    private static Vector availableLanguages;

    /** Current language (UPPER CASED) */
    private static String language;

    /** Path to the dictionary file inside the JAR file */
    private final static String DICTIONARY_FILE_PATH = "/dictionary.txt";

    /** Default language (UPPER CASED) */
    private final static String DEFAULT_LANGUAGE = "EN";

    /** Preferred language's configuration key */
    private final static String LANGUAGE_CONFIGURATION_KEY = "prefs.language";

    /** Key for available languages */
    private final static String AVAILABLE_LANGUAGES_KEY = "available_languages";

    /** Singleton instance */
    private final static Translator instance = new Translator(DICTIONARY_FILE_PATH);


    /**
     * Creates a Translator instance and loads all entries from the dictionary file. 
     *
     * @param filePath the path to the dictionary file.
     */
    private Translator(String filePath) {
        try {
            loadDictionaryFile(filePath);
        } catch (IOException e) {
            new RuntimeException("Translator.init: unable to load dictionary file "+e);
        }
    }


    /**
     * Determines and sets current language based on the given list of available languages
     * and language in preferences if it has been set and if not, on system's language.
     * <p>
     * If the language set in preferences or the system's language is not available, use default language (English).
     */
    private static void determineCurrentLanguage(Vector availableLanguages) {
        String lang = ConfigurationManager.getVariable(LANGUAGE_CONFIGURATION_KEY);

        if(lang==null) {
            // language is not set in preferences, use system's language
            // Try to match language with the system's language, only if the system's language
            // has values in dictionary, otherwise use default language (English).
            lang = Locale.getDefault().getLanguage();
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Language not set in preferences, trying to match system's language ("+lang+")");
        }
        else {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Language in prefs: "+lang);
        }
		
        lang = lang.toUpperCase();
		
        // Determines if language is one of the languages declared as available
        if(availableLanguages.contains(lang)) {
            // Language is available
            Translator.language = lang;
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Language "+lang+" is available.");
        }
        else {
            // Language is not available, fall back to default language (English)
            Translator.language = DEFAULT_LANGUAGE;
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Language "+lang+" is not available, falling back to default language "+DEFAULT_LANGUAGE);
        }
		
        // Set preferred language in configuration file
        ConfigurationManager.setVariable(LANGUAGE_CONFIGURATION_KEY, Translator.language);

        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Current language has been set to "+Translator.language);
    }
	
	
    /**
     * Reads the dictionary file which contains localized text entries.
     */
    private void loadDictionaryFile(String filePath) throws IOException {
        availableLanguages = new Vector();
        dictionary = new Hashtable();

        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filePath), "UTF-8"));
        String line;
        String key;
        String lang;
        String text;
        StringTokenizer st;
        int nbEntries = 0;

        while ((line = br.readLine())!=null) {
            if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                st = new StringTokenizer(line);

                try {
                    // Sets delimiter to ':'
                    key = st.nextToken(":").trim();
					
                    // Special key that lists available languages, must
                    // be defined before any other entry
                    if(Translator.language==null && key.equals(AVAILABLE_LANGUAGES_KEY)) {
                        // Parse comma separated languages
                        st = new StringTokenizer(st.nextToken(), ",\n");
                        while(st.hasMoreTokens())
                            availableLanguages.add(st.nextToken().trim().toUpperCase());

                        if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Available languages= "+availableLanguages);

                        // Determines current language based on available languages and preferred language (if set) or sytem's language 
                        determineCurrentLanguage(availableLanguages);

                        continue;
                    }
					
                    lang = st.nextToken().toUpperCase().trim();

                    // Delimiter is now line break
                    text = st.nextToken("\n");
                    text = text.substring(1, text.length());

                    // Replace "\n" strings in the text by \n characters
                    int pos = 0;

                    while ((pos = text.indexOf("\\n", pos))!=-1)
                        text = text.substring(0, pos)+"\n"+text.substring(pos+2, text.length());

                    // Replace "\\uxxxx" unicode charcter strings by the designated character
                    pos = 0;

                    while ((pos = text.indexOf("\\u", pos))!=-1)
                        text = text.substring(0, pos)+(char)(Integer.parseInt(text.substring(pos+2, pos+6), 16))+text.substring(pos+6, text.length());

                    // Add entry for current language, or for default language if a value for current language wasn't already set
                    if(lang.equals(language) || (lang.equals(DEFAULT_LANGUAGE) && dictionary.get(key)==null))
                        put(key, text);
					
                    nbEntries++;
                } catch (Exception e) {
                    if(com.mucommander.Debug.ON) e.printStackTrace();
                    com.mucommander.Debug.trace("error in line "+line+" ("+e+")");
                }
            }
        }
        br.close();
    }


    /**
     * Empty method that does nothing but trigger the static initializer block.
     */
    public static void init() {
    }

	
    /**
     * Returns the current language.
     *
     * @return lang 2-letter language code
     */
    public static String getLanguage() {
        return language;
    }
	
	
    /**
     * Returns an array of available languages, each described by a 2-letter
     * String ("en", "fr", "jp"...).
     *
     * @return a String array of 2-letter language codes.
     */
    public static String[] getAvailableLanguages() {
        return (String[])availableLanguages.toArray(new String[]{});
    }


    /**
     * Returns true if the given key exists (has a corresponding value) in the current language.
     */
    public static boolean entryExists(String key) {
        return (String)dictionary.get(key.toLowerCase())!=null;
    }


    /**
     * Adds the key/text value to the dictionary.
     *
     * @param key a case-insensitive key.
     * @param text localized text.
     */
    private static void put(String key, String text) {
        // Adds a new entry to the dictionary
        dictionary.put(key.toLowerCase(), text);
    }


    /**
     * Returns the localized text String corresponding to the given key and
     * current language (or default language if a value for current language is not available),
     * and replaces  the %1, %2... parameters by their given value.
     *
     * @param key a case-insensitive key.
     * @param paramValues array of parameters which will be used as values for variables.
     */
    public static String get(String key, String paramValues[]) {
        // Returns the localized text
        String text = (String)dictionary.get(key.toLowerCase());

        if (text==null) {
            if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Unknown key "+key, -1);
            return key;
        }

        // Replace %1, %2 ... parameters by their value
        if (paramValues!=null) {
            int pos = -1;
            for(int i=0; i<paramValues.length; i++) {
                while(++pos<text.length()-1 && (pos = text.indexOf("%"+(i+1), pos))!=-1)
                    text = text.substring(0, pos)+paramValues[i]+text.substring(pos+2, text.length());
            }
        }

        // Replace $[key] occurrences by their value
        int pos = 0;
        int pos2;
        String variable;

        while ((pos = text.indexOf("$[", pos))!=-1) {
            pos2 = text.indexOf("]", pos+1);
            variable = text.substring(pos+2, pos2);
            text = text.substring(0, pos)+get(variable, paramValues)+text.substring(pos2+1, text.length());
        }

        return text;
    }


    /**
     * Convenience method, equivalent to <code>get(key, (String[])null)</code>.
     *
     * @param key a case-insensitive key.
     */
    public static String get(String key) {
        return get(key, (String[])null);
    }


    /**
     * Convenience method, equivalent to <code>get(key, new String[]{paramValue1})</code>)</code>.
     *
     * @param key a case-insensitive key.
     * @param paramValue1 first parameter which will be used to replace %1 variables.
     */
    public static String get(String key, String paramValue1) {
        return get(key, new String[] {paramValue1});
    }


    /**
     * Convenience method, equivalent to <code>get(key, new String[]{paramValue1, paramValue2})</code>)</code>.
     *
     * @param key a case-insensitive key.
     * @param paramValue1 first parameter which will be used to replace %1 variables.
     * @param paramValue2 second parameter which will be used to replace %2 variables.
     */
    public static String get(String key, String paramValue1, String paramValue2) {
        return get(key, new String[] {paramValue1, paramValue2});
    }


    /**
     * Convenience method, equivalent to <code>get(key, new String[]{paramValue1, paramValue2, paramValue3})</code>)</code>.
     *
     * @param key a case-insensitive key.
     * @param paramValue1 first parameter which will be used to replace %1 variables.
     * @param paramValue2 second parameter which will be used to replace %2 variables.
     * @param paramValue3 third parameter which will be used to replace %3 variables.
     */
    public static String get(String key, String paramValue1, String paramValue2, String paramValue3) {
        return get(key, new String[] {
                       paramValue1, paramValue2, paramValue3
                   });
    }


    /**
     * Based on the number of supplied command line parameters, this method either :
     * <ul>
     * <li>Looks for and reports any missing or unused dictionary entry,
     * using the supplied source folder path to look inside source files
     * for references to dictionary entries.
     * <li>Merges a new language's entries from a dictionary file into a new one.
     * </ul>
     */
    public static void main(String args[]) throws IOException {
        /*	
        // Looks for missing and unused entries
        if(args.length<4) {
        Enumeration languages = dictionaries.keys();
        Vector langsV = new Vector();
        while(languages.hasMoreElements())
        langsV.add(languages.nextElement());
				
        String langs[] = new String[langsV.size()];
        langsV.toArray(langs);
			
        com.mucommander.file.AbstractFile sourceFolder = com.mucommander.file.AbstractFile.getAbstractFile(args[0]);
		
        System.out.println("\n##### Looking for missing entries #####");
        checkMissingEntries(sourceFolder, langs);

        System.out.println("\n##### Looking for unused entries #####");
        checkUnusedEntries(sourceFolder, langs);
        }
        // Integrates a new language into the dictionary
        else {
        */
        // Parameters order: originalFile newLanguageFile resultingFile newLanguage
        addLanguageToDictionary(args[0], args[1], args[2], args[3]);
        /*
          }
        */
    }


    /**
     * Checks for missing dictionary entries in the given file or folder and reports them on the standard output.
     * If the given file is a folder, recurse on each file that it contains, if it's a 'regular' file and the
     * extension is '.java', looks for any calls to {@link #Translator.get(String), Translator.get()} and checks
     * that the request entry has a value in each language's dictionary.
     */ 
    /*
      private static void checkMissingEntries(com.mucommander.file.AbstractFile file, String languages[]) throws IOException {
      if(file.isDirectory()) {
      com.mucommander.file.AbstractFile children[] = file.ls();
      for(int i=0; i<children.length; i++)
      checkMissingEntries(children[i], languages);
      }
      else if(file.getName().endsWith(".java")) {
      BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
      String line;
      int pos;
      String entry;
      String value;
      String language;
      while((line=br.readLine())!=null) {
      if(!line.trim().startsWith("//") && (pos=line.indexOf("Translator.get(\""))!=-1) {
      try {
      entry = line.substring(pos+16, line.indexOf("\"", pos+16));
      for(int i=0; i<languages.length; i++) {
      language = languages[i];
      if((String)((Hashtable)dictionaries.get(language)).get(entry)!=null || (!language.equalsIgnoreCase("en") && (value=(String)((Hashtable)dictionaries.get("en")).get(entry))!=null && value.startsWith("$")))
      continue;
      System.out.println("Missing "+language.toUpperCase()+" entry '"+entry+"' in "+file.getAbsolutePath());
      }
      }
      catch(Exception e) {
      }
      }
      }
      br.close();
      } 
      }
    */


    /**
     * Checks all enties in all dictionaries, checks that they are used in at least one source file
     * in or under the supplied folder, and reports unused entries on the standard output.
     */ 
    /*
      private static void checkUnusedEntries(com.mucommander.file.AbstractFile sourceFolder, String languages[]) throws IOException {
      Enumeration entries;
      String entry;
      for(int i=0; i<languages.length; i++) {
      entries = ((Hashtable)dictionaries.get(languages[i])).keys();
      while(entries.hasMoreElements()) {
      entry = (String)entries.nextElement();

      if(!isEntryUsed(entry, sourceFolder))
      System.out.println("Unused "+languages[i].toUpperCase()+" entry "+entry);
      }
      }
      }
    */

    /**
     * Checks if the given entry is used in the supplied file or folder.
     */
    /*
      private static boolean isEntryUsed(String entry, com.mucommander.file.AbstractFile file) throws IOException {
      if(file.isDirectory()) {
      com.mucommander.file.AbstractFile children[] = file.ls();
      for(int i=0; i<children.length; i++)
      if(isEntryUsed(entry, children[i]))
      return true;
      return false;
      }
      else if(file.getName().endsWith(".java")) {
      BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
      String line;
      int pos;
      while((line=br.readLine())!=null) {
      if(!line.trim().startsWith("//") && (pos=line.indexOf("\""+entry+"\""))!=-1) {
      br.close();
      return true;
      }
      }
      br.close();
      return false;
      }
		
      return false;
      }
    */

    /**
     * Merges a dictionary file with another one, adding entries of the specified new language.
     * <p>This method is used to merge dictionary files sent by contributors.
     *
     * @param originalFile current version of the dictionary file
     * @param newLanguageFile dictionary file containing new language entries
     * @param resultingFile merged dictionary file
     * @param newLanguage new language
     */
    private static void addLanguageToDictionary(String originalFile, String newLanguageFile, String resultingFile, String newLanguage) throws IOException {
        newLanguage = newLanguage.toUpperCase();
		
        // Initialize streams
        BufferedReader originalFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(originalFile)), "UTF-8"));
        BufferedReader newLanguageFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(newLanguageFile)), "UTF-8"));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultingFile), "UTF-8"));

        // Parse new language's entries
        String line;
        int lineNum = 0;
        String key;
        String lang;
        String text;
        StringTokenizer st;
        Hashtable newLanguageEntries = new Hashtable();
        while ((line = newLanguageFileReader.readLine())!=null) {
            try {
                if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                    st = new StringTokenizer(line);

                    // Sets delimiter to ':'
                    key = st.nextToken(":");
                    lang = st.nextToken();

                    if(lang.equalsIgnoreCase(newLanguage)) {
                        // Delimiter is now line break
                        text = st.nextToken("\n");
                        text = text.substring(1, text.length());

                        newLanguageEntries.put(key, text);
                    }
                }
                lineNum++;
            }
            catch(Exception e) {
                if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("caught "+e+" at line "+lineNum);
                return;
            }
        }

        // Insert new language entries in resulting file
        boolean keyAlreadyHasNewLanguage = false;
        String currentKey = null;
        while ((line = originalFileReader.readLine())!=null) {
            boolean emptyLine = line.trim().startsWith("#") || line.trim().equals("");
            if (!keyAlreadyHasNewLanguage && (emptyLine || (currentKey!=null && !line.startsWith(currentKey+":")))) {
                keyAlreadyHasNewLanguage = false;
                if(currentKey!=null) {
                    String newLanguageValue = (String)newLanguageEntries.get(currentKey);
                    if(newLanguageValue!=null) {
                        // Insert new language's entry in resulting file
                        pw.println(currentKey+":"+newLanguage+":"+newLanguageValue);
                        keyAlreadyHasNewLanguage = true;
                    }
                }
            }

            if(!emptyLine) {
                // Parse entry
                st = new StringTokenizer(line);

                // Set delimiter to ':'
                key = st.nextToken(":");
                lang = st.nextToken();

                if(!key.equals(currentKey)) {
                    currentKey = key;
                    keyAlreadyHasNewLanguage = false;
                }
		
                if(lang.equalsIgnoreCase(newLanguage))
                    keyAlreadyHasNewLanguage = true;
            }
			
            // Write current entry in resulting file
            pw.println(line);
        }

        newLanguageFileReader.close();
        originalFileReader.close();
        pw.close();
    }
}
