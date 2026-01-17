package io.github.desmonhak;

import io.github.desmonhak.BMP.BMP_Image;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void resizeBMP(String inputPath, String outputPath, int newWidth, int newHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(inputPath));
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ImageIO.write(resizedImage, "BMP", new File(outputPath));
    }

    public static String ensureBMPFormat(String inputPath, boolean keepTemp) throws IOException {
        File inputFile = new File(inputPath);
        String fileName = inputFile.getName();
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".bmp")) {
            return inputPath;
        }

        String baseName = fileName.replaceFirst("[.][^.]+$", "");
        String bmpPath;

        if (keepTemp) {
            bmpPath = baseName + "_converted.bmp";
        } else {
            bmpPath = "temp_" + baseName + ".bmp";
        }

        File bmpFile = new File(bmpPath);

        BufferedImage image = ImageIO.read(inputFile);
        if (image != null) {
            ImageIO.write(image, "BMP", bmpFile);
            System.out.println("Convertido " + fileName + " a " + bmpPath);
            return bmpPath;
        } else {
            throw new IOException("No se pudo leer la imagen: " + inputPath);
        }
    }

    public static void printHelp() {
        System.out.println("Uso: ReaderBMP <archivo_imagen> [opciones]");
        System.out.println();
        System.out.println("Opciones:");
        System.out.println("  -h, --help           Muestra esta ayuda");
        System.out.println("  -k                   Mantiene BMP convertido");
        System.out.println("  -s                   Mantiene imagen redimensionada");
        System.out.println("  -r WIDTHxHEIGHT      Tamaño personalizado (ej: -r 64x64, -r 32x32)");
        System.out.println();
        System.out.println("Ejemplos:");
        System.out.println("  ReaderBMP.exe img.jpg");
        System.out.println("  ReaderBMP.exe img.jpg -k");
        System.out.println("  ReaderBMP.exe img.jpg -r 32x32");
        System.out.println("  ReaderBMP.exe img.jpg -k -s -r 64x64");
    }

    public static void main(String[] args) {
        if (args.length == 0 || hasHelpFlag(args)) {
            printHelp();
            return;
        }

        boolean keepConverted = false;
        boolean keepResized = false;
        String imagePath = null;
        String resizeSpec = null;
        int newWidth = 64;
        int newHeight = 64;

        // Parsear argumentos
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("-k".equals(arg)) {
                keepConverted = true;
            } else if ("-s".equals(arg)) {
                keepResized = true;
            } else if ("-r".equals(arg) && i + 1 < args.length) {
                resizeSpec = args[++i];
                String[] parts = resizeSpec.split("x");
                if (parts.length == 2) {
                    try {
                        newWidth = Integer.parseInt(parts[0]);
                        newHeight = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Tamaño inválido '" + resizeSpec + "'");
                        return;
                    }
                } else {
                    System.out.println("Error: Usa -r WIDTHxHEIGHT (ej: -r 64x64)");
                    return;
                }
            } else {
                imagePath = arg;
            }
        }

        if (imagePath == null) {
            System.out.println("Error: Falta especificar archivo de imagen");
            printHelp();
            return;
        }

        try {
            String bmpPath = ensureBMPFormat(imagePath, keepConverted);
            BMP_Image nino = BMP_Image.readBMPFile(bmpPath);
            nino.printBMPAttributes();

            String resizedPath = "redimensionada.bmp";
            resizeBMP(bmpPath, resizedPath, newWidth, newHeight);

            nino = BMP_Image.readBMPFile(resizedPath);
            JColorsTerm.dump_buffer_cli(nino.getDataWithoutPadding(),
                    nino.header_bmp.width_img.get_int(),
                    nino.header_bmp.height_img.get_int(),
                    true, " ");

            // Limpiar archivos según flags
            if (!keepConverted && !bmpPath.equals(imagePath)) {
                new File(bmpPath).delete();
                System.out.println("BMP temporal eliminado: " + bmpPath);
            }

            if (!keepResized) {
                new File(resizedPath).delete();
                System.out.println("Imagen redimensionada eliminada: " + resizedPath);
            }

            System.out.println("Redimensionado a: " + newWidth + "x" + newHeight);

        } catch (IIOException e) {
            System.out.println("El archivo " + imagePath + " no pudo encontrarse o no existe.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean hasHelpFlag(String[] args) {
        for (String arg : args) {
            if ("-h".equals(arg) || "--help".equals(arg)) {
                return true;
            }
        }
        return false;
    }
}
