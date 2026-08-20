package net.pl3x.livemap.world;

/**
 * Represents a pair of coordinates.
 */
public class Coordinates {
    // @formatter:off
    /**
     * Coordinates to the north and west
     */
    public static final Coordinates NORTHWEST = new Coordinates(-1, -1);
    /**
     * Coordinates to the north
     */
    public static final Coordinates NORTH     = new Coordinates( 0, -1);
    /**
     * Coordinates to the north and east
     */
    public static final Coordinates NORTHEAST = new Coordinates( 1, -1);
    /**
     * Coordinates to the east
     */
    public static final Coordinates EAST      = new Coordinates( 1,  0);
    /**
     * Coordinates to the south and east
     */
    public static final Coordinates SOUTHEAST = new Coordinates( 1,  1);
    /**
     * Coordinates to the south
     */
    public static final Coordinates SOUTH     = new Coordinates( 0,  1);
    /**
     * Coordinates to the south and west
     */
    public static final Coordinates SOUTHWEST = new Coordinates(-1,  1);
    /**
     * Coordinates to the west
     */
    public static final Coordinates WEST      = new Coordinates(-1,  0);
    // @formatter:on

    private final int x;
    private final int z;

    /**
     * Constructs a new instance of Coordinates.
     *
     * @param x X coordinate
     * @param z Z coordinate
     */
    public Coordinates(int x, int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Get X coordinate.
     *
     * @return X coordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get Z coordinate.
     *
     * @return Z coordinate
     */
    public int getZ() {
        return this.z;
    }
}
