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

package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.*;
import org.icepdf.core.pobjects.acroform.ChoiceFieldDictionary;
import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.pobjects.graphics.Shapes;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.util.Library;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import static org.icepdf.core.pobjects.acroform.ChoiceFieldDictionary.ChoiceFieldType;

/**
 * Represents a Acroform Choice widget and manages the appearance streams
 * for the various appearance states. This class can generate a postscript
 * stream that represents it current state.
 *
 * @since 5.1
 */
public class ChoiceWidgetAnnotation extends AbstractWidgetAnnotation<ChoiceFieldDictionary> {

    private ChoiceFieldDictionary fieldDictionary;

    public ChoiceWidgetAnnotation(Library l, HashMap h) {
        super(l, h);
        fieldDictionary = new ChoiceFieldDictionary(library, entries);
    }

    /**
     * Some choices lists are lacking the /opt key so we need to do our best to generate the list from the shapes.
     *
     * @return list of potential options.
     */
    public ArrayList<ChoiceFieldDictionary.ChoiceOption> generateChoices() {
        Shapes shapes = getShapes();
        if (shapes != null) {
            ArrayList<ChoiceFieldDictionary.ChoiceOption> options = new ArrayList<ChoiceFieldDictionary.ChoiceOption>();
            String tmp;
            ArrayList<LineText> pageLines = shapes.getPageText().getPageLines();
            for (LineText lines : pageLines) {
                for (WordText word : lines.getWords()) {
                    tmp = word.toString();
                    if (!(tmp.equals("") || tmp.equals(" "))) {
                        options.add(fieldDictionary.buildChoiceOption(tmp, tmp));
                    }
                }
            }
            return options;
        }
        return new ArrayList<ChoiceFieldDictionary.ChoiceOption>();
    }

