package net.ansinn.pixelatte;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class Main {

    static final Command[] commands = {
            new Command("help", "Get more info on a given command.", "help <command>", Main::HelpCommand),
            new Command("view", "Opens up a swift viewing window for the image. This is mainly implemented for testing purposes only", "view", args -> {}),
            new Command("info", "Show an image's info about top level data and lower level ", "info", args -> {})
    };

    public static void main(String[] args) {
        // Check for arg minimum length
        if(args.length == 0) {
            System.out.println("Available commands: ");
            for (var command : commands)
                System.out.println(" -" + command.name);
            return;
        }

        // Parse arguments
        var command = args[0];
        for (var commandInfo : commands) {
            if (commandInfo.name.equalsIgnoreCase(command))
                commandInfo.func.accept(Arrays.copyOfRange(args, 1, args.length));
        }

    }

    static void HelpCommand(String[] args) {
        if (args.length == 0) {
            PrintCommand(commands[0]);
            return;
        }

        var commandName = args[0];

        for (var command : commands) if(command.name.equalsIgnoreCase(commandName)) {
            PrintCommand(command);
            return;
        }

        System.out.println("Invalid command: " + commandName);
    }

    static void ViewCommand() {

    }

    static void PrintCommand(Command command) {
        var borderLength = 25;
        var border = "=".repeat(borderLength);

        System.out.println(border);
        System.out.println(WrapString(borderLength, "Name: " + command.name));
        System.out.println(" ");
        System.out.println(WrapString(borderLength, "Description: " + command.description));
        System.out.println(" ");
        System.out.println(WrapString(borderLength, "Usage: " + command.example));
        System.out.println(border);
    }

    static String WrapString(int maxLen, String str) {
        // No need for useless computation
        if (maxLen < 2)
            throw new IllegalStateException("Less than two length is illegal");

        if (str.length() <= maxLen)
            return str;

        var words = str.split("\\s+");

        var curLen = 0;
        var sb = new StringBuilder();
        for (var word : words) {

            if (curLen + word.length() > maxLen) {
                var emptyLen = maxLen - curLen;
                var empty = " ".repeat(emptyLen);

                sb.append(empty).append("\n");
                curLen = 0;
            }

            sb.append(word).append(" ");
            curLen += word.length();
        }

        return sb.toString();
    }

    record Command(String name, String description, String example, Consumer<String[]> func) { }

}