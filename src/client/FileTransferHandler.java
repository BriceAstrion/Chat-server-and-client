package client;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import static shared.Constant.*;

public class FileTransferHandler {


    private Socket socket;


    private File file = new File("resources/file.txt");


    public FileTransferHandler() {}

    /**
     * Initiates the file.txt transfer process.
     *
     * @param uuid The recipient of the file.txt.
     */

    public void FileTransferstart(UUID uuid) {
        new Thread(() -> {
            try {
                // Connect to the file.txt transfer server
                socket = new Socket( "127.0.0.1", 1338);

                // Send the file.txt content to the server
               sendFileContent(uuid.toString());

                // Close the file.txt transfer socket
                socket.close();
            } catch (IOException e) {
                System.err.println("Exception during file.txt transfer initiation: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

    }


    private void sendFileContent(String uuid) {
        String filePath = file.getPath();
        try (FileInputStream fileInputStream = new FileInputStream(file);
             OutputStream outputStream = socket.getOutputStream()) {

            // Include sender/receiver indicator, UUID, and checksum in the file.txt content
            outputStream.write('S');
            outputStream.write(uuid.getBytes());
            String fileExtension = Optional.of(filePath)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filePath.lastIndexOf(".") + 1)).orElse("");
            if (fileExtension.length() != 3) {
                fileExtension = "txt";
            }
            outputStream.write(fileExtension.getBytes());
            System.out.println("UUID:" + uuid + " " + uuid.getBytes());
            System.out.println("File ext:" + fileExtension);
            String checksum = calculateMD5Checksum(file);
            System.out.println("Checksum: " + checksum);
            System.out.println("Checksum length: " + checksum.length());
         //   outputStream.write(checksum.length());
            outputStream.write(checksum.getBytes());

            // Send file content
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            long bytesTransferred = fileInputStream.transferTo(socket.getOutputStream());
            outputStream.close();
            fileInputStream.close();
            System.out.println("File Transfer Complete. Bytes Transferred: " + bytesTransferred);

        } catch (IOException e) {
            System.err.println("Exception during file.txt content transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String calculateMD5Checksum(File file) throws IOException {
        //  use a way to generate a checksum using the FileStream
        try (FileInputStream fis = new FileInputStream(file); FileChannel channel = fis.getChannel()) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            ByteBuffer buffer = ByteBuffer.allocate(8192);

            while (channel.read(buffer) > 0) {
                buffer.flip();
                md.update(buffer);
                buffer.clear();
            }

            byte[] hash = md.digest();

            return String.format("%032x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }









}
