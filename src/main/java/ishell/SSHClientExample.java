package ishell;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;

public class SSHClientExample {
    public static void main(String[] args) {
        String host = "host";  // Replace with your SSH server's hostname or IP address
        int port = 22;               // SSH server port (default is 22)
        String username = "username";
        String password = "password";

        try {
            JSch jsch = new JSch();

            // Create an SSH session with the specified host, port, username, and password
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);

            // Set additional configuration options if needed
            session.setConfig("StrictHostKeyChecking", "no");

            // Create an SSH client interaction object using the session
            SSHClientInteraction sshClient = new SSHClientInteraction(session);

            // Connect to the SSH server
            sshClient.connect();

            // Execute a command on the SSH server
            String command = "echo 'Hello, World!'";  // Replace with your desired command
            sshClient.send(command);

            // Wait for the command to complete and capture the output
            String commandOutput = sshClient.expect(".*");  // Capture all output

            // Print the command output
            System.out.println("Command Output:");
            System.out.println(commandOutput);

            // Close the SSH session
            sshClient.close();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}