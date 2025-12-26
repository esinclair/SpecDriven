package com.example.specdriven.acceptance;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpecFilesPresenceTest {

    @Test
    void specFilesShouldExist() {
        Path base = Path.of("spec");
        assertTrue(Files.exists(base.resolve("domain.yaml")), "spec/domain.yaml must exist");
        assertTrue(Files.exists(base.resolve("api.yaml")), "spec/api.yaml must exist");
        assertTrue(Files.exists(base.resolve("behaviors/create_item.feature")), "feature spec must exist");
    }
}

