package ro.crownstudio;

import ro.crownstudio.config.CmdArgs;
import ro.crownstudio.config.MainConfig;
import ro.crownstudio.engine.rabbit.Scaler;

public class Main {
    public static void main(String[] args) {
        CmdArgs cmdArgs = new CmdArgs(args);
        MainConfig.getInstance().loadFromCmdArgs(cmdArgs);

        Scaler.scale();
    }
}