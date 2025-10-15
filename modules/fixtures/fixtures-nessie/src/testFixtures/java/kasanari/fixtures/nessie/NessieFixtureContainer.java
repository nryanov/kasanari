package kasanari.fixtures.nessie;

import org.projectnessie.testing.nessie.ImmutableNessieConfig;
import org.projectnessie.testing.nessie.NessieContainer;

public class NessieFixtureContainer {
    private final NessieContainer nessie = new NessieContainer(
            ImmutableNessieConfig
                    .builder()
                    .dockerImage("ghcr.io/projectnessie/nessie")
                    .dockerTag("0.104.3")
                    .build()
    );

    NessieContainer getNessie() {
        if (!nessie.isRunning()) {
            throw new RuntimeException("Nessie is not yet started");
        }
        return nessie;
    }

    public void start() {
        nessie.start();
    }

    public void stop() {
        nessie.stop();
    }

    public String url() {
        return nessie.getExternalNessieUri().toString();
    }
}
