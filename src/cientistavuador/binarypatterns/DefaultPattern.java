package cientistavuador.binarypatterns;

/**
 *
 * @author Cien
 */
public class DefaultPattern implements PatternGenerator {

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public int getPatternSize() {
        return 2;
    }

    @Override
    public void generatePointPosition(int[] position, byte[] patternData) {
        position[0] = Byte.toUnsignedInt(patternData[0]);
        position[1] = Byte.toUnsignedInt(patternData[1]);
    }
    
}
