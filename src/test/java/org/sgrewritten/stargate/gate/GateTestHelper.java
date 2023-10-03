package org.sgrewritten.stargate.gate;

import org.sgrewritten.stargate.FakeStargateLogger;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.gate.GateFormatHandler;

import java.io.File;
import java.util.Objects;

public class GateTestHelper {

    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");

    public static void setUpGates() {
        GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
    }

}
