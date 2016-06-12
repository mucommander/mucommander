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

package com.mucommander.job;

import com.mucommander.text.Translator;

/**
 * Actions on {@code FileJob}
 *
 * @author Arik Hadas
 */
class FileJobAction {

	protected final static int SKIP = 0;
	protected final static int SKIP_ALL = 1;
	protected final static int RETRY = 2;
	protected final static int CANCEL = 3;
	protected final static int APPEND = 4;
	protected final static int OK = 5;

	protected final static String SKIP_TEXT = Translator.get("skip");
	protected final static String SKIP_ALL_TEXT = Translator.get("skip_all");
	protected final static String RETRY_TEXT = Translator.get("retry");
	protected final static String CANCEL_TEXT = Translator.get("cancel");
	protected final static String APPEND_TEXT = Translator.get("resume");
	protected final static String OK_TEXT = Translator.get("ok");

}
