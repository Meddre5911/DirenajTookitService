package direnaj.util.matrix;

public class MatrixElement {
    // matrix coordinates
    private int x;
    private int y;
    // matrix value
    private double value;

    public MatrixElement(int x, int y, double value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Element " + x + "," + y + " - " + value;
    }

}
