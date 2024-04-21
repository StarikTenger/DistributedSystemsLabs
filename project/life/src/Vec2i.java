public class Vec2i {
    public int x;
    public int y;

    // Constructor
    public Vec2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Addition of vectors
    public Vec2i add(Vec2i other) {
        return new Vec2i(this.x + other.x, this.y + other.y);
    }

    // Subtraction of vectors
    public Vec2i sub(Vec2i other) {
        return new Vec2i(this.x - other.x, this.y - other.y);
    }

    // Scalar multiplication
    public Vec2i mult(int scalar) {
        return new Vec2i(this.x * scalar, this.y * scalar);
    }

    // Override toString() to display vector coordinates
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
