package com.zgrylmz.springSecurity.service.Impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	// "emailExecutor" isimli thread pool'u kullanarak async çalışacak
	@Async("emailExecutor")
	public void sendEmail(String to, String subject, String body) {
		// Burada mail gönderme işlemini yapacaksın
		// Örneğin SMTP çağrısı, mail içeriği hazırlama vs.

		System.out.println("Sending email to: " + to + " by thread: " + Thread.currentThread().getName());

		// Simülasyon: mail gönderme süresi
		try {
			Thread.sleep(3000); // 3 saniye bekle
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		System.out.println("Email sent to: " + to);
	}
}
