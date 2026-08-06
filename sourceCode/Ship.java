public class Ship
{
    private int startX = 10;
    private int startY = 10;
    private final int length;
    private boolean horizontal;
    private boolean[] hits;
    private boolean placed = false;

    public Ship(int length)
    {
        this.length = length;
        this.hits = new boolean[length];
    }

    public void place(int x, int y, boolean horizontal)
    {
        this.startX = x;
        this.startY = y;
        this.horizontal = horizontal;
    }

    public boolean registerHit(int x, int y)
    {
        if(horizontal)
        {
            int offset = y - startY;
            if(offset >= 0 && offset < length)
            {
                hits[offset] = true;
                return true;
            }
        }
        else
        {
            int offset = x - startX;
            if(offset >= 0 && offset < length)
            {
                hits[offset] = true;
                return true;
            }
        }
        return false;
    }

    public boolean isSunk()
    {
        for (boolean b : hits)
        {
            if(!b) return false;
        }
        return true;
    }

    public int getLength() { return length; }

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public boolean isHorizontal() { return horizontal; }

    public boolean isPlaced() { return placed; }
    public void setPlaced(boolean placed) { this.placed = placed; }
}
