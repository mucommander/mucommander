package com.mucommander.core.desktop;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.desktop.LocalFileOperation;
import com.mucommander.process.ProcessRunner;

class CommandOpenCommandPrompt extends LocalFileOperation {
    @Override
    public boolean isAvailable() {
        return CommandManager.getCommandForAlias(CommandManager.CMD_OPENER_ALIAS) != null;
    }

    @Override
    public CompletionStage<Optional<String>> execute(AbstractFile file) throws IOException {
        Command command = CommandManager.getCommandForAlias(CommandManager.CMD_OPENER_ALIAS);
        if (command == null)
            throw new UnsupportedOperationException();

        if (!file.isDirectory()) {
            file = file.getParent();
        }
        return ProcessRunner.executeAsync(command.getTokens(file), file);
    }

    @Override
    public String getName() {
        Command command = CommandManager.getCommandForAlias(CommandManager.CMD_OPENER_ALIAS);
        return command != null ? command.getDisplayName() : null;
    }
}