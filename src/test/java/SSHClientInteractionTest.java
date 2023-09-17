import com.jcraft.jsch.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ishell.SSHClientInteraction;

public class SSHClientInteractionTest {

    private static JSch jsch;
    private static Session session;
    private static SSHClientInteraction sshClient;

    @BeforeAll
    public static void setUp() {
        try {
            // Initialize JSch and create an SSH session
            jsch = new JSch();
            session = jsch.getSession("username", "host", 22);
            session.setPassword("password");

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // Create SSHClientInteraction instance
            sshClient = new SSHClientInteraction(session);
            // Connect to the SSH server
            sshClient.connect();
        } catch (com.jcraft.jsch.JSchException | java.io.IOException e) {
            // Handle exceptions (log or throw as needed)
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void tearDown() {
        // Disconnect the SSH session
        session.disconnect();
    }

    @Test
    public void testExpect() throws Exception {
        // Execute a command on the remote server
        sshClient.send("echo 'Hello, World!'");

        // Wait for the output and expect the specific pattern
        String result = sshClient.expect("Hello, World!");
        assertEquals("Hello, World!\n", result);
    }

    @Test
    public void testFileUploadAndDownload() throws Exception {
        // Create a temporary local file for testing
        File localFile = File.createTempFile("test-file", ".txt");
        String localFilePath = localFile.getAbsolutePath();

        // Define the remote file path on the server
        String remoteFilePath = "/tmp/test-file.txt"; // You can adjust the path as needed
        String downloadedFilePath = "downloaded-file.txt"; // Declare it here

        try {
            // Write some content to the local file
            String content = "Test file content";
            Files.write(Paths.get(localFilePath), content.getBytes());

            // Upload the local file to the remote server
            sshClient.file(localFilePath, remoteFilePath, true);

            // Download the file from the remote server to a different local path
            sshClient.file(remoteFilePath, downloadedFilePath, false);

            // Read the downloaded file and assert its content
            String downloadedContent = new String(Files.readAllBytes(Paths.get(downloadedFilePath)));
            assertEquals(content, downloadedContent);
        } finally {
            // Clean up: delete the local and remote files created during testing
            Files.deleteIfExists(Paths.get(localFilePath));
            sshClient.send("rm " + remoteFilePath);
            Files.deleteIfExists(Paths.get(downloadedFilePath));
        }
    }

    @Test
    public void testTail() throws Exception {
        // Execute a tail command on a file and capture the output
        String remoteFilePath = "/tmp/tail-file.txt";
        sshClient.send("echo 'Line 1' > " + remoteFilePath);
        sshClient.send("echo 'Line 2' >> " + remoteFilePath);

        try {
            // Start tailing the file
            Thread tailingThread = new Thread(() -> {
                try {
                    sshClient.tail("tail -f " + remoteFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            tailingThread.start();

            // Wait for some time to allow tailing to start
            Thread.sleep(2000);

            // Append a new line to the remote file
            sshClient.send("echo 'Line 3' >> " + remoteFilePath);

            // Wait for tailing to capture the new line
            Thread.sleep(2000);

            // Stop tailing thread
            tailingThread.interrupt();
        } finally {
            // Clean up: delete the remote file created during testing
            sshClient.send("rm " + remoteFilePath);
        }
    }
}