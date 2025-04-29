package com.example.Secondhand.service;

import com.example.Secondhand.model.Contact;
import com.example.Secondhand.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Transactional
    public Contact submitContactForm(String email, String subject, String message) {
        Contact contact = new Contact();
        contact.setEmail(email);
        contact.setSubject(subject);
        contact.setMessage(message);
        return contactRepository.save(contact);
    }
} 