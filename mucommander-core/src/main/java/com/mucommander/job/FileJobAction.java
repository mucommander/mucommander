/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
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

package com.mucommander.job;

import com.mucommander.text.Translator;

/**
 * Actions on {@code FileJob}
 *
 * @author Arik Hadas
 */
public interface FileJobAction {

	int SKIP = 0;
	int SKIP_ALL = 1;
	int RETRY = 2;
	int CANCEL = 3;
	int APPEND = 4;
	int OK = 5;

	String SKIP_TEXT = Translator.get("skip");
	String SKIP_ALL_TEXT = Translator.get("skip_all");
	String RETRY_TEXT = Translator.get("retry");
	String CANCEL_TEXT = Translator.get("cancel");
	String APPEND_TEXT = Translator.get("resume");
	String OK_TEXT = Translator.get("ok");

}
