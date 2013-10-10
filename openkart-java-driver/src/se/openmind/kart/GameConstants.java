package se.openmind.kart;

/**
 * A few constants that defines how the game mechanics work
 */
public class GameConstants {
	// Size of arena
	public static final double MinBoundX = 0.0;
	public static final double MaxBoundX = 100.0;
	public static final double MinBoundY = 0.0;
	public static final double MaxBoundY = 100.0;
	
	/**
	 * Distance traveled in one second
	 */
	public static final double KartSpeed = 10.0;
	/**
	 * Radians turned in one second
	 */
	public static final double KartTurnSpeed = Math.PI / 2;
	/**
	 * Maximum number of shells for a Kart
	 */
	public static final int KartMaxShells = 5;
	
	
	/**
	 * Distance traveled in one second
	 */
	public static final double ShellSpeed = 30.0;
	/**
	 * Radians turned in one second
	 */
	public static final double ShellTurnSpeed = Math.PI / 2;
	/**
	 * Seconds before a shell despawns (disappears)
	 */
	public static final double ShellMaxTime = 1.0;
	
	/**
	 * Seconds stunned after being hit by a shell
	 */
	public static final double ShellStunTime = 2.0;
	/**
	 * Seconds invulnerable after being hit by a shell
	 */
	public static final double ShellInvulnerableTime = 5.0;
	
	/**
	 * Number of itemboxes in the arena at any given time
	 */
	public static final int ItemBoxCount = 10;
}
