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

import org.icepdf.core.pobjects.FileSpecification;
import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.pobjects.acroform.InteractiveForm;
import org.icepdf.core.pobjects.annotations.AbstractWidgetAnnotation;
import org.icepdf.core.util.Library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Upon invocation of a submit-form action, a conforming processor shall transmit the names and values of selected
 * interactive form fields to a specified uniform resource locator (URL).
 * <p/>
 * The main function of this class is to interpret the F value against the submit bit flags and submit the form
 * data and other data as needed.
 * <p/>
 * <b>Note: </b>Some flagss are not yet supported, see flag _BIT constants for more information on support flags.
 *
 * @since 5.1
 */
public class SubmitFormAction extends FormAction {

    private static final Logger logger =
            Logger.getLogger(SubmitFormAction.class.toString());

    private static final String USER_AGENT = "Mozilla/5.0";

    // Table 236 flags, Additional entries specific to a submit-form action.

    /**
     * If clear, the Fields array (see Table 236) specifies which fields to include in the submission.
     * (All descendants of the specified fields in the field hierarchy shall be submitted as well.)
     * <p/>
     * If set, the Fields array tells which fields to exclude. All fields in the document’s interactive form shall be
     * submitted except those listed in the Fields array and those whose NoExport flag (see Table 221)
     * is set and fields with no values if the IncludeNoValueFields flag is clear.
     */
    public int INCLUDE_EXCLUDE_BIT = 0X0000001;  // bit 1

    /**
     * If set, all fields designated by the Fields array and the Include/Exclude flag shall be submitted, regardless
     * of whether they have a value (V entry in the field dictionary). For fields without a value, only the field name
     * shall be transmitted.
     * <p/>
     * If clear, fields without a value shall not be submitted.
     */
    public int INCLUDE_NO_VALUE_FIELDS_BIT = 0X0000002; // bit 2

    /**
     * Meaningful only if the SubmitPDF and XFDF flags are clear. If set, field names and values shall be submitted in
     * HTML Form format. If clear, they shall be submitted in Forms Data Format (FDF); see 12.7.7, “Forms Data Format.”
     */
    public int EXPORT_FORMAT_BIT = 0X0000004;  // bit 3

    /**
     * If set, field names and values shall be submitted using an HTTP GET request. If clear, they shall be submitted
     * using a POST request. This flag is meaningful only when the ExportFormat flag is set; if ExportFormat is clear,
     * this flag shall also be clear.
     */
    public int GET_METHOD_BIT = 0X0000010;  // bit 4

    /**
     * If set, the coordinates of the mouse click that caused the submit-form action shall be transmitted as part of
     * the form data. The coordinate values are relative to the upper-left corner of the field’s widget annotation
     * rectangle. They shall be represented in the data in the format
     * <p/>
     * name.x=xval&name.y=yval
     * <p/>
     * where name is the field’s mapping name (TM in the field dictionary) if present; otherwise, name is the field name.
     * If the value of the TM entry is a single ASCII SPACE (20h) character, both the name and the ASCII PERIOD (2Eh)
     * following it shall be suppressed, resulting in the format
     * <p/>
     * x=xval&y=yval
     * <p/>
     * This flag shall be used only when the ExportFormat flag is set. If ExportFormat is clear, this flag shall also
     * be clear
     */
    public int SUBMIT_COORDINATES_BIT = 0X0000004;  // bit 5

    /**
     * (PDF 1.4) shall be used only if the SubmitPDF flags are clear. If set, field names and values shall be
     * submitted as XFDF.
     */
    public int XFDF_BIT = 0X0000020;  // bit 6

    /**
     * (PDF 1.4) shall be used only when the form is being submitted in Forms Data Format (that is, when both the
     * XFDF and ExportFormat flags are clear). If set, the submitted FDF file shall include the contents of all
     * incremental updates to the underlying PDF document, as contained in the Differences entry in the FDF
     * dictionary (see Table 243). If clear, the incremental updates shall not be included.
     */
    public int INCLUDE_APPEND_SAVES_BIT = 0X0000040;  // bit 7