    /**
     * Resets the appearance stream for this instance using the current state.  The mark content section of the stream
     * is found and the edit it make to best of our ability.
     *
     * @param dx            x offset of the annotation
     * @param dy            y offset of the annotation
     * @param pageTransform current page transform.
     */
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageTransform) {
        ChoiceFieldType choiceFieldType =
                fieldDictionary.getChoiceFieldType();

        // get at the original postscript as well alter the marked content
        Appearance appearance = appearances.get(currentAppearance);
        AppearanceState appearanceState = appearance.getSelectedAppearanceState();
        Rectangle2D bbox = appearanceState.getBbox();
        AffineTransform matrix = appearanceState.getMatrix();
        String currentContentStream = appearanceState.getOriginalContentStream();

        // alterations vary by choice type.
        if (choiceFieldType == ChoiceFieldType.CHOICE_COMBO ||
                choiceFieldType == ChoiceFieldType.CHOICE_EDITABLE_COMBO) {
            // relatively straight forward replace with new selected value.
            if (currentContentStream != null) {
                currentContentStream = buildChoiceComboContents(currentContentStream);
            } else {
                // todo no stream and we will need to build one.
                currentContentStream = "";
            }
        } else {
            // build out the complex choice list content stream
            if (currentContentStream != null) {
                currentContentStream = buildChoiceListContents(currentContentStream);
            } else {
                // todo no stream and we will need to build one.
                currentContentStream = "";
            }
        }
        // finally create the shapes from the altered stream.
        if (currentContentStream != null) {
            appearanceState.setContentStream(currentContentStream.getBytes());
        }

        // some widgets don't have AP dictionaries in such a case we need to create the form object
        // and build out the default properties.
        Form appearanceStream = getOrGenerateAppearanceForm();

        if (appearanceStream != null) {
            // update the content stream with the new stream data.
            appearanceStream.setRawBytes(currentContentStream.getBytes());
            // add the appearance stream
            StateManager stateManager = library.getStateManager();
            stateManager.addChange(new PObject(appearanceStream, appearanceStream.getPObjectReference()));
            // add an AP entry for the
            HashMap<Object, Object> appearanceRefs = new HashMap<Object, Object>();
            appearanceRefs.put(APPEARANCE_STREAM_NORMAL_KEY, appearanceStream.getPObjectReference());
            entries.put(APPEARANCE_STREAM_KEY, appearanceRefs);
            Rectangle2D formBbox = new Rectangle2D.Float(0, 0,
                    (float) bbox.getWidth(), (float) bbox.getHeight());
            appearanceStream.setAppearance(null, matrix, formBbox);
            // add link to resources on forum, if no resources exist.
            if (library.getResources(appearanceStream.getEntries(), Form.RESOURCES_KEY) == null) {
                appearanceStream.getEntries().put(Form.RESOURCES_KEY,
                        library.getCatalog().getInteractiveForm().getResources().getEntries());
            }
            // add the annotation as changed as T entry has also been updated to reflect teh changed content.
            stateManager.addChange(new PObject(this, this.getPObjectReference()));

            // compress the form object stream.
            if (false && compressAppearanceStream) {
                appearanceStream.getEntries().put(Stream.FILTER_KEY, new Name("FlateDecode"));
            } else {
                appearanceStream.getEntries().remove(Stream.FILTER_KEY);
            }
            appearanceStream.init();
        }
    }


    public void reset() {
        Object oldValue = fieldDictionary.getFieldValue();
        Object tmp = fieldDictionary.getDefaultFieldValue();
        if (tmp == null) {
            FieldDictionary parentFieldDictionary = fieldDictionary.getParent();
            if (parentFieldDictionary != null) {
                tmp = parentFieldDictionary.getDefaultFieldValue();
            }
        }
        if (tmp != null) {
            // apply the default value
            fieldDictionary.setFieldValue(tmp, getPObjectReference());
            changeSupport.firePropertyChange("valueFieldReset", oldValue, tmp);
        } else {
            // otherwise we remove the key
            fieldDictionary.getEntries().remove(FieldDictionary.V_KEY);
            fieldDictionary.setIndexes(null);
            // check the parent as well.
            FieldDictionary parentFieldDictionary = fieldDictionary.getParent();
            if (parentFieldDictionary != null) {
                parentFieldDictionary.getEntries().remove(FieldDictionary.V_KEY);
                if (parentFieldDictionary instanceof ChoiceFieldDictionary) {
                    ((ChoiceFieldDictionary) parentFieldDictionary).setIndexes(null);
                }
            }
            changeSupport.firePropertyChange("valueFieldReset", oldValue, null);
        }
    }

    @Override
    public ChoiceFieldDictionary getFieldDictionary() {
        return fieldDictionary;
    }

    public String buildChoiceComboContents(String currentContentStream) {
        ArrayList<ChoiceFieldDictionary.ChoiceOption> choices = fieldDictionary.getOptions();
        // double check we have some choices to work with.
        if (choices == null) {
            // generate them from the content stream.
            choices = generateChoices();
            fieldDictionary.setOptions(choices);
        }
        String selectedField = (String) fieldDictionary.getFieldValue();
        int btStart = currentContentStream.indexOf("BT");
        int btEnd = currentContentStream.lastIndexOf("ET");
        int bmcStart = currentContentStream.indexOf("BMC");
        int bmcEnd = currentContentStream.lastIndexOf("EMC");
        // grab the pre post marked content postscript.
        String preBmc = btStart >= 0 ? currentContentStream.substring(0, btStart + 2) :
                currentContentStream.substring(0, bmcStart + 3);
        String postEmc = btEnd >= 0 ? currentContentStream.substring(btEnd) :
                currentContentStream.substring(0, bmcEnd + 3);

        // marked content which we will use to try and find some data points.
        //String markedContent = currentContentStream.substring(bmcStart, bmcEnd);

        // check for a bounding box definition
        //Rectangle2D.Float bounds = findBoundRectangle(markedContent);

        // finally build out the new content stream
        StringBuilder content = new StringBuilder();
        // apply font
        if (fieldDictionary.getDefaultAppearance() != null) {
            String markedContent = fieldDictionary.getDefaultAppearance();
            Page page = getPage();
            markedContent = fieldDictionary.generateDefaultAppearance(markedContent,
                    page != null ? page.getResources() : null);
            content.append(markedContent).append(' ');
        } else { // common font and colour layout for most form elements.
            content.append("/Helv 12 Tf 0 g ");
        }
        // apply the text offset, 4 is just a generic padding.
        content.append(4).append(' ').append(4).append(" Td ");
        // hex encode the text so that we better handle character codes > 127
        content = encodeHexString(content, selectedField).append(" Tj ");
        // build the final content stream.
        if (btStart >= 0) {
            currentContentStream = preBmc + "\n" + content + "\n" + postEmc;
        } else {
            currentContentStream = preBmc + " BT\n" + content + "\n ET EMC";
        }

        return currentContentStream;
    }

    public String buildChoiceListContents(String currentContentStream) {

        ArrayList<ChoiceFieldDictionary.ChoiceOption> choices = fieldDictionary.getOptions();
        // double check we have some choices to work with.
        if (choices == null) {
            // generate them from the content stream.
            choices = generateChoices();
            fieldDictionary.setOptions(choices);
        }
        ArrayList<Integer> selections = fieldDictionary.getIndexes();
        // mark the indexes of the mark content.
        int bmcStart = currentContentStream.indexOf("BMC") + 3;
        int bmcEnd = currentContentStream.indexOf("EMC");
        // grab the pre post marked content postscript.
        String preBmc = currentContentStream.substring(0, bmcStart);
        String postEmc = currentContentStream.substring(bmcEnd);
        // marked content which we will use to try and find some data points.
        String markedContent = currentContentStream.substring(bmcStart, bmcEnd);

        // check for a bounding box definition
        Rectangle2D.Float bounds = findBoundRectangle(markedContent);

        // check to see if there is a selection box colour defined.
        float[] selectionColor = findSelectionColour(markedContent);

        // and finally look for a previous selection box,  this can be null, no default value
        Rectangle2D.Float selectionRectangle = findSelectionRectangle(markedContent);
        float lineHeight = 13.87f;
        if (selectionRectangle != null) {
            lineHeight = selectionRectangle.height;
        }

        // we need to plot out where the opt text is going to go as well as the background colour and text colour
        // for any selected items. So we update the choices model to reflect the current selection state.
        boolean isSelection = false;
        if (selections != null) {
            for (int i = 0, max = choices.size(); i < max; i++) {
                for (int selection : selections) {
                    if (selection == i) {
                        choices.get(i).setIsSelected(true);
                        isSelection = true;
                    } else {
                        choices.get(i).setIsSelected(false);
                    }
                }
            }
        }
        // figure out offset range to insure a single selection is always visible
        int startIndex = 0, endIndex = choices.size();
        if (selections != null && selections.size() == 1) {
            int numberLines = (int) Math.floor(bounds.height / lineHeight);
            // check if list is smaller then number of lines
            int selectedIndex = selections.get(0);
            if (choices.size() < numberLines) {
                // nothing to do.
            } else if (selectedIndex < numberLines) {
                endIndex = numberLines + 1;
            }
            // check for bottom out range
            else if (endIndex - selectedIndex <= numberLines) {
                startIndex = endIndex - numberLines;
            }
            // else mid range just need to start the index.
            else {
                startIndex = selectedIndex;
                endIndex = numberLines + 1;

            }
            // we have a single line
            if (startIndex > endIndex) {
                endIndex = startIndex + 1;
            }
        }

        // finally build out the new content stream
        StringBuilder content = new StringBuilder();
        // bounding rectangle.
        content.append("q ").append(generateRectangle(bounds)).append("W n ");
        // apply selection highlight background.
        if (isSelection) {
            // apply colour
            content.append(selectionColor[0]).append(' ').append(selectionColor[1]).append(' ')
                    .append(selectionColor[2]).append(" rg ");
            // apply selection
            Rectangle2D.Float firstSelection;
            if (selectionRectangle == null) {
                firstSelection = new Rectangle2D.Float(bounds.x, bounds.y + bounds.height - lineHeight, bounds.width, lineHeight);
            } else {
                firstSelection = new Rectangle2D.Float(selectionRectangle.x, bounds.y + bounds.height - lineHeight,
                        selectionRectangle.width, lineHeight);
            }
            ChoiceFieldDictionary.ChoiceOption choice;
            for (int i = startIndex; i < endIndex; i++) {
                choice = choices.get(i);
                // check if a selection rectangle was defined, if not we might have a custom style and we
                // avoid the selection background (only have one test case for this)
                if (choice.isSelected() && selectionRectangle != null) {
                    content.append(generateRectangle(firstSelection)).append("f ");
                }
                firstSelection.y -= lineHeight;
            }
        }
        // apply the ext.
        content.append("BT ");
        // apply font
        if (fieldDictionary.getDefaultAppearance() != null) {
            content.append(fieldDictionary.getDefaultAppearance());
        } else { // common font and colour layout for most form elements.
            content.append("/Helv 12 Tf 0 g ");
        }
        // apply the line height
        content.append(lineHeight).append(" TL ");
        // apply the text offset, 4 is just a generic padding.
        content.append(4).append(' ').append(bounds.height + 4).append(" Td ");
        // print out text
        ChoiceFieldDictionary.ChoiceOption choice;
        for (int i = startIndex; i < endIndex; i++) {
            choice = choices.get(i);
            if (choice.isSelected() && selectionRectangle != null) {
                content.append("1 g ");
            } else {
                content.append("0 g ");
            }
            content.append('(').append(choice.getLabel()).append(")' ");
        }
        content.append("ET Q");
        // build the final content stream.
        currentContentStream = preBmc + "\n" + content + "\n" + postEmc;
        return currentContentStream;
    }

    /**
     * The selection colour is generally defined in DeviceRGB and occurs after the bounding box has been defined.
     * This utility method tries to parse out the colour information and return it in float[3].  If the data can't
     * be found then we return the default colour of new float[]{0.03922f, 0.14118f, 0.41569f}.
     *
     * @param markedContent content to look for colour info.
     * @return found colour data or new float[]{0.03922f, 0.14118f, 0.41569f}.
     */
    private float[] findSelectionColour(String markedContent) {
        int selectionStart = markedContent.indexOf("n") + 1;
        int selectionEnd = markedContent.lastIndexOf("rg");
        if (selectionStart < selectionEnd && selectionEnd > 0) {
            String potentialNumbers = markedContent.substring(selectionStart, selectionEnd);
            StringTokenizer toker = new StringTokenizer(potentialNumbers);
            float[] points = new float[3];
            int i = 0;
            while (toker.hasMoreTokens()) {
                try {
                    float tmp = Float.parseFloat(toker.nextToken());
                    points[i] = tmp;
                    i++;
                } catch (NumberFormatException e) {
                    break;
                }
            }
            if (i == 3) {
                return points;
            }
        }
        // default selection colour.
        return new float[]{0.03922f, 0.14118f, 0.41569f};
    }

}

