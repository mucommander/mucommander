
package com.mucommander.text;

import java.io.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Locale;


/**
 * This class takes care of all text localization issues by looking up in a
 * dictionary localized text entries.
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

	private static String language = Locale.getDefault().getLanguage();


	/**
	 * Creates a new Translator instance.
	 *
	 * @param filePath the path of the dictionnary file.
	 */
	private Translator(String filePath) {
		dictionaryFilePath = filePath;
//		Locale locale = getDefault();
//		System.out.println(locale.getLanguage()+" ("+locale.getDisplayLanguage()+")"+" / "+locale.getCountry()+" ("+locale.getDisplayCountry()+")"+" / "+locale.getVariant()+"("+locale.getDisplayVariant());

		try {
			loadDictionnaryFile();
		} catch (IOException e) {
			new RuntimeException("Translator.init: unable to load dictionary file "+e);
		}
	}

	/**
	 * Returns an array of available languages, each described by a 2-letter
	 * String ("fr", "en", "jp"...).
	 *
	 * @return DOCUMENT ME!
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
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public static void loadDictionnaryFile() throws IOException {
		dictionaries = new Hashtable();

		BufferedReader br = new BufferedReader(new InputStreamReader(dictionaries.getClass().getResourceAsStream(dictionaryFilePath), "UTF-8"));
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

					while ((pos = text.indexOf("\\n", pos))!=-1) {
						text = text.substring(0, pos)+"\n"+text.substring(pos+2, text.length());
					}

					// Replace "\\uxxxx" unicode charcter strings by the designated character
					pos = 0;

					while ((pos = text.indexOf("\\u", pos))!=-1) {
						text = text.substring(0, pos)+(char)(Integer.parseInt(text.substring(pos+2, pos+6), 16))+text.substring(pos+6, text.length());
					}

					put(key, lang, text);
//					orderedEntries.add(key+":"+lang);
					nbEntries++;
				} catch (Exception e) {
//					LogManager.log("Translator init: Error in line "+line+" ("+e+")");
					System.out.println("Translator init: error in line "+line+" ("+e+")");
				}
			} else {
//				orderedEntries.add(line);
			}
		}

		br.close();
//		needsToBeSaved = false;

//		LogManager.log("Translator init: "+nbEntries+" entries loaded");
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
	 * @param language a 2-letter language case-insensitive string.
	 * @param paramValues an array containing the parameters' value.
	 *
	 * @return DOCUMENT ME!
	 */
	public static String get(String key, String language, String[] paramValues) {
		// Gets the dictionary for this language
		language = language.toLowerCase();

		Hashtable dictionary = (Hashtable)dictionaries.get(language);

		// Dictionary for this language doesn't exist 
		if (dictionary==null) {
			if (language.equals("en")) {
//				LogManager.logError("Translator.get: Unknown key "+key, true);
				System.out.println("Translator.get: Unknown key "+key);

				return key;
			} else

				return get(key, "en", paramValues);
		}

		// Returns the localized text
		String text = (String)dictionary.get(key.toLowerCase());

		if (text==null) {
			if (language.equals("en")) {
//				LogManager.logError("Translator.get: Unknown key "+key, true);
				System.out.println("Translator.get: Unknown key "+key);

				return key;
			} else

				return get(key, "en", paramValues);
		}

		// Replace %1, %2 ... parameters by their value
		if (paramValues!=null) {
			int i;
			int pos = 0;

			while ((pos = text.indexOf('%', pos))!=-1) {
				i = Integer.parseInt(""+text.charAt(pos+1))-1;

				if (i<paramValues.length)
					text = text.substring(0, pos)+paramValues[i]+text.substring(pos+2, text.length());

				i++;
			}
		}

		// Replace $[] constants by their value
		int pos = 0;
		int pos2;
		String var;

		while ((pos = text.indexOf("$[", pos))!=-1) {
			pos2 = text.indexOf("]", pos+1);
			var = text.substring(pos+2, pos2);
			text = text.substring(0, pos)+get(var, language)+text.substring(pos2+1, text.length());
		}

		return text;
	}


	/**
	 * Returns the localized text String corresponding to the given key and
	 * language.
	 * 
	 * <p>
	 * Equivalent to <code>get(key, language, ((paramValues[])null)</code>.
	 * </p>
	 *
	 * @param key a case-insensitive key.
	 *
	 * @return DOCUMENT ME!
	 */
	public static String get(String key) {
		return get(key, language, (String[])null);
	}


	/**
	 * <p>
	 * Equivalent to <code>get(key, language, new
	 * String[]{paramValue1})</code>.
	 * </p>
	 *
	 * @param key DOCUMENT ME!
	 * @param paramValue1 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static String get(String key, String paramValue1) {
		return get(key, language, new String[] {paramValue1});
	}


	/**
	 * <p>
	 * Equivalent to <code>get(key, language, new String[]{paramValue1,
	 * paramValue2})</code>.
	 * </p>
	 *
	 * @param key DOCUMENT ME!
	 * @param paramValue1 DOCUMENT ME!
	 * @param paramValue2 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static String get(String key, String paramValue1, String paramValue2) {
		return get(key, language, new String[] {paramValue1, paramValue2});
	}


	/**
	 * <p>
	 * Equivalent to <code>get(key, language, new String[]{paramValue1,
	 * paramValue2, paramValue3})</code>.
	 * </p>
	 *
	 * @param key DOCUMENT ME!
	 * @param paramValue1 DOCUMENT ME!
	 * @param paramValue2 DOCUMENT ME!
	 * @param paramValue3 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static String get(String key, String paramValue1, String paramValue2, String paramValue3) {
		return get(key, language, new String[] {
			    paramValue1, paramValue2, paramValue3
		    });
	}


	/**
	 * Returns the String encoding String (confusing uh?) for the given
	 * language, e.g. "SJIS" for "JP"
	 *
	 * @param lang DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static String getEncoding(String lang) {
		if (lang.toLowerCase().equals("jp")) {
			return "SJIS";
		} else

			return null;
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
