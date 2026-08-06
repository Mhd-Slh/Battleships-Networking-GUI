public class Ship
{
    private final int length;
    private int row = -1;
    private int col = -1;
    private boolean horizontal = false;
    private boolean placed = false;
    private boolean[] hits;

    public Ship(int length)
    {
        this.length = length;
        this.hits = new boolean[length];
    }

    public int getLength() { return length; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isHorizontal() { return horizontal; }
    public boolean isPlaced() { return placed; }

    public void setPlaced(boolean placed) { this.placed = placed; }

    public void place(int row, int col, boolean horizontal)
    {
        this.row = row;
        this.col = col;
        this.horizontal = horizontal;
        this.placed = true;
        this.hits = new boolean[length];
    }

    public void registerHit(int r, int c) {
        if (horizontal){
            int offset = c - col;
            if (offset >= 0 && offset < length){
                hits[offset] = true;
            }
        }
        else {
            int offset = r - row;
            if (offset >= 0 && offset < length){
                hits[offset] = true;
            }
        }
    }

    public boolean isSunk(){
        for(boolean hit : hits) {
            if (!hit) return false;
        }
        return true;
    }

}
