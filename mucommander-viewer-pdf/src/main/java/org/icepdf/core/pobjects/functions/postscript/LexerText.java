/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.functions.postscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple type 4 function tests.
 *
 * @author ICEsoft Technologies Inc.
 * @since 4.2
 */
public class LexerText {

    public static final String TEST_1 =
            "{1.000000 3 1 roll 1.000000 3 1 roll 1.000000 3 1 roll 5 -1 roll \n" +
                    "2 index -0.874500 mul 1.000000 add mul 1 index -0.098000 mul 1.000000 add mul 5 \n" +
                    "1 roll 4 -1 roll 2 index -0.796100 mul 1.000000 add mul 1 index -0.247100 \n" +
                    "mul 1.000000 add mul 4 1 roll 3 -1 roll 2 index -0.647100 mul 1.000000 \n" +
                    "add mul 1 index -0.878400 mul 1.000000 add mul 3 1 roll pop pop }";

    public static final String TEST_2 =
            "{1.000000 2 1 roll 1.000000 2 1 roll 1.000000 2 1 roll 0 index 1.000000 \n" +
                    "cvr exch sub 2 1 roll 5 -1 roll 1.000000 cvr exch sub 5 1 \n" +
                    "roll 4 -1 roll 1.000000 cvr exch sub 4 1 roll 3 -1 roll 1.000000 \n" +
                    "cvr exch sub 3 1 roll 2 -1 roll 1.000000 cvr exch sub 2 1 \n" +
                    "roll pop }";

    public static final String TEST_3 =
            "{0 0 0 0 5 4 roll 0 index 3 -1 roll add 2 1 roll pop dup 1 gt {pop 1} if " +
                    "4 1 roll dup 1 gt {pop 1} if 4 1 roll dup 1 gt {pop 1} if 4 1 roll dup 1 gt {pop 1} if 4 1 roll}";

    public static final String TEST_4 =
            "{dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 0 index 1 exp 1 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 1 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 2 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 3 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 5 4 roll pop }";

    public static void main(String[] args) {

        try {
//           new LexerText().test5();
            new LexerText().test8();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test1() throws IOException {
        String test =
                "{1.000000 3 1 roll 1.000000 3 1 roll 1.000000 3 1 roll 5 -1 roll \n" +
                        "2 index -0.874500 mul 1.000000 add mul 1 index -0.098000 mul 1.000000 add mul 5 \n" +
                        "1 roll 4 -1 roll 2 index -0.796100 mul 1.000000 add mul 1 index -0.247100 \n" +
                        "mul 1.000000 add mul 4 1 roll 3 -1 roll 2 index -0.647100 mul 1.000000 \n" +
                        "add mul 1 index -0.878400 mul 1.000000 add mul 3 1 roll pop pop }";
        ;

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{1.0f, 1.0f});

        System.out.println("result: " + lex.getStack().toString());
    }

    public void test2() throws IOException {
        String test =
                "{2 index 1.000000 cvr exch sub 4 1 roll 1 index 1.000000 cvr exch sub \n" +
                        "4 1 roll 0 index 1.000000 cvr exch sub 4 1 roll 1.000000 4 1 \n" +
                        "roll 7 -1 roll 1.000000 cvr exch sub 7 1 roll 6 -1 roll 1.000000 \n" +
                        "cvr exch sub 6 1 roll 5 -1 roll 1.000000 cvr exch sub 5 1 \n" +
                        "roll 4 -1 roll 1.000000 cvr exch sub 4 1 roll pop pop pop }";

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{0.360779f, 0.094238274f, 0.00392151f});

        System.out.println("result: " + lex.getStack().toString());
    }

    // ficha--3--para+impresion.pdf page 1 - function CORRECT
    public void test5() throws IOException {
        String test =
                "{2 index 1.000000 cvr exch sub 4 1 roll 1 index 1.000000 cvr exch sub \n" +
                        "4 1 roll 0 index 1.000000 cvr exch sub 4 1 roll 1.000000 4 1 \n" +
                        "roll 7 -1 roll 1.000000 cvr exch sub 7 1 roll 6 -1 roll 1.000000 \n" +
                        "cvr exch sub 6 1 roll 5 -1 roll 1.000000 cvr exch sub 5 1 \n" +
                        "roll 4 -1 roll 1.000000 cvr exch sub 4 1 roll pop pop pop }";

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{0.360779f, 0.094238274f, 0.00392151f});

        System.out.println("result: " + lex.getStack().toString());
    }

    // ficha--3--para+impresion.pdf page 2 - function 1 NOT CORRECT?
    public void test6() throws IOException {
        String test =
                "{1.000000 2 1 roll 1.000000 2 1 roll 1.000000 2 1 roll 0 index 1.000000 \n" +
                        "cvr exch sub 2 1 roll 5 -1 roll 1.000000 cvr exch sub 5 1 \n" +
                        "roll 4 -1 roll 1.000000 cvr exch sub 4 1 roll 3 -1 roll 1.000000 \n" +
                        "cvr exch sub 3 1 roll 2 -1 roll 1.000000 cvr exch sub 2 1 \n" +
                        "roll pop }";

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{0.300003f});

        System.out.println("result: " + lex.getStack().toString());

        // length of output array
        int n = 4;
        float[] range = new float[]{0, 1, 0, 1, 0, 1, 0, 1};
        // ready output array
        float y[] = new float[n];

        System.out.println();
        float value;
        for (int i = 0; i < n; i++) {
            value = (Float) lex.getStack().elementAt(i);
            y[i] = Math.min(Math.max(value, range[2 * i]), range[2 * i + 1]);
            System.out.print(y[i] + ", ");
        }
        System.out.println();
    }

    // 9560_test.pdf page 2 - function 1 NOT CORRECT?
    public void test7() throws IOException {
        String test =
                "{0 0 0 0 5 4 roll 0 index 3 -1 roll add 2 1 roll pop dup 1 gt " +
                        "{pop 1} if 4 1 roll dup 1 gt {pop 1} if 4 1 roll dup 1 gt " +
                        "{pop 1} if 4 1 roll dup 1 gt {pop 1} if 4 1 roll}";

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{1f});

        System.out.println("result: " + lex.getStack().toString());

        // length of output array
        int n = 4;
        float[] range = new float[]{0, 1, 0, 1, 0, 1, 0, 1};
        // ready output array
        float y[] = new float[n];

        System.out.println();
        float value;
        for (int i = 0; i < n; i++) {
            value = (Float) lex.getStack().elementAt(i);
            y[i] = Math.min(Math.max(value, range[2 * i]), range[2 * i + 1]);
            System.out.print(y[i] + ", ");
        }
        System.out.println();
    }

    public void test8() throws IOException {
        String test =
                "{dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 0 index 1 exp 1 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 1 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 2 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 3 index 1 exp 0 mul 0 add dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse 5 4 roll pop }";

        InputStream function_4 = new ByteArrayInputStream(test.getBytes());
        Lexer lex = new Lexer();
        lex.setInputStream(function_4);
        lex.parse(new float[]{1.0f});

        System.out.println("result: " + lex.getStack().toString());

        // length of output array
        int n = 4;
        float[] range = new float[]{0, 1, 0, 1, 0, 1, 0, 1};
        // ready output array
        float y[] = new float[n];

        // domain = 0, 1, 0, 1, 0, 1
        System.out.println();
        float value;
        for (int i = 0; i < n; i++) {
            value = (Float) lex.getStack().elementAt(i);
            y[i] = Math.min(Math.max(value, range[2 * i]), range[2 * i + 1]);
            System.out.print(y[i] + ", ");
        }
        System.out.println();
    }


}