    /**
     * (PDF 1.4) shall be used only when the form is being submitted in Forms Data Format (that is, when both the XFDF a
     * nd ExportFormat flags are clear). If set, the submitted FDF file shall include includes all markup
     * annotations in the underlying PDF document (see 12.5.6.2, “Markup Annotations”). If clear, markup annotations
     * shall not be included.
     */
    public int INCLUDE_ANNOTATIONS_BIT = 0X0000040;  // bit 8

    /**
     * (PDF 1.4) If set, the document shall be submitted as PDF, using the MIME content type application/pdf (described
     * in Internet RFC 2045, Multipurpose Internet Mail Extensions (MIME), Part One: Format of Internet Message Bodies;
     * see the Bibliography). If set, all other flags shall be ignored except GetMethod.
     */
    public int SUBMIT_PDF_BIT = 0X0000100;  // bit 9

    /**
     * (PDF 1.4) If set, any submitted field values representing dates shall be converted to the standard format
     * described in 7.9.4, “Dates.”
     * <p/>
     * <b>NOTE</b><br/>
     * The interpretation of a form field as a date is not specified explicitly in the field itself but only in the
     * JavaScript code that processes it.
     */
    public int CANONICAL_FORMAT_BIT = 0X0000512;  // bit 10

    /**
     * (PDF 1.4) shall be used only when the form is being submitted in Forms Data Format (that is, when both the XFDF
     * and ExportFormat flags are clear) and the IncludeAnnotations flag is set. If set, it shall include only those
     * markup annotations whose T entry (see Table 170) matches the name of the current user, as determined by the
     * remote server to which the form is being submitted.
     * <p/>
     * <b>NOTE 1</b><br/>The T entry for markup annotations specifies the text label that is displayed in the title bar
     * of the annotation’s pop-up window and is assumed to represent the name of the user authoring the annotation.
     * <p/>
     * <b>NOTE 2</b><br/>This allows multiple users to collaborate in annotating a single remote PDF document without
     * affecting one another’s annotations.
     */
    public int EXCL_NON_USER_ANNOTS_BIT = 0X0001024;  // bit 11

    /**
     * (PDF 1.4) shall be used only when the form is being submitted in Forms Data Format (that is, when both the XFDF
     * and ExportFormat flags are clear). If set, the submitted FDF shall exclude the F entry.
     */
    public int EXCL_F_KEY_BIT = 0X0002048;  // bit 12

    /**
     * (PDF 1.5) shall be used only when the form is being submitted in Forms Data Format (that is, when both the XFDF
     * and ExportFormat flags are clear). If set, the F entry of the submitted FDF shall be a file specification
     * containing an embedded file stream representing the PDF file from which the FDF is being submitted.
     */
    public int EMBED_FORM_BIT = 0X0008192;  // bit 14

    public SubmitFormAction(Library l, HashMap h) {
        super(l, h);
    }


    /**
     * An array identifying which fields to reset or which to exclude from
     * resetting, depending on the setting of the Include/Exclude flag in the
     * Flags entry (see Table 239). Each element of the array shall be either
     * an indirect reference to a field dictionary or (PDF 1.3) a text string
     * representing the fully qualified name of a field. Elements of both kinds
     * may be mixed in the same array.
     * <p/>
     * If this entry is omitted, the Include/Exclude flag shall be ignored, and all
     * fields in the document’s interactive form shall be submitted except those whose
     * NoExport flag (see Table 221) is set. Fields with no values may also be excluded,
     * as dictated by the value of the IncludeNoValueFields flag; see Table 237.
     *
     * @return list of fields if present otherwise null.
     */
    public List getFields() {
        Object tmp = library.getArray(entries, FIELDS_KEY);
        if (tmp != null) {
            return (List) tmp;
        }
        return null;
    }

    public FileSpecification getFileSpecification() {
        Object tmp = library.getObject(entries, F_KEY);
        if (tmp instanceof HashMap) {
            return new FileSpecification(library, (HashMap) tmp);
        }
        return null;
    }

