package net.awesomepowered.crashalerter;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CrashAlerter extends JavaPlugin {
	
	
	protected boolean updated = false;
	protected boolean running = true;
	protected boolean shuttingDown = false;
	protected int FreezeTime = 0;
	protected int sinceShutdown = 0;
	public int WaitTime;
	public String prefix = "[Crash] ";
	public String alertMessage;
	public String sendTo;
	
	//todo StringList for sendTo
	public void onEnable() {
		configStuff();
		startPlugin();
		printDebug();
	}
	
	public void configStuff() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		WaitTime = getConfig().getInt("WaitTime");
		sendTo = getConfig().getString("Mail.sendTo");
		alertMessage = getConfig().getString("Mail.AlertMessage");
	}
	
	public void printDebug() {
		System.out.println(prefix + "Wait time until sending " + WaitTime + " seconds");
		System.out.println(prefix + "Sending email to " + sendTo);
		System.out.println(prefix + "Email message is " + alertMessage);
	}
	
	public void startPlugin() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				updated = true;
			}
		}, 1, 1);
		new Checker().start();	
	}
	
	public void onDisable() {
		running = false;
	}
	
	//Checker code by Double0Negative
	class Checker extends Thread {
		public void run() {
			while(running || shuttingDown) {
				if(!updated && !shuttingDown) {
					FreezeTime++;
					System.out.println(prefix + "Server unresponsive for " + FreezeTime + " second(s)...");
				} else {
					updated = false;
					FreezeTime = 0;
				} if(FreezeTime == WaitTime) {
					configStuff();
					System.out.println(prefix + "Server was unresponsive for " + WaitTime + " second(s)!");
					System.out.println(prefix + "Sending alert message.");
					sendAlert(sendTo, alertMessage);
					Bukkit.shutdown();
					shuttingDown = true;
				} if(shuttingDown) {
					if(sinceShutdown == 30) {
						System.out.println(prefix + "Killing Server!");
						Runtime.getRuntime().halt(1);
					}
					System.out.println(prefix + "killing server in "+(30-sinceShutdown)+" second(s)...");
					sinceShutdown++;
				} try{ sleep(1000);} catch(Exception e) {}
			}		
		}
	}
	
	public void sendAlert(String To, String AM) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("Alerter@Timelord.com"));
            msg.addRecipient(Message.RecipientType.TO,
                             new InternetAddress(To));
            msg.setSubject("Crash Alert");
            msg.setText(AM);
            Transport.send(msg);
            System.out.println("Message sent!");

        } catch (AddressException e) {
            // ...
        } catch (MessagingException e) {
            // ...
        }
	}
}