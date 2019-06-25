package cuchaz.enigma.command;

import cuchaz.enigma.throwables.MappingParseException;
import cuchaz.enigma.translation.mapping.EntryMapping;
import cuchaz.enigma.translation.mapping.tree.EntryTree;
import cuchaz.enigma.utils.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConvertMappingsCommand extends Command {
    public ConvertMappingsCommand() {
        super("convert-mappings");
    }

    @Override
    public String getUsage() {
        return "<source-format> <source> <result-format> <result>";
    }

    @Override
    public boolean isValidArgument(int length) {
        return length == 4;
    }

    @Override
    public void run(String... args) throws IOException, MappingParseException {
        EntryTree<EntryMapping> mappings = MappingCommandsUtil.read(args[0], Paths.get(args[1]));

        Path output = Paths.get(args[3]);
        Utils.delete(output);
        MappingCommandsUtil.write(mappings, args[2], output);
    }
}
