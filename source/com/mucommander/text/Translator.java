
package com.mucommander.text;

import com.mucommander.conf.ConfigurationManager;

import java.io.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Locale;


/**
 * This class takes care of all text localization issues by looking up text entries in a
 * dictionary file and translating them into the current language.
 *
 * <p>See dictionary file for more information about translation features.</p>
 *
 * @author Maxence Bernard
 */
public class Translator {
	private final static String DICTIONARY_RESOURCE_FILE = "/dictionary.txt";
	private final static Translator dico = new Translator(DICTIONARY_RESOURCE_FILE);

	private static Hashtable dictionaries;
	private static String dictionaryFilePath;
//	private static Vector orderedEntries;
//	private static boolean needsToBeSaved;

	/** Current language (2-letter language code). Uses default locale's language if language hasn't been set yet */
	private static String language;


	/**
	 * Creates a new Translator instance.
	 *
	 * @param filePath the path of the dictionnary file.
	 */
	private Translator(String filePath) {
		dictionaryFilePath = filePath;

		try {
			loadDictionnaryFile();
		} catch (IOException e) {
			new RuntimeException("Translator.init: unable to load dictionary file "+e);
		}
		
		String langVal = ConfigurationManager.getVariable("prefs.language");
		if(com.mucommander.Debug.ON) System.out.println("Language in prefs: "+langVal);

		// If language is not set in preferences 
		if(langVal==null) {
			// Try to set language to the system's language, if system's language
			// has a dictionary, otherwise English is used by default.
			String languages[] = getAvailableLanguages();
			String localeLang = Locale.getDefault().getLanguage();

			if(com.mucommander.Debug.ON) System.out.println("Language not set, trying to match system's language ("+localeLang+")");
			
			for(int i=0; i<languages.length; i++) {
				if(languages[i].equalsIgnoreCase(localeLang)) {
					Translator.language = localeLang;
					break;
				}
			}

			// Fall back to English (system's language doesn't have a dictionary)
			if(Translator.language==null) {
				Translator.language = "en";

				if(com.mucommander.Debug.ON) System.out.println("No dictionary matching "+localeLang+", falling back to English");
			}
			
			if(com.mucommander.Debug.ON) System.out.println("Language has been set to "+Translator.language);

				// Set language to configuration file
			ConfigurationManager.setVariable("prefs.language", Translator.language);
		}
		else {
			Translator.language = langVal;
		}

		if(com.mucommander.Debug.ON) System.out.println("Translator language: "+Translator.language);
	}


	/**
	 * Empty method that does nothing but trigger the static initializer block.
	 */
	public static void init() {
	}
	
	
	/**
	 * Sets language used by <code>get()</code> methods when language parameter isn't specified.
	 *
	 * @param lang 2-letter language code
	 */
	public static void setLanguage(String lang) {
		Translator.language = lang;
	}
	
	
	/**
	 * Returns language used by <code>get()</code> methods when language parameter isn't specified.
	 *
	 * @return lang 2-letter language code
	 */
	public static String getLanguage() {
		return language;
	}
	
	
	/**
	 * Returns an array of available languages, each described by a 2-letter
	 * String ("fr", "en", "jp"...).
	 *
	 * @return a String array of 2-letter language codes.
	 */
	public static String[] getAvailableLanguages() {
		String[] languages = new String[dictionaries.size()];
		Enumeration keys = dictionaries.keys();
		int i = 0;

		while (keys.hasMoreElements()) {
			languages[i++] = (String)keys.nextElement();
		}

		return languages;
	}


	/**
	 * Returns the dictionary for the given language, <code>null</code> if it
	 * does not exist.
	 *
	 * @param language DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Hashtable getDictionary(String language) {
		return (Hashtable)dictionaries.get(language);
	}


	/**
	 * Reloads dictionnary entries (reloads dictionnary file).
	 *
	 * @throws IOException if dictionary file could not be opened/read.
	 */
/*
	public static void reloadEntries() throws IOException {
		loadDictionnaryFile();
	}
*/
	

	/**
	 * Opens, reads and closes dictionnary file which contain localized text
	 * entries.
	 */
	public void loadDictionnaryFile() throws IOException {
		dictionaries = new Hashtable();

		BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(dictionaryFilePath), "UTF-8"));
		String line;
		String key;
		String lang;
		String text;
		StringTokenizer st;
		int nbEntries = 0;
