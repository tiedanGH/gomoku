package main.utils;

public class Score {

    public int one;
    public int two;

    public Score() {
        this.one = 0;
        this.two = 0;
    }

    public int evaluate(int player) {
        if (player == 1)
            return one - two;
        else
            return two - one;
    }

    public void reset() {
        one = 0;
        two = 0;
    }
}