    /**
     * Execute the form submission.  The following formats are currently supported:
     * <ul>
     * <li>HTML Form format</li>
     * <li>Forms Data Format(not supported)</li>
     * <li>XFDF(not supported)</li>
     * <li>PDF (not supported)</li>
     * </ul>
     *
     * @param x x-coordinate of the mouse event that actuated the submit.
     * @param y y-coordinate of the mouse event that actuated the submit.
     * @return value of one if submit was successful, zero if not.
     */
    public int executeFormAction(int x, int y) {
        if (isExportFormat() && !(isXFDF() && isSubmitPDF())) {
            return executeHTMLSubmission(x, y);
        } else if (isXFDF() && !isSubmitPDF()) {
            return executeXFDFSubmission(x, y);
        } else if (isSubmitPDF()) {
            return executePDFSubmission(x, y);
        } else if (isEmbedForm()) {
            return executeFormsDatFormatSubmission(x, y);
        }
        return 0;
    }

    /**
     * Field names and values shall be submitted in HTML Form format.
     *
     * @param x x-coordinate of input action.
     * @param y y-coordinate of input action.
     * @return submit response code.
     */
    private int executeHTMLSubmission(int x, int y) {


        FileSpecification fileSpecification = getFileSpecification();
        if (fileSpecification != null) {

            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("HTML Submit form action: " + fileSpecification.getFileSpecification());
            }

            // value pairs to submit.
            HashMap<String, String> params = new HashMap<String, String>();

            // check for a fields entry
//            List fields = getFields();
//            if (!isIncludeExclude() && fields != null){
//                // pick out the specified fields.
//                if (logger.isLoggable(Level.FINEST)){
//                    logger.finest("Specified Fields for submit. ");
//                }
//            }else{
            // NOTE  we use them all have no examples yet of fields selection so we are going to submit all
            // values for the time being.
            InteractiveForm form = library.getCatalog().getInteractiveForm();
            ArrayList<Object> fields = form.getFields();
            for (Object field : fields) {
                descendFormTree(field, params);
            }
//            }

            // append coordinates.
            if (isSubmitCoordinates()) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Including coordinates in submit.");
                }
                params.put("x", String.valueOf(x));
                params.put("y", String.valueOf(y));
            }

            // todo once we get an example: dates format
            if (isCanonicalFormat()) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Uses canonical date format.");
                }
            }

            // submit the data.
            if (isGetMethod()){
                return sendGET(fileSpecification.getFileSpecification(), formatParams(params));
            }else{
                return sendPOST(fileSpecification.getFileSpecification(), formatParams(params));
            }

        }
        return 0;
    }

    //
    private int executePDFSubmission(int x, int y) {
        return 0;
    }

    // currently not supported.
    private int executeXFDFSubmission(int x, int y) {
        return 0;
    }

    // currently not supported.
    private int executeFormsDatFormatSubmission(int x, int y) {
        return 0;
    }

    /**
     * Dive into the hierarchy to get the name value pairs of the fields.  Currently not recursive as
     * we don't have any example to test against.
     *
     * @param formNode root form node.
     * @param params   name value pars of form fields.
     */
    protected void descendFormTree(Object formNode, HashMap<String, String> params) {
        if (formNode instanceof AbstractWidgetAnnotation) {
//            FieldDictionary fieldDictionary = ((AbstractWidgetAnnotation) formNode).getFieldDictionary();
        } else if (formNode instanceof FieldDictionary) {
            // iterate over the kid's array.
            FieldDictionary child = (FieldDictionary) formNode;
            Object value = child.getFieldValue();
            if ((value == null || value.equals("")) && child.getKids() != null && child.getKids().size() == 1) {
                value = child.getKids().get(0);
                if (value instanceof AbstractWidgetAnnotation) {
                    value = ((AbstractWidgetAnnotation) value).getFieldDictionary().getFieldValue();
                } else if (value instanceof FieldDictionary) {
                    value = ((FieldDictionary) value).getFieldValue();
                }
            }
            if (value != null && !value.equals("")) {
                params.put(child.getPartialFieldName(), value.toString());
            }else if(isIncludeNoValueFields()){
                params.put(child.getPartialFieldName(), value == null?null:value.toString());
            }
        }
    }

    // generates a HTTP get for submitting the form data.
    private static int sendGET(String url, String params) {
        int responseCode = HttpURLConnection.HTTP_SEE_OTHER;
        try {
            URL obj = new URL(url + "?" + params);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (logger.isLoggable(Level.FINER)) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    logger.finer("HTTP GET response: " + response.toString());
                }else {
                    if (logger.isLoggable(Level.FINER)){
                        logger.finer("HTTP GET failed: " + url + ", response " + responseCode);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error processing GET: " + url, e);
        }
        return responseCode;
    }

    // generates a HTTP post for submitting the form data.
    private static int sendPOST(String url, String params) {
        int responseCode = HttpURLConnection.HTTP_SEE_OTHER;
        try {
            URL urlObject = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) urlObject.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("User-Agent", USER_AGENT);

            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(params.getBytes());
            outputStream.flush();
            outputStream.close();
            responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                if (logger.isLoggable(Level.FINER)){
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            urlConnection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    logger.finer("HTTP POST response: " + response.toString());
                }
            }else {
                if (logger.isLoggable(Level.FINER)){
                    logger.finer("HTTP POST failed: " + url + ", response " + responseCode);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error processing POST: " + url, e);
        }
        return responseCode;
    }

    private String formatParams(HashMap<String, String> params){
        StringBuilder submitParams = new StringBuilder();
        for (String key : params.keySet()) {
            submitParams.append(key).append("=").append(params.get(key));
            submitParams.append("&");
        }
        if (submitParams.length() > 1) {
            return submitParams.subSequence(0, submitParams.length() - 1).toString();
        }else{
            return "";
        }
    }

    /**
     * @see #INCLUDE_EXCLUDE_BIT
     */
    public boolean isIncludeExclude() {
        return (getFlags() & INCLUDE_EXCLUDE_BIT) == INCLUDE_EXCLUDE_BIT;
    }

    /**
     * @see #INCLUDE_NO_VALUE_FIELDS_BIT
     */
    public boolean isIncludeNoValueFields() {
        return (getFlags() & INCLUDE_NO_VALUE_FIELDS_BIT) == INCLUDE_NO_VALUE_FIELDS_BIT;
    }

    /**
     * @see #EXPORT_FORMAT_BIT
     */
    public boolean isExportFormat() {
        return (getFlags() & EXPORT_FORMAT_BIT) == EXPORT_FORMAT_BIT;
    }

    /**
     * @see #GET_METHOD_BIT
     */
    public boolean isGetMethod() {
        return (getFlags() & GET_METHOD_BIT) == GET_METHOD_BIT;
    }

    /**
     * @see #SUBMIT_COORDINATES_BIT
     */
    public boolean isSubmitCoordinates() {
        return (getFlags() & SUBMIT_COORDINATES_BIT) == SUBMIT_COORDINATES_BIT;
    }

    /**
     * @see #SUBMIT_COORDINATES_BIT
     */
    public boolean isXFDF() {
        return (getFlags() & XFDF_BIT) == XFDF_BIT;
    }

    /**
     * @see #INCLUDE_APPEND_SAVES_BIT
     */
    public boolean isIncludeAppendSaves() {
        return (getFlags() & INCLUDE_APPEND_SAVES_BIT) == INCLUDE_APPEND_SAVES_BIT;
    }

    /**
     * @see #INCLUDE_ANNOTATIONS_BIT
     */
    public boolean isIncludeAnnotations() {
        return (getFlags() & INCLUDE_ANNOTATIONS_BIT) == INCLUDE_ANNOTATIONS_BIT;
    }

    /**
     * @see #SUBMIT_PDF_BIT
     */
    public boolean isSubmitPDF() {
        return (getFlags() & SUBMIT_PDF_BIT) == SUBMIT_PDF_BIT;
    }

    /**
     * @see #CANONICAL_FORMAT_BIT
     */
    public boolean isCanonicalFormat() {
        return (getFlags() & CANONICAL_FORMAT_BIT) == CANONICAL_FORMAT_BIT;
    }

    /**
     * @see #EXCL_NON_USER_ANNOTS_BIT
     */
    public boolean isExcludeNonUserAnnots() {
        return (getFlags() & EXCL_NON_USER_ANNOTS_BIT) == EXCL_NON_USER_ANNOTS_BIT;
    }

    /**
     * @see #EXCL_F_KEY_BIT
     */
    public boolean isExcludeFKey() {
        return (getFlags() & EXCL_F_KEY_BIT) == EXCL_F_KEY_BIT;
    }

    /**
     * @see #EMBED_FORM_BIT
     */
    public boolean isEmbedForm() {
        return (getFlags() & EMBED_FORM_BIT) == EMBED_FORM_BIT;
    }
}