//		orderedEntries = new Vector();

		while ((line = br.readLine())!=null) {
			if (!line.trim().startsWith("#") && !line.trim().equals("")) {
				st = new StringTokenizer(line);

				try {
					// Sets delimiter to ':'
					key = st.nextToken(":");
					lang = st.nextToken();

					// Delimiter is now line break (otherwise st.nextToken())
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

if(com.mucommander.Debug.ON && entryExists(key, lang)) System.out.println("Translator init: duplicate "+lang+" entry "+key);
					
					put(key, lang, text);
//					orderedEntries.add(key+":"+lang);
					nbEntries++;
				} catch (Exception e) {
if(com.mucommander.Debug.ON) e.printStackTrace();
					System.out.println("Translator init: error in line "+line+" ("+e+")");
				}
			} else {
//				orderedEntries.add(line);
			}
		}

		br.close();
//		needsToBeSaved = false;
	}

	/**
	 * Returns true if the given key exists (has a corresponding value) in the given language.
	 */
	public static boolean entryExists(String key, String lang) {
		Hashtable dictionary = (Hashtable)dictionaries.get(lang.toLowerCase());
		if(dictionary==null)
			return false;
		
		String entry = (String)dictionary.get(key.toLowerCase());
		return entry!=null; 
	}


	/**
	 * Adds the key/text value to the dictionary corresponding to the given
	 * language.
	 *
	 * @param key a case-insensitive key.
	 * @param language a 2-letter language case-insensitive string.
	 * @param text localized text.
	 */
	public static void put(String key, String language, String text) {
		// Gets the dictionary for this language
		Hashtable dictionary = (Hashtable)dictionaries.get(language.toLowerCase());

		// Dictionary for this language doesn't exist yet, let's create it
		if (dictionary==null) {
			dictionary = new Hashtable();
			dictionaries.put(language.toLowerCase(), dictionary);
		}

		// Adds a new entry to the dictionary
		dictionary.put(key.toLowerCase(), text);

//		needsToBeSaved = true;
	}


	/**
	 * Returns the localized text String corresponding to the given key and
	 * language in its 'raw' form, that is without parameters and variables
	 * replaced by their value.
	 *
	 * @param key a case-insensitive key.
	 * @param language a 2-letter language case-insensitive string.
	 *
	 * @return DOCUMENT ME!
	 */
