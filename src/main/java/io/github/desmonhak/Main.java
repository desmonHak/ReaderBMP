package io.github.desmonhak;

import io.github.desmonhak.BMP.BMP_Image;

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
        // Usar interpolación de alta calidad para mejor resultado
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ImageIO.write(resizedImage, "bmp", new File(outputPath));
    }

    static void main(String[] args) throws IOException {


        BMP_Image nino = BMP_Image.readBMPFile(args[0]);
        nino.printBMPAttributes();



        // redimensiono el BMP a una imagen de 600x600
        // Pagina para crear pixelarts de imagenes: https://pixelartvillage.com/
        // pagina para convertir png a bmp https://cloudconvert.com/png-to-bmp
        resizeBMP(args[0], "redimensionada_" + args[0], 60, 60);

        nino = BMP_Image.readBMPFile("redimensionada_" + args[0]);

        // leemos la imagen y obtemos los bytes de los pixeles
        JColorsTerm.dump_buffer_cli(nino.getDataWithoutPadding(), nino.header_bmp.width_img.get_int(), nino.header_bmp.height_img.get_int(), true, " ");


    }
}
