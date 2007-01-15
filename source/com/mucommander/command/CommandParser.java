package com.mucommander.command;

/**
 * Used to parse system commands.
 * <p>
 * muCommander needs to execute system commands in many different cases - shell,
 * file opening, default browser starting...<br/>
 * Such commands need to be split into tokens before being executed.
 * <code>CommandParser</code> offers a uniformised way of doing that.
 * </p>
 * @author Nicolas Rinaudo
 */
public class CommandParser {
    // - Token types -----------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Nothing special about the token. */
    static final int TYPE_NORMAL      = 0;
    /** The token contains a reference to {@link #TOKEN_PATH}. */
    static final int TYPE_PATH        = 1;
    /** The token contains a reference to {@link #TOKEN_NAME}. */
    static final int TYPE_NAME        = 2;
    /** The token contains a reference to {@link #TOKEN_PARENT}. */
    static final int TYPE_PARENT      = 4;
    /** The token contains a reference to {@link #TOKEN_VM_PATH}. */
    static final int TYPE_VM_PATH     = 8;



    // - Special tokens --------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Header of replacement keywords. */
    static final char KEYWORD_HEADER  = '$';
    /** Instances of this keyword will be replaced by the file's full path. */
    static final char KEYWORD_PATH    = 'f';
    /** Instances of this keyword will be replaced by the file's name. */
    static final char KEYWORD_NAME    = 'n';
    /** Instances of this keyword will be replaced by the file's parent directory. */
    static final char KEYWORD_PARENT  = 'p';
    /** Instances of this keyword will be replaced by the JVM's current directory. */
    static final char KEYWORD_VM_PATH = 'j';



    // - Initialisation --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Prevents instances of the parser from being created.
     */
    private CommandParser() {}



    // - Token retrieval -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Returns the tokens that compose the specified command.
     * @param  command command to tokenize.
     * @return         the tokens that compose the command.
     */
    public static String[] getTokens(String command) {
        String[] tokens; // Stores the tokens that compose the command.
        String[] buffer; // Buffer for the parse method

        // Parses the command and initialises token to the right size.
        tokens = new String[parse(command, buffer = new String[command.length()], null)];

        // Copies the parsing output into tokens.
        System.arraycopy(buffer, 0, tokens, 0, tokens.length);

        return tokens;
    }

    /**
     * Returns the tokens that compose the specified command.
     * <p>
     * In some cases, the command that needs to be tokenized is not complete (such as,
     * for example, with the {@link com.mucommander.shell.Shell shell} command). This
     * method will allocate <code>paramLength</code> free slot at the end of the token
     * array to accomodate for such cases.
     * </p>
     * @param  command     command to tokenize.
     * @param  paramLength number of free slot that should be made available in the token array.
     * @return             the tokens that compose the command.
     */
    public static String[] getTokensWithParams(String command, int paramLength) {
        String[] tokens; // Stores the tokens that compose the command.
        String[] buffer; // Buffer for the parse method

        // Parses the command and initialises token to the right size.
        tokens = new String[parse(command, buffer = new String[command.length()], null) + paramLength];

        // Copies the parsing output into tokens.
        System.arraycopy(buffer, 0, tokens, 0, tokens.length - paramLength);

        return tokens;
    }

    /**
     * Builds an instance of {@link com.mucommander.command.Command} from the specified alias and command.
     * @param  alias   alias for the new command.
     * @param  command what to execute when the Command is being called.
     * @return an instance of Command.
     */
    static Command getCommand(String alias, String command) {
        String[] tokenBuffer; // Buffer for the tokens that compose command.
        int[]    typeBuffer;  // Buffer for the type of tokens that compose command.
        String[] tokens;      // Actual tokens.
        int[]    tokenTypes;  // Actual types.

        // Parses the command and grabs the proper buffer sizes.
        tokens      = new String[parse(command, tokenBuffer = new String[command.length()], typeBuffer = new int[command.length()])];
        tokenTypes  = new int[tokens.length];

        // Stores the tokens and token types.
        for(int i = 0; i < tokens.length; i++) {
            tokens[i]      = tokenBuffer[i];
            tokenTypes[i]  = typeBuffer[i];
        }

        // Creates and returns a new command.
        return new Command(alias, tokens, tokenTypes);
    }



