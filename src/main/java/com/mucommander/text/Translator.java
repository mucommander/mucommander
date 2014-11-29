/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.text;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.util.ResourceLoader;
import com.mucommander.commons.io.bom.BOMReader;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;


/**
 * This class takes care of all text localization issues by loading all text entries from a dictionary file on startup
 * and translating them into the current language on demand.
 *
 * <p>All public methods are static to make it easy to call them throughout the application.</p>
 *
 * <p>See dictionary file for more information about th dictionary file format.</p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class Translator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Translator.class);
	
    /** Contains key/value pairs for the current language */
    private static Map<String, String> dictionary;

    /** Contains key/value pairs for the default language, for entries that are not defined in the current language */
    private static Map<String, String> defaultDictionary;

    /** List of all available languages in the dictionary file */
    private static List<Locale> availableLanguages = new ArrayList<Locale>();

    /** Current language */
    private static Locale language;

    /** Key for available languages */
    private final static String AVAILABLE_LANGUAGES_KEY = "available_languages";

    /**
     * Prevents instance creation.
     */
    private Translator() {}

    static {
    	registerLocale(Locale.forLanguageTag("ar-SA"));
    	registerLocale(Locale.forLanguageTag("be-BY"));
    	registerLocale(Locale.forLanguageTag("ca-ES"));
    	registerLocale(Locale.forLanguageTag("cs-CZ"));
    	registerLocale(Locale.forLanguageTag("da-DA"));
    	registerLocale(Locale.forLanguageTag("de-DE"));
    	registerLocale(Locale.forLanguageTag("en-GB"));
    	registerLocale(Locale.forLanguageTag("en-US"));
    	registerLocale(Locale.forLanguageTag("es-ES"));
    	registerLocale(Locale.forLanguageTag("fr-FR"));
    	registerLocale(Locale.forLanguageTag("hu-HU"));
    	registerLocale(Locale.forLanguageTag("it-IT"));
    	registerLocale(Locale.forLanguageTag("ja-JP"));
    	registerLocale(Locale.forLanguageTag("ko-KR"));
    	registerLocale(Locale.forLanguageTag("no-NO"));
    	registerLocale(Locale.forLanguageTag("nl-NL"));
    	registerLocale(Locale.forLanguageTag("pl-PL"));
    	registerLocale(Locale.forLanguageTag("pt-BR"));
    	registerLocale(Locale.forLanguageTag("ro-RO"));
    	registerLocale(Locale.forLanguageTag("ru-RU"));
    	registerLocale(Locale.forLanguageTag("sk-SK"));
    	registerLocale(Locale.forLanguageTag("sl-SL"));
    	registerLocale(Locale.forLanguageTag("sv-SV"));
    	registerLocale(Locale.forLanguageTag("tr-TR"));
    	registerLocale(Locale.forLanguageTag("uk-UA"));
    	registerLocale(Locale.forLanguageTag("zh-CN"));
    	registerLocale(Locale.forLanguageTag("zh-TW"));
    }

    public static void registerLocale(Locale locale) {
    	availableLanguages.add(locale);
    }

    /**
     * Determines and sets the current language based on the given list of available languages
     * and the current language set in the preferences if it has been set, else on the system's language.
     * <p>
     * If the language set in the preferences or the system's language is not available, the default language as
     * defined by {@link #DEFAULT_LANGUAGE} will be used.
     * </p>
     *
     * @param availableLanguages list of available languages
     */
    private static void setCurrentLanguage() {
        Locale locale = getLocale();
            
        // Determines if language is one of the languages declared as available
        if(availableLanguages.contains(locale)) {
            // Language is available
            Translator.language = locale;
            LOGGER.debug("Language "+Translator.language+" is available.");
        }
        else {
            // Language is not available, fall back to default language
        	Translator.language = Locale.getDefault();
            LOGGER.debug("Language "+Translator.language+" is not available, falling back to English");
        }
		
        // Set preferred language in configuration file
        MuConfigurations.getPreferences().setVariable(MuPreference.LANGUAGE, Translator.language.toLanguageTag());

        LOGGER.debug("Current language has been set to "+Translator.language);
    }

    private static Locale getLocale() {
    	String localeNameFromConf = MuConfigurations.getPreferences().getVariable(MuPreference.LANGUAGE);
    	if (localeNameFromConf == null) {
    		// language is not set in preferences, use system's language
            // Try to match language with the system's language, only if the system's language
            // has values in dictionary, otherwise use default language (English).
            Locale defaultLocale = Locale.getDefault();
            LOGGER.info("Language not set in preferences, trying to match system's language ("+defaultLocale+")");
            return defaultLocale;
    	}

    	LOGGER.info("Using language set in preferences: "+localeNameFromConf);
    	switch (localeNameFromConf) {
    	case "EN": return Locale.forLanguageTag("en-US");
    	case "en_GB": return Locale.forLanguageTag("en_GB");
    	case "FR": return Locale.forLanguageTag("fr-FR");
    	case "DE": return Locale.forLanguageTag("de-DE");
    	case "ES": return Locale.forLanguageTag("es-ES");
    	case "CS": return Locale.forLanguageTag("cs-CZ");
    	case "zh_CN": return Locale.forLanguageTag("zh-CN");
    	case "zh_TW": return Locale.forLanguageTag("zh-TW");
    	case "PL": return Locale.forLanguageTag("pl-PL");
    	case "HU": return Locale.forLanguageTag("hu-HU");
    	case "RU": return Locale.forLanguageTag("ru-RU");
    	case "SL": return Locale.forLanguageTag("sl-SL");
    	case "RO": return Locale.forLanguageTag("ro-RO");
    	case "IT": return Locale.forLanguageTag("it-IT");
    	case "KO": return Locale.forLanguageTag("ko-KR");
    	case "pt_BR": return Locale.forLanguageTag("pt-BR");
    	case "NL": return Locale.forLanguageTag("nl-NL");
    	case "SK": return Locale.forLanguageTag("sk-SK");
    	case "JA": return Locale.forLanguageTag("ja-JP");
    	case "SV": return Locale.forLanguageTag("sv-SV");
    	case "DA": return Locale.forLanguageTag("da-DA");
    	case "UA": return Locale.forLanguageTag("uk-UA");
    	case "AR": return Locale.forLanguageTag("ar-SA");
    	case "BE": return Locale.forLanguageTag("be-BY");
    	case "NB": return Locale.forLanguageTag("no-NO");
    	case "TR": return Locale.forLanguageTag("tr-TR");
    	case "CA": return Locale.forLanguageTag("ca-ES");
    	default: return Locale.forLanguageTag(localeNameFromConf);
    	}
    }

    public static void loadDictionaryFile() throws IOException {
        loadDictionaryFile(com.mucommander.RuntimeConstants.DICTIONARY_FILE);
    }

    /**
     * Loads the specified dictionary file, which contains localized text entries.
     *
     * @param filePath path to the dictionary file
     * @throws IOException thrown if an IO error occurs.
     */
    public static void loadDictionaryFile(String filePath) throws IOException {
        dictionary         = new Hashtable<String, String>();
        defaultDictionary  = new Hashtable<String, String>();

        // Determines current language based on available languages and preferred language (if set) or system's language
        setCurrentLanguage();

        BufferedReader br = new BufferedReader(new BOMReader(ResourceLoader.getResourceAsStream(filePath)));
        String line;
        String keyLC;
        String lang;
        String text;
        StringTokenizer st;

        while((line = br.readLine())!=null) {
            if (!line.trim().startsWith("#") && !line.trim().equals("")) {
                st = new StringTokenizer(line);

                try {
                    // Sets delimiter to ':'
                    keyLC = st.nextToken(":").trim().toLowerCase();

                    // Special key that lists available languages, must
                    // be defined before any other entry
                    if(keyLC.equals(AVAILABLE_LANGUAGES_KEY)) {
                    	// Parse comma separated languages
                    	st = new StringTokenizer(st.nextToken(), ",\n");
                    	while(st.hasMoreTokens())
                    		st.nextToken();

                    	continue;
                    }

                    lang = st.nextToken().trim();

                    // Delimiter is now line break
                    text = st.nextToken("\n");
                    text = text.substring(1, text.length());

                    // Replace "\n" strings in the text by \n characters
                    int pos = 0;

                    while ((pos = text.indexOf("\\n", pos))!=-1)
                        text = text.substring(0, pos)+"\n"+text.substring(pos+2, text.length());

                    // Replace "\\uxxxx" unicode strings by the designated character
                    pos = 0;

                    while ((pos = text.indexOf("\\u", pos))!=-1)
                        text = text.substring(0, pos)+(char)(Integer.parseInt(text.substring(pos+2, pos+6), 16))+text.substring(pos+6, text.length());

                    // Add entry for current language, or for default language if a value for current language wasn't already set
                    if(lang.equalsIgnoreCase(language.getLanguage())) {
                        dictionary.put(keyLC, text);
                        // Remove the default dictionary entry as it will not be used (saves some memory).
                        defaultDictionary.remove(keyLC);
                    }
                }
                catch(Exception e) {
                    LOGGER.info("error in line " + line + " (" + e + ")");
                    throw new IOException("Syntax error in line " + line);
                }
            }
        }
        br.close();
    }

    /**
     * Returns the current language as a language code ("EN", "FR", "pt_BR", ...).
     *
     * @return lang a language code
     */
    public static String getLanguage() {
        return language.getLanguage();
    }
	
	
    /**
     * Returns an array of available languages, expressed as language codes ("EN", "FR", "pt_BR"...).
     * The returned array is sorted by language codes in case insensitive order.
     *
     * @return an array of language codes.
     */
    public static List<Locale> getAvailableLanguages() {
    	return availableLanguages;
    }


    /**
     * Returns <code>true</code> if the given entry's key has a value in the current language.
     * If the <code>useDefaultLanguage</code> parameter is <code>true</code>, entries that have no value in the 
     * {@link #getLanguage() current language} but one in the {@link #DEFAULT_LANGUAGE} will be considered as having
     * a value (<code>true</code> will be returned).
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param useDefaultLanguage if <code>true</code>, entries that have no value in the {@link #getLanguage() current
     * language} but one in the {@link #DEFAULT_LANGUAGE} will be considered as having a value
     * @return <code>true</code> if the given key has a corresponding value in the current language.
     */
    public static boolean hasValue(String key, boolean useDefaultLanguage) {
        return dictionary.get(key.toLowerCase())!=null
                || (useDefaultLanguage && defaultDictionary.get(key.toLowerCase())!=null);
    }

    /**
     * Returns the localized text String for the given key expressd in the current language, or in the default language
     * if there is no value for the current language. Entry parameters (%1, %2, ...), if any, are replaced by the
     * specified values.
     *
     * @param key key of the requested dictionary entry (case-insensitive)
     * @param paramValues array of parameters which will be used as values for variables.
     * @return the localized text String for the given key expressd in the current language
     */
    public static String get(String key, String... paramValues) {
        // Returns the localized text
        String text = dictionary.get(key.toLowerCase());

        if (text==null) {
            text = defaultDictionary.get(key.toLowerCase());

            if(text==null) {
            	LOGGER.debug("No value for "+key+", returning key");
                return key;
            }
            else {
            	LOGGER.debug("No value for "+key+" in language "+language+", using English value");
                // Don't return yet, parameters need to be replaced
            }
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
			
        com.mucommander.commons.file.AbstractFile sourceFolder = com.mucommander.commons.file.AbstractFile.getFile(args[0]);
		
        System.out.println("\n##### Looking for missing entries #####");
        checkMissingEntries(sourceFolder, langs);

        System.out.println("\n##### Looking for unused entries #####");
        checkUnusedEntries(sourceFolder, langs);
        }
        // Integrates a new language into the dictionary
        else {
        */
        // Parameters order: originalFile newLanguageFile resultingFile newLanguage
        if(args.length<4) {
            System.out.println("usage: Translator originalFile newLanguageFile mergedFile newLanguage");
            return;
        }

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
      private static void checkMissingEntries(com.mucommander.commons.file.AbstractFile file, String languages[]) throws IOException {
      if(file.isDirectory()) {
      com.mucommander.commons.file.AbstractFile children[] = file.ls();
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
      private static void checkUnusedEntries(com.mucommander.commons.file.AbstractFile sourceFolder, String languages[]) throws IOException {
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
      private static boolean isEntryUsed(String entry, com.mucommander.commons.file.AbstractFile file) throws IOException {
      if(file.isDirectory()) {
      com.mucommander.commons.file.AbstractFile children[] = file.ls();
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
     * @throws IOException if an I/O error occurred
     */
    private static void addLanguageToDictionary(String originalFile, String newLanguageFile, String resultingFile, String newLanguage) throws IOException {
        // Initialize streams
        BufferedReader originalFileReader = new BufferedReader(new BOMReader(new FileInputStream(originalFile)));
        BufferedReader newLanguageFileReader = new BufferedReader(new BOMReader(new FileInputStream(newLanguageFile)));
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultingFile), "UTF-8"));

        // Parse new language's entries
        String line;
        int lineNum = 0;
        String key;
        String lang;
        String text;
        StringTokenizer st;
        Map<String, String> newLanguageEntries = new Hashtable<String, String>();
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
            	LOGGER.warn("caught "+e+" at line "+lineNum);
                return;
            }
        }

        // Insert new language entries in resulting file
        boolean keyProcessedForNewLanguage = false;
        String currentKey = null;
        while ((line = originalFileReader.readLine())!=null) {
            boolean emptyLine = line.trim().startsWith("#") || line.trim().equals("");
            if (!keyProcessedForNewLanguage && (emptyLine || (currentKey!=null && !line.startsWith(currentKey+":")))) {
                if(currentKey!=null) {
                    String newLanguageValue = newLanguageEntries.get(currentKey);
                    if(newLanguageValue!=null) {
                        // Insert new language's entry in resulting file
                    	LOGGER.info("New language entry for key="+currentKey+" value="+newLanguageValue);
                        pw.println(currentKey+":"+newLanguage+":"+newLanguageValue);
                    }

                    keyProcessedForNewLanguage = true;
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
                    keyProcessedForNewLanguage = false;
                }

                if(lang.equalsIgnoreCase(newLanguage)) {
                    // Delimiter is now line break
                    String existingNewLanguageValue = st.nextToken("\n");
                    existingNewLanguageValue = existingNewLanguageValue.substring(1, existingNewLanguageValue.length());
                    String newLanguageValue = newLanguageEntries.get(currentKey);

                    if(newLanguageValue!=null) {
                        if(!existingNewLanguageValue.equals(newLanguageValue))
                        	LOGGER.warn("Warning: found an updated value for key="+currentKey+", using new value="+newLanguageValue+" existing value="+existingNewLanguageValue);

                        pw.println(currentKey+":"+newLanguage+":"+newLanguageValue);
                    }
                    else {
                    	LOGGER.warn("Existing dictionary has a value for key="+currentKey+" that is missing in the new dictionary file, using existing value= "+existingNewLanguageValue);
                        pw.println(currentKey+":"+newLanguage+":"+existingNewLanguageValue);
                    }

                    keyProcessedForNewLanguage = true;
                }
                else {
                    pw.println(line);
                }
            }
            else {
                pw.println(line);
            }
        }

        newLanguageFileReader.close();
        originalFileReader.close();
        pw.close();
    }
}
