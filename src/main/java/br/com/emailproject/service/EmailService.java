package br.com.emailproject.service;



import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Startup;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import br.com.emailproject.model.Email;
import br.com.emailproject.util.LogUtil;

@Startup
public class EmailService extends Thread {

	private List<Email> emails; /* Atributo de Lista de emails a ser enviado */
	
	public static final String HEADER_CONTEXT = "text/html; charset=utf-8";
	
	public void enviar(Email email) {
		emails = new ArrayList<>();/* Inicialiazar uma nova Lista de emails a ser enviado */
		emails.add(email);/* Adicionando email na lista de email informado por parametro */
		send();
	}

	public void envia(List<Email> emails) {/* Método de enviar fazendo sobrecarga para receber a lista de emails */
		this.emails = emails;/* Passando para o atributo a lista de emails informado por parametro */
		send();
	}

	private EmailService copy() {/* Método que faz uma cópia da classe de EmailService */
		EmailService emailService = new EmailService(); /* Criando um novo serviço */
		emailService.emails = emails;/* Adicionando a lista de emails passada no atributo */
		return emailService;/* Retorna a Copia do serviço criada */
	}

	private void send() {/* Método que vai enviar os emails */
		new Thread(this.copy()).start();
	}

	@Override
	public void run() {

		Properties props = new Properties();

		props.put("mail.smtp.startls", true);
		props.put("email.smtp.host", System.getProperty("email-project.email.smtp.host"));
		props.put("email.smtp.port", System.getProperty("email-project.email.smtp.port"));

		javax.mail.Session session = javax.mail.Session.getInstance(props); /* Criando uma session do java mail */
		session.setDebug(false);

		for (Email email : emails) {

			try {

				Message message = new MimeMessage(session);
				message.setFrom(new InternetAddress(System.getProperty("email-project.email.from")));
				
				if (email.getDestinatario().contains("/")) {
					List<InternetAddress> emailsLocal = new ArrayList<>();
					for(String e : email.getDestinatario().split("/")) {
						emailsLocal.add(new InternetAddress(e));
					}
				
				message.addRecipients(Message.RecipientType.TO,emailsLocal.toArray(new InternetAddress[0]));
				
				}else {
					InternetAddress para = new InternetAddress(email.getDestinatario());
					message.addRecipient(Message.RecipientType.TO, para);
				}
				
				message.setSubject(email.getAssunto());
				
				MimeBodyPart textpart = new MimeBodyPart();
				textpart.setHeader("content-type", HEADER_CONTEXT);
				textpart.setContent(email.getTexto(),HEADER_CONTEXT);
				
				Multipart mp = new MimeMultipart();
				mp.addBodyPart(textpart);
				message.setContent(mp);
				Transport.send(message);
				
			} catch (MessagingException e) {
				LogUtil.getLogger(EmailService.class).error("Erro ao enviar e-mail" + e.getMessage());
			}
		}
	}
}