/*
	 private static String getRawValue(String key, String language) {
		// Gets the dictionary for this language
		language = language.toLowerCase();

		Hashtable dictionary = (Hashtable)dictionaries.get(language);

		// Dictionary for this language doesn't exist 
		if (dictionary==null) {
			if (language.equals("en"))
				return "";
			else

				return getRawValue(key, "en");
		}

		// Returns the localized text
		String text = (String)dictionary.get(key.toLowerCase());

		if (text==null) {
			if (language.equals("en"))
				return "";
			else

				return getRawValue(key, "en");
		}

		return text;
	}
*/

	/**
	 * Returns the localized text String corresponding to the given key and
	 * language, and replaces  the %1, %2... parameters by their given value.
	 *
	 * @param key a case-insensitive key.
	 * @param language the language (2-letter code) in which the value will be returned.
	 * @param originalLanguage used for recursive calls to this method, to keep track of language originally requested.
	 * @param paramValues array of parameters which will be used to for variables in values.
	 */
	private static String get(String key, String language, String originalLanguage, String[] paramValues) {
		// Gets the dictionary for this language
		language = language.toLowerCase();

		Hashtable dictionary = (Hashtable)dictionaries.get(language);

		// Dictionary for this language doesn't exist 
		if (dictionary==null) {
			if (language.equals("en")) {
				if(com.mucommander.Debug.ON) System.out.println("Translator.get: Unknown key "+key);

				return key;
			} else

				return get(key, "en", originalLanguage, paramValues);
		}

		// Returns the localized text
		String text = (String)dictionary.get(key.toLowerCase());

		if (text==null) {
			if (language.equals("en")) {
				if(com.mucommander.Debug.ON) System.out.println("Translator.get: Unknown key "+key);

				return key;
			} else

				return get(key, "en", originalLanguage, paramValues);
		}


		// Replace %1, %2 ... parameters by their value
		if (paramValues!=null) {
			int pos = -1;
			for(int i=0; i<paramValues.length; i++) {
				while(++pos<text.length()-1 && (pos = text.indexOf("%"+(i+1), pos))!=-1)
					text = text.substring(0, pos)+paramValues[i]+text.substring(pos+2, text.length());
			}
		}

		// Replace $[] constants by their value
		int pos = 0;
		int pos2;
		String var;

		while ((pos = text.indexOf("$[", pos))!=-1) {
			pos2 = text.indexOf("]", pos+1);
			var = text.substring(pos+2, pos2);
			text = text.substring(0, pos)+get(var, originalLanguage, originalLanguage, paramValues)+text.substring(pos2+1, text.length());
		}

		return text;
	}


	/**
	 * Returns the localized text String corresponding to the given key and
	 * language, with the specified parameters (can be null).
	 * 
	 * @param key a case-insensitive key.
	 * @param language the language (2-letter code) in which the value will be returned.
	 * @param params array of parameters which will be used to for variables in values.
	 */
	public static String get(String key, String language, String params[]) {
		return get(key, language, language, params);
	}


	/**
	 * Returns the localized text String corresponding to the given key in the current language.
	 * 
	 * <p>
	 * Equivalent to <code>get(key, language, ((paramValues[])null)</code>.
	 * </p>
	 *
	 * @param key a case-insensitive key.
	 */
	public static String get(String key) {
		return get(key, language, language, (String[])null);
	}


	/**
	 * Convenience method, equivalent to <code>get(key, Translator.getLanguage(), new String[]{paramValue1})</code>)</code>.
	 *
	 * @param key a case-insensitive key.
	 * @param paramValue1 first parameter which will be used to replace %1 variables.
	 */
	public static String get(String key, String paramValue1) {
		return get(key, language, language, new String[] {paramValue1});
	}


	/**
	 * Convenience method, equivalent to <code>get(key, Translator.getLanguage(), new String[]{paramValue1, paramValue2})</code>)</code>.
	 *
	 * @param key a case-insensitive key.
	 * @param paramValue1 first parameter which will be used to replace %1 variables.
	 * @param paramValue2 second parameter which will be used to replace %2 variables.
	 */
	public static String get(String key, String paramValue1, String paramValue2) {
		return get(key, language, language, new String[] {paramValue1, paramValue2});
	}


	/**
	 * Convenience method, equivalent to <code>get(key, Translator.getLanguage(), new String[]{paramValue1, paramValue2, paramValue3})</code>)</code>.
	 *
	 * @param key a case-insensitive key.
	 * @param paramValue1 first parameter which will be used to replace %1 variables.
	 * @param paramValue2 second parameter which will be used to replace %2 variables.
	 * @param paramValue3 third parameter which will be used to replace %3 variables.
	 */
	public static String get(String key, String paramValue1, String paramValue2, String paramValue3) {
		return get(key, language, language, new String[] {
			    paramValue1, paramValue2, paramValue3
		    });
	}


	/**
	 * Looks for and reports any missing or unused dictionary entry,
	 * using the supplied source folder path to look inside source files
	 * for references to dictionary entries.
	 */
	public static void main(String args[]) throws IOException {
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


	/**
	 * Checks for missing dictionary entries in the given file or folder and reports them on the standard output.
	 * If the given file is a folder, recurse on each file that it contains, if it's a 'regular' file and the
	 * extension is '.java', looks for any calls to {@link #Translator.get(String), Translator.get()} and checks
	 * that the request entry has a value in each language's dictionary.
	 */ 
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



	/**
	 * Checks all enties in all dictionaries, checks that they are used in at least one source file
	 * in or under the supplied folder, and reports unused entries on the standard output.
	 */ 
	private static void checkUnusedEntries(com.mucommander.file.AbstractFile sourceFolder, String languages[]) throws IOException {
		Hashtable dictionary;
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

	/**
	 * Checks if the given entry is used in the supplied file or folder.
	 */
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


	/**
	 * Writes the dictionary file to the disk.
	 *
	 * @throws IOException DOCUMENT ME!
	 */
/*
	 public static void writeFileToDisk() throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(KContext.getValue("translation.file")), "utf-8"));

		String entry;
		String line;
		String rawValue;
		String rawValue2;
		int pos;
		int pos2;
		int nbEntries = orderedEntries.size();

		for (int i = 0; i<nbEntries; i++) {
			entry = ((String)orderedEntries.elementAt(i)).trim();

			if (entry.startsWith("#") || entry.equals("")) {
				line = entry;
			} else {
				pos = entry.indexOf(":");
				rawValue = getRawValue(entry.substring(0, pos), entry.substring(pos+1, pos+3));

				// Replace <eol> characters by \n
				pos2 = 0;
				rawValue2 = "";

				while ((pos = rawValue.indexOf(System.getProperty("line.separator"), pos2))!=-1) {
					rawValue2 += (rawValue.substring(pos2, pos)+"\\n");
					pos2 = pos+1;
				}

				rawValue2 += rawValue.substring(pos2, rawValue.length());

				line = entry+":"+rawValue2;
			}

			pw.println(line);
		}

		pw.close();
		needsToBeSaved = false;
	}
*/

	/**
	 * Returns <code>true</code> if some changes were made to the dictionary
	 * (thru the 'put' method) and were not saved to the dictionary file.<br>
	 * Calling writeFileToDisk() will reset the value to <code>false</code>
	 * until the next call to put().
	 *
	 * @return DOCUMENT ME!
	 */
/*
	 public static boolean needsToBeSaved() {
		return needsToBeSaved;
	}
*/
}
