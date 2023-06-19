package ro.crownstudio.engine.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.crownstudio.config.MainConfig;
import ro.crownstudio.engine.docker.DockerServiceScaler;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Scaler {

    private static final Logger LOGGER = LogManager.getLogger(Scaler.class);

    private static final MainConfig CONFIG = MainConfig.getInstance();

    public static void scale() {
        try (Connection connection = RabbitConnector.getInstance().getFactory().newConnection()) {
            Channel channel = connection.createChannel();

            channel.queueDeclarePassive(CONFIG.getQueue());
            DockerServiceScaler serviceScaler = new DockerServiceScaler(CONFIG.getDockerServiceName());

            while (true) {
                int consumerCount = channel.queueDeclarePassive(CONFIG.getQueue()).getConsumerCount();
                int messageCount = channel.queueDeclarePassive(CONFIG.getQueue()).getMessageCount();
                int ratio = messageCount / consumerCount;
                LOGGER.info(
                        "Consumer count: %s | Message count: %s | ratio: %s".formatted(
                                consumerCount,
                                messageCount,
                                ratio
                        )
                );

                if (ratio > CONFIG.getThreshold() && consumerCount <= CONFIG.getMaxConsumers()) {
                    if (serviceScaler.scaleUp()) {
                        LOGGER.info("Scaled up service: %s".formatted(CONFIG.getDockerServiceName()));
                    }
                } else if (messageCount == 0 && consumerCount > 1) {
                    if (serviceScaler.scaleDown()) {
                        LOGGER.info("Scaled down service: %s".formatted(CONFIG.getDockerServiceName()));
                    }
                } else {
                    LOGGER.info("No action taken. Ratio is: " + ratio);
                }

                Thread.sleep(CONFIG.getPollingRate());
            }

        } catch (IOException e) {
            LOGGER.error("IOException while creating a new Rabbit Connection.", e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            LOGGER.error("Timeout while creating a new Rabbit Connection.", e);
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted while sleeping.", e);
            throw new RuntimeException(e);
        }
    }
}
