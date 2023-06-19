package ro.crownstudio.engine.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.UpdateServiceCmd;
import com.github.dockerjava.api.model.Service;
import com.github.dockerjava.api.model.ServiceReplicatedModeOptions;
import com.github.dockerjava.api.model.ServiceSpec;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.crownstudio.config.MainConfig;

import java.time.Duration;
import java.util.List;

public class DockerServiceScaler {

    private static final Logger LOGGER = LogManager.getLogger(DockerServiceScaler.class);

    private static final MainConfig CONFIG = MainConfig.getInstance();

    private final String serviceName;
    private final DockerClientConfig config;

    public DockerServiceScaler(String serviceName) {
        this.serviceName = serviceName;
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(CONFIG.getDockerHost())
                .build();
    }

    public boolean  scaleUp() {
        try (
                DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofSeconds(30))
                        .responseTimeout(Duration.ofSeconds(30))
                        .build();
                DockerClient client = DockerClientBuilder.getInstance(config)
                        .withDockerHttpClient(httpClient)
                        .build()
        ) {
            List<Service> services = client.listServicesCmd().withNameFilter(List.of(serviceName)).exec();
            if (services.isEmpty()) {
                LOGGER.error("Service not found: {}", serviceName);
                return false;
            }
            Service service = services.get(0);

            ServiceSpec serviceSpec = service.getSpec();
            ServiceReplicatedModeOptions replicatedModeOptions = serviceSpec.getMode().getReplicated();
            long replicas = replicatedModeOptions.getReplicas();

            replicatedModeOptions.withReplicas((int) replicas + 1);

            UpdateServiceCmd updateServiceCmd = client.updateServiceCmd(service.getId(), serviceSpec)
                    .withVersion(service.getVersion().getIndex());

            updateServiceCmd.exec();

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Docker.", e);
            return false;
        }
    }

    public boolean scaleDown() {
        try (
                DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                        .dockerHost(config.getDockerHost())
                        .maxConnections(100)
                        .connectionTimeout(Duration.ofSeconds(30))
                        .responseTimeout(Duration.ofSeconds(30))
                        .build();
                DockerClient client = DockerClientBuilder.getInstance(config)
                        .withDockerHttpClient(httpClient)
                        .build()
        ) {            List<Service> services = client.listServicesCmd().withNameFilter(List.of(serviceName)).exec();
            if (services.isEmpty()) {
                LOGGER.error("Service not found: {}", serviceName);
                return false;
            }
            Service service = services.get(0);

            ServiceSpec serviceSpec = service.getSpec();
            ServiceReplicatedModeOptions replicatedModeOptions = serviceSpec.getMode().getReplicated();
            long replicas = replicatedModeOptions.getReplicas();

            replicatedModeOptions.withReplicas(replicas <= 1 ? 1 : (int) replicas - 1);

            UpdateServiceCmd updateServiceCmd = client.updateServiceCmd(service.getId(), serviceSpec)
                    .withVersion(service.getVersion().getIndex());

            updateServiceCmd.exec();

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Docker.", e);
            return false;
        }
    }
}
