/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.ui.notifier;

/**
 * @author Maxence Bernard
 */
public interface NotificationTypes {

    /** 'Job completed' notification type, to notify that a job is finished */
    public final static int NOTIFICATION_TYPE_JOB_COMPLETED = 0;

    /** 'Job completed' notification type, to notify of an error in a job */
    public final static int NOTIFICATION_TYPE_JOB_ERROR = 1;
    
}
