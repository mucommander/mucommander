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

package com.mucommander.conf;

import java.util.List;

import com.mucommander.commons.conf.ValueList;

/**
 * 
 * @author Arik Hadas
 */
public interface MuPreferencesAPI {
	public boolean setVariable(MuPreference preference, String value);
	public boolean setVariable(MuPreference preference, int value);
	public boolean setVariable(MuPreference preference, List<String> value, String separator);
	public boolean setVariable(MuPreference preference, float value);
	public boolean setVariable(MuPreference preference, boolean value);
	public boolean setVariable(MuPreference preference, long value);
	public boolean setVariable(MuPreference preference, double value);
	
	public String 		getVariable(MuPreference preference);
	public String 		getVariable(MuPreference preference, String value);
	public int 			getVariable(MuPreference preference, int value);
	public List<String> getVariable(MuPreference preference, List<String> value, String separator);
	public float 		getVariable(MuPreference preference, float value);
	public boolean 		getVariable(MuPreference preference, boolean value);
	public long 		getVariable(MuPreference preference, long value);
	public double 		getVariable(MuPreference preference, double value);
	public ValueList 	getListVariable(MuPreference preference, String separator);
	
	// TODO: remove those methods
	public boolean getBooleanVariable(String name);
	public String  getVariable(String name);
	
	public boolean isVariableSet(MuPreference preference);
	
	public String removeVariable(String name);
}
