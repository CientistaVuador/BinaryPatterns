package cientistavuador.binarypatterns;

/**
 *
 * @author Cien
 */
public interface PatternGenerator {
    public String getName();
    public int getPatternSize();
    public void generatePointPosition(int[] position, byte[] patternData);
}
