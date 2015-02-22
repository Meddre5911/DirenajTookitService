package direnaj.functionalities.sna.communityDetection;

import org.apache.commons.math3.FieldElement;
import org.apache.commons.math3.linear.Array2DRowFieldMatrix;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealVector;

public class MatrixDeneme {

    public static <T> void main(String[] args) {
        double[][] matrixData = { { 1d, 2d, 3d }, { 2d, 5d, 3d } };
        RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

        m.setEntry(1, 1, 9d);
        double entry = m.getEntry(1, 1);
        System.out.println("Entry is " + entry);
        RealMatrix columnMatrix = m.getColumnMatrix(1);

        RealVector rowVector = m.getRowVector(1);

        //        RealVector newOne;
        //        RealVector add;
        //        newOne = rowVector.getSubVector(0, 0);
        //        add = newOne;
        //         add = newOne.append(rowVector.getSubVector(2, 1));
        //        System.out.println("New One : " + add);
        //        System.out.println("Row vector : " + rowVector);
        System.out.println(m);

        //        RealMatrix createRealMatrix = new Array2DRowRealMatrix(2,2);
        //        createRealMatrix.setRowVector(1, add);
        //        System.out.println("Son : " + createRealMatrix);

        System.out.println("Column Dimension : " + m.getColumnDimension());
        RealMatrixPreservingVisitor visitor = new DefaultRealMatrixPreservingVisitor();

        m.walkInRowOrder(new DefaultRealMatrixPreservingVisitor() {
            @Override
            public void visit(int row, int column, double value) {
                // TODO Auto-generated method stub
                super.visit(row, column, value);
                System.out.println(value);
            }
        }, 0, 0, 0, 2);
    }

    //    walkInRowOrder(RealMatrixChangingVisitor visitor, int startRow, int endRow, int startColumn, int endColumn) 
}
