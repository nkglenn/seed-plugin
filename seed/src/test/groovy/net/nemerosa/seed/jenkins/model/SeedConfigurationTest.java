package net.nemerosa.seed.jenkins.model;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;

public class SeedConfigurationTest {

    @Test
    public void parseYaml() throws IOException {
        String text = IOUtils.toString(
                getClass().getResourceAsStream("/test.yml"),
                "UTF-8"
        );
        SeedConfiguration.parseYaml(text);
    }

}