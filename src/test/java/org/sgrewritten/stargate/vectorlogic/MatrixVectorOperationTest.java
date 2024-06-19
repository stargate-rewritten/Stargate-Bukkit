package org.sgrewritten.stargate.vectorlogic;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sgrewritten.stargate.api.vectorlogic.MatrixVectorOperation;
import org.sgrewritten.stargate.api.vectorlogic.VectorOperation;
import org.sgrewritten.stargate.exception.InvalidStructureException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MatrixVectorOperationTest {

    private static VectorOperationTester vectorOperationTester;

    @BeforeAll
    public static void setUp() {
        List<Vector> testVectors = new ArrayList<>();
        Random random = new Random();
        int maxBound = 2000;
        for (int i = 0; i < 10000; i++) {
            testVectors.add(new Vector(random.nextInt(maxBound), -random.nextInt(maxBound), random.nextInt(maxBound)));
        }
        vectorOperationTester = new VectorOperationTester(testVectors);
    }

    @Test
    void noRotationForEastTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST);
        vectorOperationTester.noRotationForEastTest(operation);
    }

    @Test
    void originalVectorNotModifiedTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.originalVectorNotModifiedTest(operation);
    }

    @Test
    void rotateSouthTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.rotateSouthTest(operation);
    }

    @Test
    void rotateWestTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST);
        vectorOperationTester.rotateWestTest(operation);
    }

    @Test
    void rotateNorthTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH);
        vectorOperationTester.rotateNorthTest(operation);
    }

    @Test
    void rotateEastInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    void rotateWestInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    void rotateSouthInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    void rotateNorthInverseTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH);
        vectorOperationTester.inverseOperationTest(operation);
    }

    @Test
    void runningWestOperationTwiceGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST);
        vectorOperationTester.runningOperationTwiceGivesInitialTest(operation);
    }

    @Test
    void runningSouthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    void runningNorthOperationFourTimesGivesInitialTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH);
        vectorOperationTester.runningOperationFourTimesGivesInitialTest(operation);
    }

    @Test
    void rotateEastFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.EAST);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    void rotateWestFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.WEST);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    void rotateNorthFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.NORTH);
        vectorOperationTester.flipTest(operation);
    }

    @Test
    void rotateSouthFlipZTest() throws InvalidStructureException {
        VectorOperation operation = new MatrixVectorOperation(BlockFace.SOUTH);
        vectorOperationTester.flipTest(operation);
    }

}
