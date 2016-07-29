package net.nemerosa.jenkins.seed.test;

import net.nemerosa.jenkins.seed.SeedException;

public class JenkinsNotFoundException extends SeedException {

    public JenkinsNotFoundException(String path) {
        super("Not found (404): %s", path);
    }
}
