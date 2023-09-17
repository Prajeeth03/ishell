package ishell;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SSHClientInteraction {
    private Session session;     // SSH session object
    private Channel channel;     // SSH channel for communication
    private InputStream in;      // Input stream for reading data from SSH channel
    private OutputStream out;    // Output stream for sending data to SSH channel

    // Constructor
    public SSHClientInteraction(Session session) {
        this.session = session;
    }

    /**
     * Connects to the SSH server.
     *
     * @throws JSchException If a JSch exception occurs during the connection.
     * @throws IOException   If an I/O exception occurs during the connection.
     */
    public void connect() throws JSchException, IOException {
        this.session.connect();
        this.channel = this.session.openChannel("shell");
        this.in = this.channel.getInputStream();
        this.out = this.channel.getOutputStream();
        this.channel.connect();
    }

    /**
     * Waits for a specified pattern to appear in the SSH output.
     *
     * @param pattern The regular expression pattern to match in the output.
     * @return The captured output that matches the pattern.
     * @throws Exception If an exception occurs during the operation.
     */
    public String expect(String pattern) throws Exception {
        StringBuilder output = new StringBuilder();
        byte[] buffer = new byte[1024];
        Pattern regexPattern = Pattern.compile(pattern);

        Matcher matcher;
        do {
            while (this.in.available() <= 0) {
                if (this.channel.isClosed()) {
                    if (this.in.available() <= 0) {
                        return output.toString();
                    }
                } else {
                    Thread.sleep(1000L);
                }
            }

            int bytesRead = this.in.read(buffer);
            String data = new String(buffer, 0, bytesRead);
            output.append(data);
            matcher = regexPattern.matcher(output.toString());
        } while (!matcher.find());

        return output.toString();
    }

    /**
     * Sends a command to the SSH server.
     *
     * @param command The command to send.
     * @throws IOException If an I/O exception occurs during the operation.
     */
    public void send(String command) throws IOException {
        this.out.write((command + "\n").getBytes());
        this.out.flush();
    }

    /**
     * Uploads or downloads a file between the local machine and the remote server.
     *
     * @param localFilePath  The path to the local file (for uploading) or the destination path on the local machine (for downloading).
     * @param remoteFilePath The path to the remote file (for downloading) or the destination path on the remote server (for uploading).
     * @param upload         True if uploading from local to remote, false if downloading from remote to local.
     * @throws JSchException If a JSch exception occurs during the operation.
     * @throws SftpException If an SFTP exception occurs during the operation.
     */
    public void file(String localFilePath, String remoteFilePath, boolean upload) throws JSchException, SftpException {
        Channel channel = this.session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;

        try {
            if (upload) {
                sftpChannel.put(localFilePath, remoteFilePath); // Upload from local to remote
            } else {
                sftpChannel.get(remoteFilePath, localFilePath); // Download from remote to local
            }
        } finally {
            sftpChannel.disconnect();
            channel.disconnect();
        }
    }

    /**
     * Executes a remote command that streams and displays the output (similar to 'tail -f' command).
     *
     * @param command The command to execute, typically used to tail a file.
     * @throws JSchException If a JSch exception occurs during the operation.
     * @throws IOException   If an I/O exception occurs during the operation.
     */
    public void tail(String command) throws JSchException, IOException {
        Channel channel = this.session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command); // Set the command to execute
        channel.setInputStream((InputStream) null);
        ((ChannelExec) channel).setErrStream(System.err);
        InputStream in = channel.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        channel.connect();

        String line;
        while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
        channel.disconnect();
    }

    /**
     * Closes the SSH channel and session.
     */
    public void close() {
        this.channel.disconnect();
        this.session.disconnect();
    }
}