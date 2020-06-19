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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


class outOfOffice {
    private static String email = "cos332receiver@sitecheck.co.za";
    private static String password = "&V46CrD?1S4x";
    private static String mailServer = "mail.sitecheck.co.za";
    public static void main(String[] args) throws InterruptedException {

        // Read existing emails from stored txt files
        List<String> OldReceivers = new ArrayList<String>();
        List<String> OldUIDL = new ArrayList<String>();
        try {
            OldReceivers = Files.readAllLines(new File("Receivers.txt").toPath(), Charset.defaultCharset());
            OldUIDL = Files.readAllLines(new File("UIDL.txt").toPath(), Charset.defaultCharset());
        } catch (Exception e) {
        }

        // START POP3 SECTION
        Socket mySocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        Boolean isEmailToSend = true;
        List<String> Receivers = new ArrayList<String>();

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
              //  System.out.println(line);
                if (line.split(" ").length > 1) { // skip the trailing "."
                    EmailNumbers.add(line.split(" ")[0]);
                    UIDL.add(line.split(" ")[1]);
                }
                Thread.sleep(10);
            }

            //retrieve new emails
            for (String item : UIDL) {
                String currentReceiver = "";
                isEmailToSend = true;
                if(!OldUIDL.contains(item)){
                    out.println("top "+EmailNumbers.get(UIDL.indexOf(item)) + " 0");
                    Thread.sleep(100);

                    //read entire email
                    line = "";
                    while (!line.equals(".")) {
                        line = in.readLine();

                        //check subject
                        if(line.contains("Subject:"))
                            if(! line.contains("prac7"))
                                isEmailToSend = false;
                        
                         //see if there are others cc'd (mailing list)
                         if(line.length()>2)
                         if(line.substring(0,3).equals("To:"))
                             if(line.contains(","))
                                isEmailToSend = false;

                        //get respond adress
                        if(line.contains("Return-Path:")){
                           currentReceiver = (line.substring(line.indexOf("<")+1, line.indexOf(">")));
                        }                       
                    }
                    if(isEmailToSend && !OldReceivers.contains(currentReceiver)){
                        OldReceivers.add(currentReceiver);
                        Receivers.add(currentReceiver);
                    }
                }
            }

            if(Receivers.size() == 0){
                System.out.println("No new mails (or mails that satisfy criteria to send out of office)");
            } else {
                SendOutOfOffice(Receivers);
            }

            Path outReceivers = Paths.get("Receivers.txt");
            Files.write(outReceivers, OldReceivers, Charset.defaultCharset());

            Path outUIDL = Paths.get("UIDL.txt");
            Files.write(outUIDL, UIDL, Charset.defaultCharset());

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

    private static void SendOutOfOffice(List<String> receivers){
        if(! (receivers.size() > 0))
            return;

        System.out.println("Sending Emails (please wait for server)...");
        Socket pingSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            for (String receiver : receivers) {
                pingSocket = new Socket("sitecheck.co.za", 25);
                out = new PrintWriter(pingSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(pingSocket.getInputStream()));
    
                //greet server
                out.println("ehlo sitecheck.co.za");
               
                //wait for server to respond
                Thread.sleep(3000);
    
                //authenticate SMTP
                out.println("AUTH LOGIN");
    
                //wait for server to respond
                Thread.sleep(2000);
              
                //send username
                out.println(Base64.getEncoder().encodeToString(email.getBytes("utf-8")));
    
                //wait for server to respond
                Thread.sleep(2000);
    
                //send password
                out.println(Base64.getEncoder().encodeToString(password.getBytes("utf-8")));
    
                //wait for server to respond
                Thread.sleep(2000);
    
                
                out.println("mail from: cos332receiver@sitecheck.co.za");
                out.println("rcpt to: " + receiver);
                out.println("data");
                out.println("To: " + receiver);
                out.println("From: " + email);
                out.println("Subject: On Vacation");
                out.println("Hi! I will respond to your email once I am back from holiday! :)");
                out.println("");
                out.println("Regards,");
                out.println("Your COS332 Friend");
                out.println(".");
                
                out.println("quit");
    
                String inputLine;
    
                //Print server responses
                while ((inputLine = in.readLine()) != null) 
                    System.out.println(inputLine);
    
                out.close();
                in.close();
                pingSocket.close();

                System.out.println("Sent to: " + receivers);
            }
        } catch (IOException e) {
            return;
        } catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
}