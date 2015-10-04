package com.allanditzel.easymassmailer.config;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Allan Ditzel
 * @since 1.0.0
 */
@SpringBootApplication
public class EasyMassMailer implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(EasyMassMailer.class);
	private static final int NUMBER_OF_COLUMNS_IN_CSV_FILE = 1;

	@Value("${email.input.file}")
	private String emailFilePath;

	@Value("${email.template.file}")
	private String emailTemplateFilePath;
	
	@Value("${application.version}")
	private String emailVersion;

	@Autowired
	private JavaMailSender mailSender;

	public static void main(String[] args) {
		SpringApplication massMailer = new SpringApplication(EasyMassMailer.class);
		massMailer.setWebEnvironment(false);
		massMailer.run(args);
	}

	@Override
	public void run(String... strings) throws Exception {
		log.info("Reading email addresses from {}", emailFilePath);

		Resource inputEmailsResource = new FileSystemResource(emailFilePath);
		CSVReader reader = new CSVReader(new FileReader(inputEmailsResource.getFile()));

		String [] nextLine;
		while ((nextLine = reader.readNext()) != null && nextLine.length == NUMBER_OF_COLUMNS_IN_CSV_FILE) {
			String email = nextLine[0];
			if (StringUtils.hasText(email)) {
				log.info("Sending email to: {}", email);
				MimeMessage mimeMessage = mailSender.createMimeMessage();
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
				message.setTo(email);
				message.setFrom("tango.at.maryland@gmail.com");
				message.setSubject("Tango Tidbits");

				BufferedReader br = new BufferedReader(new FileReader("src/main/resources/templates/tangoTidbits.html"));
				String text = "", line;

				while((line = br.readLine()) != null) {
					text += line;
				}

				message.setText(text, true);
				this.mailSender.send(mimeMessage);
				br.close();
			}
		}
		reader.close();
	}
}
