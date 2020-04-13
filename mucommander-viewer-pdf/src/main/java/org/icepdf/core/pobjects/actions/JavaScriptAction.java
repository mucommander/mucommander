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
package org.icepdf.core.pobjects.actions;

import org.icepdf.core.pobjects.Name;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.pobjects.StringObject;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.HashMap;

/**
 * Upon invocation of a JavaScript action, a conforming processor shall execute a script that is written in the
 * JavaScript programming language. Depending on the nature of the script, various interactive form fields in the
 * document may update their values or change their visual appearances. Mozilla Development Center's Client-Side
 * JavaScript Reference and the Adobe JavaScript for Acrobat API Reference (see the Bibliography) give details on
 * the contents and effects of JavaScript scripts. Table 217 shows the action dictionary entries specific to this
 * type of action.
 *
 * @since 5.2
 */
public class JavaScriptAction extends Action{

    public static final Name JS_KEY = new Name("JS");

    private String javaScript;

    public JavaScriptAction(Library l, HashMap h) {
        super(l, h);
        Object value = library.getObject(entries, JS_KEY);
        if (value instanceof StringObject) {
            StringObject text = (StringObject) value;
            javaScript = Utils.convertStringObject(library, text);
        }else if (value instanceof Stream){
            Stream jsStream = (Stream)value;
            javaScript = new String(jsStream.getDecodedStreamBytes());
        }
    }

    public String getJavaScript(){
        return javaScript;
    }
}
