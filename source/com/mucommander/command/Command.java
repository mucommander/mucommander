package com.mucommander.command;

import com.mucommander.file.AbstractFile;

import java.io.File;
import java.util.regex.Pattern;

/**
 * @author Nicolas Rinaudo
 */
public class Command {
    // - Replacement regexps ---------------------------------------------------
    // -------------------------------------------------------------------------
    /** $f keyword. */
    public static final Pattern REGEXP_PATH    = Pattern.compile("\\" + CommandParser.KEYWORD_HEADER + CommandParser.KEYWORD_PATH);
    /** $n keyword. */
    public static final Pattern REGEXP_NAME    = Pattern.compile("\\" + CommandParser.KEYWORD_HEADER + CommandParser.KEYWORD_NAME);
    /** $p keyword. */
    public static final Pattern REGEXP_PARENT  = Pattern.compile("\\" + CommandParser.KEYWORD_HEADER + CommandParser.KEYWORD_PARENT);
    /** $j keyword. */
    public static final Pattern REGEXP_VM_PATH = Pattern.compile("\\" + CommandParser.KEYWORD_HEADER + CommandParser.KEYWORD_VM_PATH);



    // - Instance variables ----------------------------------------------------
    // -------------------------------------------------------------------------
    /** Different tokens that compose the command. */
    private String[] tokens;
    /** Information about potential keywords contained in the tokens. */
    private int[]    tokenTypes;
    /** Command's alias. */
    private String   alias;



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Creates a new command.
     * @param alias      alias of the command.
     * @param tokens     tokens that compose the command.
     * @param tokenTypes description of each token in <code>tokens</code>
     */
    Command(String alias, String[] tokens, int[] tokenTypes) {
        this.tokens     = tokens;
        this.tokenTypes = tokenTypes;
        this.alias      = alias;
    }



    // - Token retrieval -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the command's token, replacing any keyword by the values from <code>target</code>.
     * @param  target file from which to get the values by which to replace keywords.
     * @return        the command's tokens.
     */
    public String[] getTokens(AbstractFile target) {
        String[] newTokens; // Buffer for the final tokens.
        String   buffer;    // Current token.

        newTokens = new String[tokens.length];

        // Performs keyword replacement.
        for(int i = 0; i < tokens.length; i++) {
            buffer = tokens[i];

            // Checks whether the current token contains $f
            if((tokenTypes[i] & CommandParser.TYPE_PATH) == CommandParser.TYPE_PATH)
                REGEXP_PATH.matcher(buffer).replaceAll(target.getAbsolutePath());

            // Checks whether the current token contains $n
            if((tokenTypes[i] & CommandParser.TYPE_NAME) == CommandParser.TYPE_NAME)
                REGEXP_NAME.matcher(buffer).replaceAll(target.getName());

            // Checks whether the current token contains $p
            if((tokenTypes[i] & CommandParser.TYPE_PARENT) == CommandParser.TYPE_PARENT)
                REGEXP_PARENT.matcher(buffer).replaceAll(target.getParent().getAbsolutePath());

            // Checks whether the current token contains $j
            if((tokenTypes[i] & CommandParser.TYPE_VM_PATH) == CommandParser.TYPE_VM_PATH)
                REGEXP_VM_PATH.matcher(buffer).replaceAll(new File(System.getProperty("user.dir")).getAbsolutePath());

            newTokens[i] = buffer;
        }

        return newTokens;
    }
}
