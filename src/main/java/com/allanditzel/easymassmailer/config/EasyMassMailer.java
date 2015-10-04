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
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.VariablesMap;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Allan Ditzel
 * @since 1.0.0
 */
@SpringBootApplication
public class EasyMassMailer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(EasyMassMailer.class);
    private static final int NUMBER_OF_COLUMNS_IN_CSV_FILE = 3;

    @Value("${email.input.file}")
    private String emailFilePath;

    @Value("${email.template.file}")
    private String emailTemplateFilePath;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    private Context context = new Context(Locale.getDefault());

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
            String firstName = nextLine[0];
            String lastName = nextLine[1];
            String email = nextLine[2];
            if (StringUtils.hasText(email)) {
                log.info("Sending email to: {}", email);
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
                message.setTo(email);
                message.setFrom("allan@allanditzel.com");
                message.setSubject("My awesome subject");
                context.setVariable("firstName", firstName);
                context.setVariable("lastName", lastName);
                String text = this.templateEngine.process("default", context);
                message.setText(text, true);
                this.mailSender.send(mimeMessage);
            }
        }
    }
}
