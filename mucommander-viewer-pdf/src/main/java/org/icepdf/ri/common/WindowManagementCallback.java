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
package org.icepdf.ri.common;

import org.icepdf.ri.util.PropertiesManager;

import javax.swing.*;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * <p>An interface that describes the necessary methods needed for common
 * window management.  An application may need to centrally manage the process
 * of opening and closing new windows, as well as requests
 * to end the program. This interface facilitates that capability.
 *
 * @author Mark Collette
 * @since 2.0
 */
public interface WindowManagementCallback {
    public void newWindow(String path);

    public void newWindow(URL url);

    public void disposeWindow(SwingController controller, JFrame viewer,
                              Properties properties);

    public void minimiseAllWindows();

    public void bringAllWindowsToFront(SwingController frontMost);

    public void bringWindowToFront(int index);

    public List getWindowDocumentOriginList(SwingController giveIndex);

    public void quit(SwingController controller, JFrame viewer,
                     Properties properties);

    public PropertiesManager getProperties();
}
