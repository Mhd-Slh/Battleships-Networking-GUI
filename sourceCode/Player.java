public class Player
{
    private String name;
    private Board board;
    private Ship[] ships;

    public Player(String name)
    {
        this.name = name;
        this.board = new Board();
    }

    public boolean allShipsSunk()
    {
        for (Ship s : ships)
        {
            if (!s.isSunk()) return false;
        }
        return true;
    }

    public boolean registerAttack(int x, int y)
    {
        boolean hit = board.getCell(x, y) == Board.CellState.SHIP;
        if (hit)
        {
            for (Ship s : ships)
            {
                s.registerHit(x, y);
            }
        }
        board.attack(x, y);
        return hit;
    }

//    public Ship getShipAt(int x, int y)
//    {
//        //add later
//    }

    public boolean hasLost() { return allShipsSunk(); }

    public void resetBoard() { board.clear(); }
    public String getName() { return name; }
    public Board getBoard() { return board; }
    public Ship[] getShips() { return ships; }
}
