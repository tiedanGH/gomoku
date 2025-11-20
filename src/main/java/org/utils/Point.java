package org.utils;

public class Point {
    public int x;
    public int y;
    public int color;
    public float val;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        this.color = 0;
    }

    public Point(int x, int y, int c)
    {
        this.x = x;
        this.y = y;
        this.color = c;
    }

    @Override
    public String toString() {
        return "( x: " + x + ", y: " + y + ")" + ", val: " + val;
    }

    public String colorMove() {
        String C = this.color == 1 ? "B" : "W";
        if (color == 0)
            return "(" + x + "," + y + ")";
        return C + "(" + x + "," + y + ")";
    }

    public void setValue(float v){
        val = v;
    }

    public float getValue(){
        return val;
    }

    public Point()
    {
        this.x = 0;
        this.y = 0;
    }
}
