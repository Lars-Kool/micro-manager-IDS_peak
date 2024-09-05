package org.micromanager.plugins.MapPositions;

import org.apache.commons.math3.linear.*;

import static java.lang.Math.*;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;

public class MatrixTransformations {
    public double getMaxVal(double[] array) {
        double maxVal = array[0];
        for (int i = 1; i < array.length; i++) {
            maxVal = max(maxVal, array[i]);
        }
        return maxVal;
    }

    public RealMatrix unityTM() {
        double[][] temp = { {1, 0, 0}, {0, 1, 0}, {0, 0, 1} };
        return MatrixUtils.createRealMatrix(temp);
    }

    public RealMatrix rotateTM(RealMatrix TM, double angle) {
        double[][] temp = {
                { cos(angle), -sin(angle), 0},
                { sin(angle), cos(angle), 0},
                { 0, 0, 1}
        };
        RealMatrix rotationMatrix = MatrixUtils.createRealMatrix(temp);
        return TM.preMultiply(rotationMatrix);
    }

    public RealMatrix translateTM(RealMatrix TM, double dx, double dy) {
        double[][] temp = {
                {0, 0, dx},
                {0, 0, dy},
                {0, 0, 0}
        };
        RealMatrix translationMatrix = MatrixUtils.createRealMatrix(temp);
        return TM.add(translationMatrix);
    }

    public RealMatrix scaleTM(RealMatrix TM, double xScale, double yScale) {
        double[][] temp = {
                {xScale, 0, 0},
                {0, yScale, 0},
                {0, 0, 1}
        };
        RealMatrix scaleMatrix = MatrixUtils.createRealMatrix(temp);
        return TM.multiply(scaleMatrix);
    }

    public RealMatrix transposeTM(RealMatrix TM) {
        RealMatrix transposeMat = unityTM();
        transposeMat = scaleTM(transposeMat, -1, 1);
        transposeMat = rotateTM(transposeMat, -PI/2);
        return transposeMat.multiply(TM);
    }

    // Rolls matrix such that first column contains the right-angled corner
    public double[][] orientMatrix(double[][] data) {
        double dist_max = sqrt(pow(data[0][0] - data[0][1],2) + pow(data[1][0] - data[1][1],2));
        int origin = 0;
        for (int i = 0; i < 3; i++) {
            double dist = sqrt(pow(data[0][(i+1)%3] - data[0][(i+2)%3],2) + pow(data[1][(i+1)%3] - data[1][(i+2)%3],2));
            if (dist < dist_max) {
                dist_max = dist;
                origin = i;
            }
        }
        if (origin != 0) {
            double temp = 0;
            for (int i = 0; i < 3; i++) {
                temp = data[i][0];
                data[i][0] = data[i][origin];
                data[i][origin] = temp;
            }
        }
        return data;
    }

    public double[][] transform(double[][] data, RealMatrix TM) {
        return TM.multiply(MatrixUtils.createRealMatrix(data)).getData();
    }

    public static double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    public double distance(double[] point1, double[] point2) {
        return sqrt(pow(point1[0]-point2[0],2) + pow(point1[1]-point2[1],2));
    }

    public double[][] rollMatrix(double[][] m, int n) {
        if (n == 0) { return m; }
        if (n < 0) { n = m.length + n; }

        double[][] rolled = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            rolled[(i+n)%m.length] = m[i];
        }
        return rolled;
    }

    public double[][] standardizeMatrixOrder(double[][] matrix) {
        double[][] transposed = transposeMatrix(matrix);
        double distMax = distance(transposed[1], transposed[2]);
        int idx = 0;
        for (int i = 1; i < 3; i++) {
            double dist = distance(transposed[(i+1)%3], transposed[(i+2)%3]);
            if (dist > distMax) {
                distMax = dist;
                idx = i;
            }
        }
        transposed = rollMatrix(transposed, -idx);
        if (distance(transposed[0], transposed[2]) > distance(transposed[0], transposed[1])) {
            double[] temp = transposed[1];
            transposed[1] = transposed[2];
            transposed[2] = temp;
        }
        return transposeMatrix(transposed);
    }

    public RealMatrix remap(double[][] alignmentMatrix, double[][] targetMatrix) {
        alignmentMatrix = standardizeMatrixOrder(alignmentMatrix);
        targetMatrix = standardizeMatrixOrder(targetMatrix);
        RealMatrix coefficients = createRealMatrix(new double[][]{
                {alignmentMatrix[0][0], alignmentMatrix[1][0], 1},
                {alignmentMatrix[0][1], alignmentMatrix[1][1], 1},
                {alignmentMatrix[0][2], alignmentMatrix[1][2], 1}
        });
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        RealVector constantsX = new ArrayRealVector(targetMatrix[0]);
        RealVector solutionX = solver.solve(constantsX);

        RealVector constantsY = new ArrayRealVector(targetMatrix[1]);
        RealVector solutionY = solver.solve(constantsY);

        return createRealMatrix(new double[][]{
                solutionX.toArray(),
                solutionY.toArray(),
                {0, 0, 1}
        }
        );
    }
}
