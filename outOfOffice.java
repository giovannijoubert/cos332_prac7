import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;

class outOfOffice {
    public static void main(String[] args) throws InterruptedException {
        String email = "cos332receiver@sitecheck.co.za";
        String password = "&V46CrD?1S4x";
        String mailServer = "mail.sitecheck.co.za";

        // Read existing emails from stored txt files
        List<String> OldEmails = new ArrayList<String>();
        List<String> OldUIDL = new ArrayList<String>();
        try {
            OldEmails = Files.readAllLines(new File("EmailNumbers.txt").toPath(), Charset.defaultCharset());
            OldUIDL = Files.readAllLines(new File("UIDL.txt").toPath(), Charset.defaultCharset());
        } catch (Exception e) {
        }

        // START POP3 SECTION
        Socket mySocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            mySocket = new Socket(mailServer, 110);
            out = new PrintWriter(mySocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

            // Greeting: +OK Dovecot ready
            Thread.sleep(100);
            System.out.println(in.readLine());

            // send email address to POP3: +OK
            out.println("user " + email);
            Thread.sleep(100);
            System.out.println(in.readLine());
            Thread.sleep(100);

            // send password to POP3: +OK Logged in.
            out.println("pass " + password);
            Thread.sleep(100);
            System.out.println(in.readLine());
            Thread.sleep(100);

            // retrieve complete list of UIDLs: +OK x messages:
            out.println("list");
            Thread.sleep(100);
            System.out.println(in.readLine());
            Thread.sleep(100);

            List<String> UIDL = new ArrayList<String>();
            List<String> EmailNumbers = new ArrayList<String>();
            String line = "";

            while (!line.equals(".")) {
                line = in.readLine();
                if (line.split(" ").length > 1) { // skip the trailing "."
                    EmailNumbers.add(line.split(" ")[0]);
                    UIDL.add(line.split(" ")[1]);
                }
                Thread.sleep(10);
            }

            //retrieve new emails
            for (String item : UIDL) {
                if(!OldUIDL.contains(item)){
                    out.println("retr "+EmailNumbers.get(UIDL.indexOf(item)));
                    Thread.sleep(1000);

                    //read entire email
                    line = "";
                    while (!line.equals(".")) {
                        line = in.readLine();

                        //get respond adress
                        if(line.contains("Return-Path:"))
                            System.out.println(line);

                        //check subject
                        if(line.contains("Subject:"))
                            System.out.println(line);

                        //see if there are others cc'd (mailing list)
                        if(line.length()>2)
                        if(line.substring(0,3).equals("To:"))
                            System.out.println(line);
                    }
                }
            }
          

            //TODO: Delete email from TXT file if it has been deleted from POP3 server
            /* STORE NEW EMAILS TO TXT FILES
            Path outEmailNumbers = Paths.get("EmailNumbers.txt");
            Files.write(outEmailNumbers, EmailNumbers, Charset.defaultCharset());

            Path outUIDL = Paths.get("UIDL.txt");
            Files.write(outUIDL, UIDL, Charset.defaultCharset());
            */

       /*     out.println("retr 1");
            Thread.sleep(100);
            System.out.println(in.readLine()); */

            // Logout: +OK Logging out.
            out.println("quit");
            Thread.sleep(100);
            System.out.println(in.readLine());

            out.close();
            in.close();
            mySocket.close();

        } catch (IOException e) {
            return;
        }

    }
}