package dev.barebones.commander.core.desktop;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import dev.barebones.commander.command.Command;
import dev.barebones.commander.command.CommandManager;
import dev.barebones.commander.commons.file.AbstractFile;
import dev.barebones.commander.desktop.LocalFileOperation;
import dev.barebones.commander.process.ProcessRunner;

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