    // - Command parsing -------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Parses the specified command and stores the result in <code>tokens</code>.
     * <p>
     * The <code>tokenTypes</code> argument is optional, and can be set to <code>null</code>.
     * If not set to null, it will contain instructions about whether a keyword is declared in
     * the token of the same index.<br/>
     * For example, if <code>tokenTypes[i] & TYPE_PATH == TYPE_PATH</code>, then
     * <code>tokens[i]</code> contains the $p keyword.
     * </p>
     * <p>
     * This method is very lax about syntax. Errors are just ignored, and a best effort will be
     * made to make sense of the rest of the command.
     * </p>
     * <p>
     * When not sure about how many tokens compose the command - which should be pretty much
     * all the time, it is advised to allocate <code>command.length()</code> big arrays.
     * While not very memory efficient, this is the only way to ensure success. This is an implementation
     * design: growing the size of the buffer to fit the actual number of tokens is resource consuming,
     * both in terms of CPU and memory. The 'use the maximum amount of memory that makes sense' is the
     * lesser of two evils.
     * </p>
     * @param  command    command to parse.
     * @param  tokens     where to store the tokens that compose the command.
     * @param  tokenTypes optional array in which to store information about potential keywords.
     * @return            the number of tokens that were found.
     */
    private static final int parse(String command, String[] tokens, int[] tokenTypes) {
        char[]       buffer;        // All the characters that compose command.
        int          tokenIndex;    // Index of the current token in tokens[];
        StringBuffer currentToken;  // Buffer for the current token.
        boolean      isInQuotes;    // Whether we're currently within quotes or not.

        // Initialises parsing.
        command      = command.trim();
        currentToken = new StringBuffer(command.length());
        tokenIndex   = 0;
        buffer       = command.toCharArray();
        isInQuotes   = false;

        // Parses the command.
        for(int i = 0; i < command.length(); i++) {
            // Quote escaping: toggle isInQuotes.
            if(buffer[i] == '\"')
                isInQuotes = !isInQuotes;

            // Backslash escaping: the next character is not analyzed.
            else if(buffer[i] == '\\') {
                if(i + 1 != command.length())
                    currentToken.append(buffer[++i]);
            }

            // Whitespace: end of token if we're not between quotes.
            else if(buffer[i] == ' ' && !isInQuotes) {
                // Skips un-escaped blocks of spaces.
                while(i + 1 < command.length() && buffer[i + 1] == ' ')
                    i++;

                // Stores the current token.
                tokens[tokenIndex] = currentToken.toString();
                tokenIndex++;

                // Resets the token buffer.
                currentToken.setLength(0);
            }

            // Keyword, gather token type information if necessary.
            else if(buffer[i] == KEYWORD_HEADER) {
                // String ends too soon. Let's pretend nothing happened.
                // We *could* raise an exception here, but it would make using this class very awkward.
                if(++i == command.length())
                    break;

                // We only analyse keywords if tokenTypes is not null - ie if keywords actually matter.
                if(tokenTypes != null) {
                    // Full path replacement.
                    if(buffer[i] == KEYWORD_PATH)
                        tokenTypes[tokenIndex] |= TYPE_PATH;

                    // File name replacement.
                    else if(buffer[i] == KEYWORD_NAME)
                        tokenTypes[tokenIndex] |= TYPE_NAME;

                    // Parent path replacement.
                    else if(buffer[i] == KEYWORD_PARENT)
                        tokenTypes[tokenIndex] |= TYPE_PARENT;

                    // VM path replacement.
                    else if(buffer[i] == KEYWORD_VM_PATH)
                        tokenTypes[tokenIndex] |= TYPE_VM_PATH;
                }

                // Appends the keyword to the current token.
                currentToken.append(KEYWORD_HEADER);
                currentToken.append(buffer[i]);
            }

            // Nothing special about this character.
            else
                currentToken.append(buffer[i]);
        }

        // Adds a possible last token.
        if(currentToken.length() != 0) {
            tokens[tokenIndex] = currentToken.toString();
            return tokenIndex + 1;
        }

        return tokenIndex;
    }
}
