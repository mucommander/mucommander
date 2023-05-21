package com.mucommander.ui.terminal;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ProcessTtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Exact copy of https://github.com/JetBrains/jediterm/blob/master/pty/src/com/jediterm/pty/PtyProcessTtyConnector.java (64b66f2)
 *
 * This is because this class is not present in jediterm-core, but it is only present in jediterm-core-pty,
 * and since both libs contain almost the same classes... and jediterm-core has newer version, and jediterm-core-pty stayed on 3.0....
 * so, we will use jediterm-core and this class to avoid having jediterm-core-pty imported.
 *
 * @author traff
 */
public class PtyProcessTtyConnector extends ProcessTtyConnector {
    private final PtyProcess myProcess;

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset) {
        this(process, charset, null);
    }

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @Nullable List<String> commandLine) {
        super(process, charset, commandLine);
        myProcess = process;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        if (isConnected()) {
            myProcess.setWinSize(new WinSize(termSize.getColumns(), termSize.getRows()));
        }
    }

    @Override
    public boolean isConnected() {
        return myProcess.isAlive();
    }

    @Override
    public String getName() {
        return "Local";
    }

}
