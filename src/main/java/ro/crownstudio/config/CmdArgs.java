package ro.crownstudio.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class CmdArgs {

    private final static Logger LOGGER = LogManager.getLogger(CmdArgs.class);

    @Parameter(names = {"--rabbitHost"}, description = "The hostname for RabbitMQ", required = true)
    private String rabbitHost;
    @Parameter(names = {"--rabbitUser"}, description = "Username for RabbitMQ")
    private String rabbitUser;
    @Parameter(names = {"--rabbitPass"}, description = "Password for RabbitMQ", password = true)
    private String rabbitPass;

    @Parameter(names = {"--queue", "-q"}, description = "The queue to listen for consumers and messages", required = true)
    private String queue;
    @Parameter(names = {"--pollingRate", "-p"}, description = "How often to check for messages or consumers", required = true)
    private long pollingRate;

    @Parameter(names = {"--dockerHost"}, description = "Docker's host and port")
    private String dockerHost;
    @Parameter(names = {"--dockerServiceName"}, description = "Name of the service that needs to be scaled")
    private String dockerServiceName;
    @Parameter(names = {"--threshold"}, description = "Consumers / Messages ratio threshold when to scale up")
    private int threshold;
    @Parameter(names = {"--maxConsumers"}, description = "Max number of rabbit consumers")
    private int maxConsumers;

    public CmdArgs(String[] args) {
        JCommander.newBuilder().addObject(this).build().parse(args);
        LOGGER.debug("Created new instance for CmdArgs with args.");
    }
}
