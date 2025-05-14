package tn.esprit.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import tn.esprit.models.Ressource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class QrCodeService {
    private static final String QR_CODE_DIR = "src/main/resources/qrcodes/";
    private static final int QR_CODE_SIZE = 300;

    public QrCodeService() {
        // Create QR code directory if it doesn't exist
        File directory = new File(QR_CODE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public String generateQrCode(Ressource ressource) throws WriterException, IOException {
        // Create JSON-like string with resource details
        String resourceInfo = String.format(
            "{\n" +
            "  \"id\": %d,\n" +
            "  \"nom\": \"%s\",\n" +
            "  \"categorie\": \"%s\",\n" +
            "  \"capacite\": %d,\n" +
            "  \"tarifHoraire\": %.2f,\n" +
            "  \"horaireOuverture\": \"%s\",\n" +
            "  \"horaireFermeture\": \"%s\",\n" +
            "  \"disponible\": %b,\n" +
            "  \"description\": \"%s\"\n" +
            "}",
            ressource.getId(),
            ressource.getNom(),
            ressource.getCategorie(),
            ressource.getCapacite(),
            ressource.getTarifHoraire(),
            ressource.getHoraireOuverture(),
            ressource.getHoraireFermeture(),
            ressource.isDisponible(),
            ressource.getDescription()
        );

        // Set QR code parameters
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        // Generate QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(resourceInfo, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        // Convert to BufferedImage
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        // Save QR code to file
        String fileName = String.format("resource_%d_qr.png", ressource.getId());
        String filePath = QR_CODE_DIR + fileName;
        ImageIO.write(qrImage, "PNG", new File(filePath));

        return filePath;
    }

    public Image loadQrCodeImage(String filePath) throws IOException {
        return new Image(new File(filePath).toURI().toString());
    }

    public Image getQrCodeForResource(Ressource ressource) {
        try {
            String filePath = String.format(QR_CODE_DIR + "resource_%d_qr.png", ressource.getId());
            File qrFile = new File(filePath);

            // Generate QR code if it doesn't exist
            if (!qrFile.exists()) {
                filePath = generateQrCode(ressource);
            }

            return loadQrCodeImage(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteQrCode(int resourceId) {
        try {
            Path path = Paths.get(QR_CODE_DIR + String.format("resource_%d_qr.png", resourceId));
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Image generateQrCodeImage(Ressource ressource) {
        try {
            // Set QR code parameters
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // Create JSON-like string with resource details
            String resourceInfo = String.format(
                "{\n" +
                "  \"id\": %d,\n" +
                "  \"nom\": \"%s\",\n" +
                "  \"categorie\": \"%s\",\n" +
                "  \"capacite\": %d,\n" +
                "  \"tarifHoraire\": %.2f,\n" +
                "  \"horaireOuverture\": \"%s\",\n" +
                "  \"horaireFermeture\": \"%s\",\n" +
                "  \"disponible\": %b,\n" +
                "  \"description\": \"%s\"\n" +
                "}",
                ressource.getId(),
                ressource.getNom(),
                ressource.getCategorie(),
                ressource.getCapacite(),
                ressource.getTarifHoraire(),
                ressource.getHoraireOuverture(),
                ressource.getHoraireFermeture(),
                ressource.isDisponible(),
                ressource.getDescription()
            );

            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(resourceInfo, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

            // Convert to BufferedImage
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Convert BufferedImage to JavaFX Image
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", outputStream);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            return new Image(inputStream);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 