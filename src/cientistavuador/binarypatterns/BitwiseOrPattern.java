package cientistavuador.binarypatterns;

/**
 *
 * @author Cien
 */
public class BitwiseOrPattern implements PatternGenerator {

    @Override
    public String getName() {
        return "bitwise_or";
    }

    @Override
    public int getPatternSize() {
        return 4;
    }

    @Override
    public void generatePointPosition(int[] position, byte[] patternData) {
        position[0] = Byte.toUnsignedInt((byte) (patternData[0] | patternData[1]));
        position[1] = Byte.toUnsignedInt((byte) (patternData[2] | patternData[3]));
    }
    
}
