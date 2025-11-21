package main.utils;

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

    @Override
    public String toString() {
        return "( x: " + x + ", y: " + y + ")" + ", val: " + val;
    }

    public void setValue(float v){
        val = v;
    }

    public float getValue(){
        return val;
    }
}
