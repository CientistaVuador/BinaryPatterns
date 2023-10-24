package cientistavuador.binarypatterns;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 *
 * @author Cien
 */
public class Main {

    private static final PatternGenerator[] generators = new PatternGenerator[]{
        new DefaultPattern(),
        new BitwiseAndPattern(),
        new BitwiseOrPattern(),
        new BitwiseXorPattern()
    };
    
    private static final String generatorsNames;

    static {
        StringBuilder b = new StringBuilder();
        b.append('<');
        for (int i = 0; i < generators.length; i++) {
            PatternGenerator e = generators[i];
            b.append(e.getName());
            if (i != (generators.length - 1)) {
                b.append('/');
            }
        }
        b.append('>');
        generatorsNames = b.toString();
    }

    private static void printUsage() {
        System.out.println("usage: <clamped/normalized> " + generatorsNames + " <input file>");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            if (args.length != 0) {
                System.out.println("Invalid amount of arguments.");
            }
            printUsage();
            return;
        }

        //clamped or normalized
        boolean clamped;
        switch (args[0].toLowerCase()) {
            case "clamped" -> {
                clamped = true;
            }
            case "normalized" -> {
                clamped = false;
            }
            default -> {
                System.out.println("Invalid output option: " + args[0]);
                printUsage();
                return;
            }
        }

        //generator
        PatternGenerator generator = null;
        for (PatternGenerator e : generators) {
            if (e.getName().equals(args[1])) {
                generator = e;
                break;
            }
        }
        if (generator == null) {
            System.out.println("Invalid pattern generator name: " + args[1]);
            printUsage();
            return;
        }

        //file
        Path path = Path.of(args[2]);
        if (Files.notExists(path)) {
            System.out.println(args[2] + " does not exist.");
            printUsage();
            return;
        }
        if (!Files.isRegularFile(path)) {
            System.out.println(args[2] + " is not a valid file.");
            printUsage();
            return;
        }

        //pattern generation
        System.out.println("Generating " + generator.getName() + " pattern for " + path.toString() + "...");
        long[] outputPattern = new long[256 * 256];

        try (InputStream input = Files.newInputStream(path)) {
            int patternSize = generator.getPatternSize();

            byte[] patternData = new byte[patternSize];
            int[] pointBuffer = new int[2];

            byte[] buffer = new byte[16384];
            int bufferOffset = 0;
            int bufferRead;
            while (true) {
                bufferRead = input.read(buffer, bufferOffset, buffer.length - bufferOffset);
                boolean eof = (bufferRead == -1);
                if (bufferRead == 0) {
                    continue;
                }
                if (eof) {
                    if (bufferOffset == 0) {
                        break;
                    }
                    bufferRead = 0;
                }
                int numberOfOperations = (bufferRead + bufferOffset) / patternSize;
                for (int i = 0; i < numberOfOperations; i++) {
                    int offset = i * patternSize;
                    System.arraycopy(buffer, offset, patternData, 0, patternSize);
                    generator.generatePointPosition(pointBuffer, patternData);
                    outputPattern[pointBuffer[0] + (pointBuffer[1] * 256)]++;
                }
                int copyOffset = numberOfOperations * patternSize;
                int leftover = (bufferRead + bufferOffset) % patternSize;
                System.arraycopy(buffer, copyOffset, buffer, 0, leftover);
                bufferOffset = leftover;
                if (eof) {
                    for (int i = bufferOffset; i < patternSize; i++) {
                        buffer[i] = buffer[bufferOffset - 1];
                    }
                    System.arraycopy(buffer, 0, patternData, 0, patternSize);
                    generator.generatePointPosition(pointBuffer, patternData);
                    outputPattern[pointBuffer[0] + (pointBuffer[1] * 256)]++;
                    break;
                }

            }
        }

        //clamping/normalizing
        if (clamped) {
            System.out.println("clamping pattern...");
        } else {
            System.out.println("normalizing pattern...");
        }
        long biggestValue = 255;
        if (!clamped) {
            biggestValue = 0;
            for (int i = 0; i < outputPattern.length; i++) {
                biggestValue = Math.max(biggestValue, outputPattern[i]);
            }
        }
        float[] processedPattern = new float[256 * 256];
        if (biggestValue != 0) {
            for (int i = 0; i < processedPattern.length; i++) {
                double value = Math.max(Math.min(outputPattern[i] / (double) (biggestValue), 1.0), 0.0);
                processedPattern[i] = (float) value;
            }
        }
        
        //outputing image
        System.out.println("Generating image...");
        String outputFilename = path.getFileName()+"_"+generator.getName()+".png";
        Path outputFile;
        if (path.getParent() != null) {
            outputFile = path.getParent().resolve(outputFilename);
        } else {
            outputFile = Path.of(outputFilename);
        }
        System.out.println(outputFile.toString());
        BufferedImage outputImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < 256 * 256; i++) {
            int value = (int) (processedPattern[i] * 255);
            int x = i % 256;
            int y = i / 256;
            int argb = (255 << 24) | (value << 16) | (value << 8) | (value << 0);
            outputImage.setRGB(x, y, argb);
        }
        ImageIO.write(outputImage, "PNG", outputFile.toFile());
        
        
        System.out.println("Done.");
    }

}
