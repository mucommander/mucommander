package com.mucommander.ant.macosx;

/**
 * Represents an date key in the property list.
 * @author Nicolas Rinaudo
 */
public class DateKey extends NamedInfoElement {
    public DateKey() {setValue(new DateValue());}
    public void setYear(int i) {((DateValue)getValue()).setYear(i);}
    public void setMonth(int i) {((DateValue)getValue()).setMonth(i);}
    public void setDay(int i) {((DateValue)getValue()).setDay(i);}
    public void setHours(int i) {((DateValue)getValue()).setHours(i);}
    public void setMinutes(int i) {((DateValue)getValue()).setMinutes(i);}
    public void setSeconds(int i) {((DateValue)getValue()).setSeconds(i);}
}
