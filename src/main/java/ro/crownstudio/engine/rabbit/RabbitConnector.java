package ro.crownstudio.engine.rabbit;

import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.crownstudio.config.MainConfig;

public class RabbitConnector {
    
    private final static Logger LOGGER = LogManager.getLogger(RabbitConnector.class);
    private final static MainConfig CONFIG = MainConfig.getInstance();
    private static RabbitConnector INSTANCE;
    
    @Getter
    private final ConnectionFactory factory;
    
    private RabbitConnector() {
        factory = new ConnectionFactory();
        factory.setHost(CONFIG.getRabbitHost());
        factory.setUsername(CONFIG.getRabbitUser());
        factory.setPassword(CONFIG.getRabbitPass());
        
        LOGGER.debug("Successfully created Rabbit ConnectionFactory.");
    }
    
    public static RabbitConnector getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RabbitConnector();
        }

        return INSTANCE;
    }
}
